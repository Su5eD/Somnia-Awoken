package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.compat.Compat;
import mods.su5ed.somnia.compat.DarkUtilsPlugin;
import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerSleepTickHandler {
	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		event.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
			if (event.phase == TickEvent.Phase.START) tickStart(props, event.player);
			else tickEnd(props, event.player);
		});

	}

	public static void tickStart(IFatigue props, PlayerEntity player) {
		if (player.isSleeping()) {

			if (props.shouldSleepNormally() || (player.getSleepTimer() > 99 && ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) || Compat.isSleepingInHammock(player)) {
				props.setSleepOverride(false);
				return;
			}

			props.setSleepOverride(true);
			
			if (SomniaConfig.fading) {
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99) sleepTimer = 98;
				player.sleepTimer = sleepTimer;
			}
		}
	}

	public static void tickEnd(IFatigue props, PlayerEntity player) {
		if (props.sleepOverride()) {
			player.startSleeping(player.getBedPosition().orElse(player.getPosition()));
			props.setSleepOverride(false);
		}
	}
}