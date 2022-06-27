package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.SomniaAPI;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.compat.Compat;
import mods.su5ed.somnia.compat.DarkUtilsPlugin;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.core.SomniaObjects;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketOpenGUI;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.util.ASMHooks;
import mods.su5ed.somnia.util.SideEffectStage;
import mods.su5ed.somnia.util.SomniaUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

@Mod.EventBusSubscriber
public class ForgeEventHandler {

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.player.level.isClientSide || (!event.player.isAlive() || event.player.isCreative() || event.player.isSpectator() && !event.player.isSleeping())) return;

		event.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
			double fatigue = props.getFatigue();
			double extraFatigueRate = props.getExtraFatigueRate();
			double replenishedFatigue = props.getReplenishedFatigue();
			boolean isSleeping = props.sleepOverride() || event.player.isSleeping();

			if (isSleeping) {
				fatigue -= SomniaConfig.fatigueReplenishRate;
				double share = SomniaConfig.fatigueReplenishRate / SomniaConfig.fatigueRate;
				double replenish = SomniaConfig.fatigueReplenishRate * share;
				extraFatigueRate -= SomniaConfig.fatigueReplenishRate / share / replenishedFatigue / 10;
				replenishedFatigue -= replenish;
			}
			else {
				double rate = SomniaConfig.fatigueRate;

				EffectInstance wakefulness = event.player.getEffect(SomniaObjects.AWAKENING_EFFECT.get());
				if (wakefulness != null) {
					rate -= wakefulness.getAmplifier() == 0 ? rate / 4 : rate / 3;
				}

				EffectInstance insomnia = event.player.getEffect(SomniaObjects.INSOMNIA_EFFECT.get());
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
				NetworkHandler.sendToClient(new PacketUpdateFatigue(fatigue), (ServerPlayerEntity) event.player);

				if (SomniaConfig.fatigueSideEffects) {
					int lastSideEffectStage = props.getSideEffectStage();
					SideEffectStage[] stages = SideEffectStage.getSideEffectStages();
					SideEffectStage firstStage = stages[0];
					if (fatigue < firstStage.minFatigue) props.setSideEffectStage(-1);

					for (SideEffectStage stage : stages) {
						boolean permanent = stage.duration < 0;
						if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue) {
							props.setSideEffectStage(stage.minFatigue);
							if (permanent || lastSideEffectStage < stage.minFatigue) {
								event.player.addEffect(new EffectInstance(Effect.byId(stage.potionID), permanent ? 150 : stage.duration, stage.amplifier));
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
		PlayerEntity player = event.getPlayer();
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
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
		PlayerEntity player = event.getPlayer();
		if (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) return;

		Optional<IFatigue> props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).resolve();
		if (props.isPresent()) {
			if (props.get().shouldSleepNormally()) {
				return;
			}
		}
		if (!SomniaUtil.isEnterSleepTime(player.level)) event.setResult(Event.Result.DENY);
		else event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		if (!SomniaUtil.checkFatigue(player)) {
			player.displayClientMessage(new TranslationTextComponent("somnia.status.cooldown"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}
		else if (!SomniaConfig.sleepWithArmor && !player.isCreative() && SomniaUtil.doesPlayerWearArmor(player)) {
			player.displayClientMessage(new TranslationTextComponent("somnia.status.armor"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}

		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> props.setSleepNormally(player.isShiftKeyDown()));

		if (Compat.isSleepingInBag(player)) ASMHooks.updateWakeTime(player);
	}

	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
		event.getPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.map(IFatigue::resetSpawn)
				.ifPresent(resetSpawn -> {
			if (!resetSpawn) event.setCanceled(true);
		});
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if (!world.isClientSide) {
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState(pos);
			if (!state.hasProperty(HorizontalBlock.FACING)) return;
			Direction direction = state.getValue(HorizontalBlock.FACING);
			PlayerEntity player = event.getPlayer();

			if (!Compat.isBed(state, pos, world, player) || !((ServerPlayerEntity) player).bedInRange(pos, direction)) return;

			ItemStack stack = player.inventory.getSelected();
			if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals(SomniaConfig.wakeTimeSelectItem)) {
				NetworkHandler.sendToClient(new PacketOpenGUI(), (ServerPlayerEntity) player);
				event.setCancellationResult(ActionResultType.SUCCESS);
				event.setCanceled(true);
			}
		}

	}

	@SubscribeEvent
	public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
		ItemStack stack = event.getItem();
		Item item = stack.getItem();
		UseAction action = stack.getUseAnimation();
		if (action == UseAction.EAT || action == UseAction.DRINK) {
			Stream.of(SomniaConfig.replenishingItems, SomniaAPI.getReplenishingItems())
					.flatMap(Collection::stream)
					.filter(pair -> pair.getLeft().getItem() == item)
					.findFirst()
					.ifPresent(pair -> event.getEntityLiving().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
							.ifPresent(props -> {
								double fatigue = props.getFatigue();
								double replenishedFatigue = props.getReplenishedFatigue();
								double coffeeFatigueReplenish = pair.getMiddle();
								double fatigueToReplenish = Math.min(fatigue, coffeeFatigueReplenish);
								double newFatigue = replenishedFatigue + fatigueToReplenish;
								props.setReplenishedFatigue(newFatigue);

								double baseMultiplier = pair.getRight();
								double multiplier = newFatigue * 4 * SomniaConfig.fatigueRate;
								props.setExtraFatigueRate(props.getExtraFatigueRate() + baseMultiplier * multiplier);
								props.setFatigue(fatigue - fatigueToReplenish);
								props.maxFatigueCounter();
							}));
		}
	}

	@SubscribeEvent
	public static void worldLoadHook(WorldEvent.Load event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			ServerTickHandler.HANDLERS.add(new ServerTickHandler(worldServer));
			Somnia.LOGGER.info("Registering tick handler for loading world!");
		}
	}

	@SubscribeEvent
	public static void worldUnloadHook(WorldEvent.Unload event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Iterator<ServerTickHandler> iter = ServerTickHandler.HANDLERS.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext()) {
				serverTickHandler = iter.next();
				if (serverTickHandler.worldServer == worldServer) {
					Somnia.LOGGER.info("Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}

	//we need the earliest PlayerEntity#hurt listener
	//because we have to set the sleep override to false before the mc stopSleeping call
	//otherwise PlayerSleepTickHandler#tickEnd will make the player to start sleeping again
	@SubscribeEvent
	public static void onPlayerDamage(LivingAttackEvent event) {
		LivingEntity entity = event.getEntityLiving();

		if (entity instanceof ServerPlayerEntity && entity.isSleeping()) {
			ServerPlayerEntity player = (ServerPlayerEntity) entity;

			if (player.isInvulnerableTo(event.getSource())) return;
			if (player.isInvulnerable() && !event.getSource().isBypassInvul()) return;
			if (player.isOnFire() && player.hasEffect(Effects.FIRE_RESISTANCE)) return;

			entity.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
					.ifPresent(props -> props.setSleepOverride(false));
			entity.stopSleeping();
			NetworkHandler.sendToClient(new PacketWakeUpPlayer(), (ServerPlayerEntity) entity);
		}
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		event.getEntityLiving().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.ifPresent(props -> {
					props.setFatigue(0);
					props.setReplenishedFatigue(0);
					props.setExtraFatigueRate(0);
				});
	}
}
