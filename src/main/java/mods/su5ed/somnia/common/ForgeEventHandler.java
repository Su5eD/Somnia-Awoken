package mods.su5ed.somnia.common;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.api.capability.FatigueCapabilityProvider;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.client.SomniaClient;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketOpenGUI;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.server.CommandSomnia;
import mods.su5ed.somnia.server.ServerTickHandler;
import mods.su5ed.somnia.util.SomniaUtil;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;
import java.util.Optional;

public class ForgeEventHandler
{
	@SubscribeEvent
	public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
		event.addCapability(new ResourceLocation(Somnia.MODID, "fatigue"), new FatigueCapabilityProvider());
	}

	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event) {
		CommandSomnia.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.player.world.isRemote || (event.player.isCreative() || event.player.isSpectator() && !event.player.isSleeping())) return;

		event.player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			double fatigue = props.getFatigue();

			boolean isSleeping = props.sleepOverride() || event.player.isSleeping();

			if (isSleeping) fatigue -= SomniaConfig.fatigueReplenishRate;
			else fatigue += SomniaConfig.fatigueRate;

			if (fatigue > 100) fatigue = 100;
			else if (fatigue < 0) fatigue = 0;

			props.setFatigue(fatigue);
			if (props.updateFatigueCounter() >= 100) {
				props.resetFatigueCounter();
				NetworkHandler.sendToClient(new PacketUpdateFatigue(fatigue), (ServerPlayerEntity) event.player);

				// Side effects
				if (SomniaConfig.fatigueSideEffects) {
					int lastSideEffectStage = props.getSideEffectStage();
					if (fatigue > SomniaConfig.sideEffectStage1 && lastSideEffectStage < SomniaConfig.sideEffectStage1) {
						props.setSideEffectStage(SomniaConfig.sideEffectStage1);
						event.player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage1Potion), SomniaConfig.sideEffectStage1Duration, SomniaConfig.sideEffectStage1Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage2 && lastSideEffectStage < SomniaConfig.sideEffectStage2) {
						props.setSideEffectStage(SomniaConfig.sideEffectStage2);
						event.player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage2Potion), SomniaConfig.sideEffectStage2Duration, SomniaConfig.sideEffectStage2Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage3 && lastSideEffectStage < SomniaConfig.sideEffectStage3) {
						props.setSideEffectStage(SomniaConfig.sideEffectStage3);
						event.player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage3Potion), SomniaConfig.sideEffectStage3Duration, SomniaConfig.sideEffectStage3Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage4) {
						event.player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage4Potion), 150, SomniaConfig.sideEffectStage4Amplifier));
					} else if (fatigue < SomniaConfig.sideEffectStage1) {
						props.setSideEffectStage(-1);
						if (lastSideEffectStage < SomniaConfig.sideEffectStage2) event.player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage2Potion));
						else if (lastSideEffectStage < SomniaConfig.sideEffectStage3) event.player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage3Potion));
						else if (lastSideEffectStage < SomniaConfig.sideEffectStage4) event.player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage4Potion));
					}
				}
			}
		});

	}

	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event) {
		PlayerEntity player = event.getPlayer();
		player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			if (props.shouldSleepNormally() && player.sleepTimer == 100) {
				props.setFatigue(props.getFatigue() - SomniaUtil.calculateFatigueToReplenish(player));
			}
			props.maxFatigueCounter();
			props.shouldResetSpawn(true);
			props.setSleepNormally(false);
		});
		
		if (player.world.isRemote) SomniaClient.autoWakeTime = -1;

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
		PlayerEntity player = event.getPlayer();
		Optional<IFatigue> props = player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).resolve();
		if (props.isPresent()) {
			if (props.get().shouldSleepNormally()) {
				return;
			}
		}
		if (!SomniaUtil.isEnterSleepTime()) event.setResult(Event.Result.DENY);
		else event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		if (!SomniaUtil.checkFatigue(player)) {
			player.sendStatusMessage(new TranslationTextComponent("somnia.status.cooldown"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}
		else if (!SomniaConfig.sleepWithArmor && !player.isCreative() && SomniaUtil.doesPlayerWearArmor(player)) {
			player.sendStatusMessage(new TranslationTextComponent("somnia.status.armor"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}

		player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			props.setSleepNormally(player.isSneaking());
		});
	}

	@SubscribeEvent
	public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
		event.getPlayer().getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			if (!props.resetSpawn()) event.setCanceled(true);
		});
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (!event.getEntity().world.isRemote) {
			event.getOriginal().getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
				CompoundNBT old = props.serializeNBT();
				event.getPlayer().getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(fatigue -> {
					fatigue.deserializeNBT(old);
				});
			});
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	private void sync(ServerPlayerEntity player) {
		player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			NetworkHandler.sendToClient(new PacketUpdateFatigue(props.getFatigue()), player);
		});
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if (!world.isRemote) {
			BlockPos pos = event.getPos();
			Direction direction = world.getBlockState(pos).get(HorizontalBlock.HORIZONTAL_FACING);
			PlayerEntity player = event.getPlayer();

			if (!((ServerPlayerEntity) player).func_241147_a_(pos, direction)) return;

			ItemStack stack = player.inventory.getCurrentItem();
			if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals(SomniaConfig.wakeTimeSelectItem)) {
				NetworkHandler.sendToClient(new PacketOpenGUI(), (ServerPlayerEntity) player);
				event.setCancellationResult(ActionResultType.SUCCESS);
				event.setCanceled(true);
			}
		}

	}

	@SubscribeEvent
	public void worldLoadHook(WorldEvent.Load event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			Somnia.LOGGER.info("Registering tick handler for loading world!");
		}
	}

	@SubscribeEvent
	public void worldUnloadHook(WorldEvent.Unload event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Iterator<ServerTickHandler> iter = Somnia.instance.tickHandlers.iterator();
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

	@SubscribeEvent
	public void onPlayerDamage(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof ServerPlayerEntity) {
			if (!(event.getEntityLiving()).isSleeping()) return;

			NetworkHandler.sendToClient(new PacketWakeUpPlayer(), (ServerPlayerEntity) event.getEntityLiving());
		}
	}
}
