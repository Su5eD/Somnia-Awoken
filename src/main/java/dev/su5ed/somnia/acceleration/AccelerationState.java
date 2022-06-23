package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.IFatigue;
import dev.su5ed.somnia.SomniaCommand;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public enum AccelerationState {
	UNAVAILABLE(false),
	INACTIVE(false),
	WAITING(true),
	SIMULATING(true);
	
	private final boolean enableOverlay;

	AccelerationState(boolean enableOverlay) {
		this.enableOverlay = enableOverlay;
	}
	
	public boolean enableOverlay() {
		return this.enableOverlay;
	}

	public static AccelerationState forLevel(ServerLevel level) {
		if (!SomniaUtil.isValidSleepTime(level)) return UNAVAILABLE;
		
		List<ServerPlayer> players = level.players();
		if (!players.isEmpty()) {
			boolean anySleeping = false;
			boolean allSleeping = true;
			int acceleratedSleep = 0;
			int normalSleep = 0;

			for (ServerPlayer player : players) {
				boolean sleeping = player.isSleeping() || SomniaCommand.OVERRIDES.contains(player.getUUID());
				anySleeping |= sleeping;
				allSleeping &= sleeping;

				boolean shouldSleepNormally = player.getCapability(CapabilityFatigue.INSTANCE)
					.map(IFatigue::shouldSleepNormally)
					.orElse(false);
				if (shouldSleepNormally) normalSleep++;
				else acceleratedSleep++;
			}

			if (allSleeping) {
				if (acceleratedSleep >= normalSleep) {
					return SIMULATING;
				}
			}
			else if (anySleeping) {
				return WAITING;
			}
		}

		return INACTIVE;
	}
}
