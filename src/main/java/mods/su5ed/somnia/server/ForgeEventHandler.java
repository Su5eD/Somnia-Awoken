package mods.su5ed.somnia.server;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.FatigueCapabilityProvider;
import mods.su5ed.somnia.client.gui.GuiSelectWakeTime;
import mods.su5ed.somnia.common.PlayerSleepTickHandler;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketGUIOpen;
import mods.su5ed.somnia.network.packet.PacketPropUpdate;
import mods.su5ed.somnia.network.packet.PacketWakePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;

public class ForgeEventHandler
{
	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
	{
		if (!event.getObject().world.isRemote) event.addCapability(new ResourceLocation(Somnia.MOD_ID, "fatigue"), new FatigueCapabilityProvider());
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.START || event.player.world.isRemote || (event.player.isCreative() && !event.player.isSleeping())) return;
		
		PlayerEntity player = event.player;

		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			double fatigue = props.getFatigue();

			boolean isSleeping = PlayerSleepTickHandler.serverState.sleepOverride || player.isSleeping();

			if (isSleeping)
				fatigue -= SomniaConfig.fatigueReplenishRate;
			else
				fatigue += SomniaConfig.fatigueRate;

			if (fatigue > 100.0d)
				fatigue = 100.0d;
			else if (fatigue < .0d)
				fatigue = .0d;

			props.setFatigue(fatigue);
			if (props.updateFatigueCounter() >= 100)
			{
				props.resetFatigueCounter();
				Pair<PacketBuffer, Integer> packet = new PacketPropUpdate(0x01, 0x00, fatigue).buildPacket();
				NetworkHandler.sendToClient(packet.getLeft(), packet.getRight(), (ServerPlayerEntity) player);

				// Side effects
				if (SomniaConfig.fatigueSideEffects)
				{
					int lastSideEffectStage = props.getSideEffectStage();
					if (fatigue > SomniaConfig.sideEffectStage1 && lastSideEffectStage < SomniaConfig.sideEffectStage1)
					{
						props.setSideEffectStage(SomniaConfig.sideEffectStage1);
						player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage1Potion), SomniaConfig.sideEffectStage1Duration, SomniaConfig.sideEffectStage1Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage2 && lastSideEffectStage < SomniaConfig.sideEffectStage2)
					{
						props.setSideEffectStage(SomniaConfig.sideEffectStage2);
						player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage2Potion), SomniaConfig.sideEffectStage2Duration, SomniaConfig.sideEffectStage2Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage3 && lastSideEffectStage < SomniaConfig.sideEffectStage3)
					{
						props.setSideEffectStage(SomniaConfig.sideEffectStage3);
						player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage3Potion), SomniaConfig.sideEffectStage3Duration, SomniaConfig.sideEffectStage3Amplifier));
					}
					else if (fatigue > SomniaConfig.sideEffectStage4)
						player.addPotionEffect(new EffectInstance(Effect.get(SomniaConfig.sideEffectStage4Potion), 150, SomniaConfig.sideEffectStage4Amplifier));
					else if (fatigue < SomniaConfig.sideEffectStage1) {
						props.setSideEffectStage(-1);
						if (lastSideEffectStage < SomniaConfig.sideEffectStage2) player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage2Potion));
						else if (lastSideEffectStage < SomniaConfig.sideEffectStage3) player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage3Potion));
						else if (lastSideEffectStage < SomniaConfig.sideEffectStage4) player.removePotionEffect(Effect.get(SomniaConfig.sideEffectStage4Potion));
					}
				}
			}
		});

	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onWakeUp(PlayerWakeUpEvent event) {
		PlayerEntity player = event.getPlayer();
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			props.maxFatigueCounter();
			props.shouldResetSpawn(true);
		});
		
		if (player.world.isRemote) {
			Somnia.clientAutoWakeTime = -1;
		}
	}

	private final ResourceLocation CHARM_SLEEP =  new ResourceLocation("darkutils", "charm_sleep");

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (!event.getEntity().world.isRemote) {
			event.getOriginal().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
				CompoundNBT old = props.serializeNBT();
				event.getPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(fatigue -> {
					fatigue.deserializeNBT(old);
				});
			});
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		sync((ServerPlayerEntity) event.getPlayer());
	}

	private void sync(ServerPlayerEntity player) {
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			Pair<PacketBuffer, Integer> packet = new PacketPropUpdate(0x01, 0x00, props.getFatigue()).buildPacket();
			NetworkHandler.sendToClient(packet.getLeft(), packet.getRight(), player);
		});
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void interactHook(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerEntity player = event.getPlayer();

		if (!world.isRemote && ((ServerPlayerEntity) player).func_241147_a_(pos, null)) return; //pos can be null

		ItemStack stack = player.inventory.getCurrentItem();
		ResourceLocation registryName = stack.getItem().getRegistryName();
		if (stack != ItemStack.EMPTY && registryName != null && registryName.equals(SomniaConfig.wakeTimeSelectItem)) {
			if (world.isRemote) {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.currentScreen instanceof GuiSelectWakeTime) return;
			}
			else {
				Pair<PacketBuffer, Integer> packet = new PacketGUIOpen().buildPacket();
				NetworkHandler.sendToClient(packet.getLeft(), packet.getRight(), (ServerPlayerEntity) player);
			}

			event.setCancellationResult(ActionResultType.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void worldLoadHook(WorldEvent.Load event)
	{
		if (event.getWorld() instanceof ServerWorld)
		{
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			Somnia.LOGGER.info("Registering tick handler for loading world!");
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void worldUnloadHook(WorldEvent.Unload event)
	{
		if (event.getWorld() instanceof ServerWorld)
		{
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Iterator<ServerTickHandler> iter = Somnia.instance.tickHandlers.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext())
			{
				serverTickHandler = iter.next();
				if (serverTickHandler.worldServer == worldServer)
				{
					Somnia.LOGGER.info("Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerDamage(LivingHurtEvent event)
	{
		if (event.getEntityLiving() instanceof ServerPlayerEntity)
		{
			if (!(event.getEntityLiving()).isSleeping())
				return;

			Pair<PacketBuffer, Integer> packet = new PacketWakePlayer().buildPacket();
			NetworkHandler.sendToClient(packet.getLeft(), packet.getRight(), (ServerPlayerEntity) event.getEntityLiving());
		}
	}

	/*
	 * Re-implementation of the sleep method.
	 */
	/*@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SuppressWarnings("unused")
	public void onSleep(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		BlockPos pos = event.getPos();
		Direction direction = player.world.getBlockState(pos).get(HorizontalBlock.HORIZONTAL_FACING);
		if (!player.world.isRemote) {
			if (player.isSleeping() || !player.isAlive()) {
				event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
				return;
			}

			if (!player.world.dimension.isSurfaceWorld()) {
				event.setResult(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE);
				return;
			}

			if (!ServerProxy.enterSleepPeriod.isTimeWithin(24000L)) {
				player.setSpawnPoint(pos, false, true, player.dimension);
				event.setResult(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
				return;
			}

			if (!player.bedInRange(pos, direction)) {
				event.setResult(PlayerEntity.SleepResult.TOO_FAR_AWAY);
				return;
			}

			if (player.bedBlocked(pos, direction)) {
				event.setResult(PlayerEntity.SleepResult.OBSTRUCTED);
				return;
			}

			if (!player.isCreative()) {
				double d0 = 8.0D;
				double d1 = 5.0D;
				Vec3d vec3d = new Vec3d((double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D);
				List<MonsterEntity> list = player.world.getEntitiesWithinAABB(MonsterEntity.class, new AxisAlignedBB(vec3d.getX() - 8.0D, vec3d.getY() - 5.0D, vec3d.getZ() - 8.0D, vec3d.getX() + 8.0D, vec3d.getY() + 5.0D, vec3d.getZ() + 8.0D), p_213820_1_ -> p_213820_1_.isPreventingPlayerRest(player));
				if (!list.isEmpty()) {
					event.setResult(PlayerEntity.SleepResult.NOT_SAFE);
					return;
				}
			}
		}

		player.startSleeping(pos);
		player.sleepTimer = 0;
		if (player.world instanceof ServerWorld) {
			((ServerWorld)player.world).updateAllPlayersSleepingFlag();
		}

		event.setResult(IForgeDimension.SleepResult.ALLOW);
	}*/
}
