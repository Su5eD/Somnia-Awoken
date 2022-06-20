package dev.su5ed.somnia.handler;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.IFatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.compat.DarkUtilsPlugin;
import dev.su5ed.somnia.core.Somnia;
import dev.su5ed.somnia.core.SomniaConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class PlayerSleepTickHandler {
	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		event.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
			if (event.phase == TickEvent.Phase.START) tickStart(props, event.player);
			else tickEnd(props, event.player);
		});

	}

	public static void tickStart(IFatigue props, Player player) {
		if (player.isSleeping()) {
			if (props.shouldSleepNormally() || (player.getSleepTimer() > 99 && Compat.darkUtilsLoaded && DarkUtilsPlugin.hasSleepCharm(player)) || Compat.isSleepingInHammock(player)) {
				props.setSleepOverride(false);
				return;
			}

			props.setSleepOverride(true);
			
			if (SomniaConfig.COMMON.fading.get()) {
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99) sleepTimer = 98;
				player.sleepCounter = sleepTimer;
			}
		}
	}

	public static void tickEnd(IFatigue props, Player player) {
		if (props.sleepOverride()) {
			player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
			props.setSleepOverride(false);
		}
	}
	
	private PlayerSleepTickHandler() {}
}