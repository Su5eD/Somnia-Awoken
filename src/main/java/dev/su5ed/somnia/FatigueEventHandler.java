package dev.su5ed.somnia;

import dev.su5ed.somnia.api.SomniaAPI;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.FatigueUpdatePacket;
import dev.su5ed.somnia.network.client.OpenGUIPacket;
import dev.su5ed.somnia.util.SideEffectStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class FatigueEventHandler {

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
        UseAnim action = stack.getUseAnimation();
        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
            Item item = stack.getItem();
            Stream.concat(SomniaConfig.COMMON.getReplenishingItems().stream(), SomniaAPI.getReplenishingItems().stream())
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

    private FatigueEventHandler() {}
}
