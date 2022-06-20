package dev.su5ed.somnia.handler;

import dev.su5ed.somnia.api.SomniaAPI;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.IFatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.compat.DarkUtilsPlugin;
import dev.su5ed.somnia.core.Somnia;
import dev.su5ed.somnia.core.SomniaConfig;
import dev.su5ed.somnia.core.SomniaObjects;
import dev.su5ed.somnia.network.FatigueUpdatePacket;
import dev.su5ed.somnia.network.OpenGUIPacket;
import dev.su5ed.somnia.network.PlayerWakeUpPacket;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.util.ASMHooks;
import dev.su5ed.somnia.util.SideEffectStage;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class ForgeEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level.isClientSide || (!event.player.isAlive() || event.player.isCreative() || event.player.isSpectator() && !event.player.isSleeping())) return;

        event.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
            double fatigue = props.getFatigue();
            double extraFatigueRate = props.getExtraFatigueRate();
            double replenishedFatigue = props.getReplenishedFatigue();
            boolean isSleeping = props.sleepOverride() || event.player.isSleeping();

            double fatigueRate = SomniaConfig.COMMON.fatigueRate.get();
            double fatigueReplenishRate = SomniaConfig.COMMON.fatigueReplenishRate.get();

            if (isSleeping) {
                fatigue -= fatigueReplenishRate;
                double share = fatigueReplenishRate / fatigueRate;
                double replenish = fatigueReplenishRate * share;
                extraFatigueRate -= fatigueReplenishRate / share / replenishedFatigue / 10;
                replenishedFatigue -= replenish;
            } else {
                double rate = fatigueRate;

                MobEffectInstance wakefulness = event.player.getEffect(SomniaObjects.AWAKENING_EFFECT.get());
                if (wakefulness != null) {
                    rate -= wakefulness.getAmplifier() == 0 ? rate / 4 : rate / 3;
                }

                MobEffectInstance insomnia = event.player.getEffect(SomniaObjects.INSOMNIA_EFFECT.get());
                if (insomnia != null) {
                    rate += insomnia.getAmplifier() == 0 ? rate / 2 : rate;
                }
                fatigue += rate + props.getExtraFatigueRate();
            }

            if (fatigue > 100) fatigue = 100;
            else if (fatigue < 0) fatigue = 0;

            if (replenishedFatigue > 100) replenishedFatigue = 100;
            else if (replenishedFatigue < 0) replenishedFatigue = 0;

            if (extraFatigueRate < 0) extraFatigueRate = 0;

            props.setFatigue(fatigue);
            props.setReplenishedFatigue(replenishedFatigue);
            props.setExtraFatigueRate(extraFatigueRate);

            if (props.updateFatigueCounter() >= 100) {
                props.resetFatigueCounter();
                SomniaNetwork.sendToClient(new FatigueUpdatePacket(fatigue), (ServerPlayer) event.player);

                if (SomniaConfig.COMMON.fatigueSideEffects.get()) {
                    int lastSideEffectStage = props.getSideEffectStage();
                    SideEffectStage[] stages = SideEffectStage.getSideEffectStages();
                    SideEffectStage firstStage = stages[0];
                    if (fatigue < firstStage.minFatigue) props.setSideEffectStage(-1);

                    for (SideEffectStage stage : stages) {
                        boolean permanent = stage.duration < 0;
                        if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue) {
                            props.setSideEffectStage(stage.minFatigue);
                            if (permanent || lastSideEffectStage < stage.minFatigue) {
                                event.player.addEffect(new MobEffectInstance(MobEffect.byId(stage.potionID), permanent ? 150 : stage.duration, stage.amplifier));
                            }
                        }
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) ServerTickHandler.HANDLERS.forEach(ServerTickHandler::tickEnd);
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getPlayer();
        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
            if (props.shouldSleepNormally() || (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player))) {
                props.setFatigue(props.getFatigue() - SomniaUtil.getFatigueToReplenish(player));
            }
            props.maxFatigueCounter();
            props.shouldResetSpawn(true);
            props.setSleepNormally(false);
            props.setWakeTime(-1);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
        Player player = event.getPlayer();
        if (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) return;

        Optional<IFatigue> props = player.getCapability(CapabilityFatigue.INSTANCE).resolve();
        if (props.isPresent()) {
            if (props.get().shouldSleepNormally()) {
                return;
            }
        }
        if (!SomniaUtil.isEnterSleepTime()) event.setResult(Event.Result.DENY);
        else event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getPlayer();
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.cooldown"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        } else if (!SomniaConfig.COMMON.sleepWithArmor.get() && !player.isCreative() && SomniaUtil.doesPlayerWearArmor(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.armor"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setSleepNormally(player.isShiftKeyDown()));

        if (Compat.isSleepingInBag(player)) ASMHooks.updateWakeTime(player);
    }

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        event.getPlayer().getCapability(CapabilityFatigue.INSTANCE)
            .map(IFatigue::resetSpawn)
            .ifPresent(resetSpawn -> {
                if (!resetSpawn) event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        if (!level.isClientSide) {
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) return;
            Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
            Player player = event.getPlayer();

            if (!Compat.isBed(state, pos, level, player) || !((ServerPlayer) player).bedInRange(pos, direction)) return;

            ItemStack stack = player.getInventory().getSelected();
            if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals(SomniaConfig.COMMON.wakeTimeSelectItem.get())) {
                SomniaNetwork.sendToClient(new OpenGUIPacket(), (ServerPlayer) player);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }

    }

    @SubscribeEvent
    public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        Item item = stack.getItem();
        UseAnim action = stack.getUseAnimation();
        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
            Stream.of(SomniaConfig.COMMON.getReplenishingItems(), SomniaAPI.getReplenishingItems())
                .flatMap(Collection::stream)
                .filter(replenishingItem -> replenishingItem.item() == item)
                .findFirst()
                .ifPresent(replenishingItem -> event.getEntityLiving().getCapability(CapabilityFatigue.INSTANCE)
                    .ifPresent(props -> {
                        double fatigue = props.getFatigue();
                        double replenishedFatigue = props.getReplenishedFatigue();
                        double fatigueToReplenish = Math.min(fatigue, replenishingItem.replenishedFatigue());
                        double newFatigue = replenishedFatigue + fatigueToReplenish;
                        props.setReplenishedFatigue(newFatigue);

                        double baseMultiplier = replenishingItem.fatigueRateModifier();
                        double multiplier = newFatigue * 4 * SomniaConfig.COMMON.fatigueRate.get();
                        props.setExtraFatigueRate(props.getExtraFatigueRate() + baseMultiplier * multiplier);
                        props.setFatigue(fatigue - fatigueToReplenish);
                        props.maxFatigueCounter();
                    }));
        }
    }

    @SubscribeEvent
    public static void worldLoadHook(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            ServerTickHandler.HANDLERS.add(new ServerTickHandler(serverLevel));
            Somnia.LOGGER.info("Registering tick handler for loading world!");
        }
    }

    @SubscribeEvent
    public static void worldUnloadHook(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            Iterator<ServerTickHandler> iter = ServerTickHandler.HANDLERS.iterator();
            ServerTickHandler serverTickHandler;
            while (iter.hasNext()) {
                serverTickHandler = iter.next();
                if (serverTickHandler.serverLevel == serverLevel) {
                    Somnia.LOGGER.info("Removing tick handler for unloading world!");
                    iter.remove();
                    break;
                }
            }
        }
    }

    // we need the earliest PlayerEntity#hurt listener
    // because we have to set the sleep override to false before the mc stopSleeping call
    // otherwise PlayerSleepTickHandler#tickEnd will make the player to start sleeping again
    @SubscribeEvent
    public static void onPlayerDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity instanceof ServerPlayer player && entity.isSleeping()) {
            if (player.isInvulnerableTo(event.getSource())) return;
            if (player.isInvulnerable() && !event.getSource().isBypassInvul()) return;
            if (player.isOnFire() && player.hasEffect(MobEffects.FIRE_RESISTANCE)) return;

            entity.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setSleepOverride(false));
            entity.stopSleeping();
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), (ServerPlayer) entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        event.getEntityLiving().getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> {
                props.setFatigue(0);
                props.setReplenishedFatigue(0);
                props.setExtraFatigueRate(0);
            });
    }
    
    private ForgeEventHandler() {}
}
