package mods.su5ed.somnia.common;

import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerSleepTickHandler {
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		event.player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			if (event.phase == TickEvent.Phase.START) tickStart(props, event.player);
			else tickEnd(props, event.player);
		});

	}

	public void tickStart(IFatigue props, PlayerEntity player) {
		if (player.isSleeping()) {
			//BlockPos pos = player.getBedLocation(player.dimension);

			//Reset fatigue in case you pick the charm up while sleeping. Doesn't trigger otherwise, because Somnia keeps the sleep timer below 100
			/*if (player.getSleepTimer() > 99 && Loader.isModLoaded("darkutils") && InvUtil.hasItem(player, CHARM_SLEEP)) {
				player.sleepTimer = 98;
			}*/ //TODO: DarkUtils compat

			if (props.shouldSleepNormally()) {
				props.setSleepOverride(false);
				return;
			}

			/*if (!CompatModule.isBed(player, pos)) {
				state.sleepOverride = false;
				return;
			}*/

			props.setSleepOverride(true);
			//player.stopSleepInBed(true, true);
			
			if (SomniaConfig.fading) {
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99) sleepTimer = 98;
				player.sleepTimer = sleepTimer;
			}
		}
	}

	public void tickEnd(IFatigue props, PlayerEntity player) {
		if (props.sleepOverride()) {
			player.startSleeping(player.getBedPosition().orElse(player.getPosition()));
			props.setSleepOverride(false);
		}
	}
}