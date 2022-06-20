package dev.su5ed.somnia.util;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.IFatigue;
import dev.su5ed.somnia.core.SomniaCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public enum State {
	INACTIVE,
	SIMULATING,
	WAITING,
	UNAVAILABLE;

	public static State forWorld(ServerLevel world) {
		if (!SomniaUtil.isValidSleepTime(world)) return UNAVAILABLE;
		List<ServerPlayer> players = world.players();

		if (!players.isEmpty()) {
			boolean anySleeping = false;
			boolean allSleeping = true;
			int somniaSleep = 0;
			int normalSleep = 0;

			for (ServerPlayer player : players) {
				boolean sleeping = player.isSleeping() || SomniaCommand.OVERRIDES.contains(player.getUUID());
				anySleeping |= sleeping;
				allSleeping &= sleeping;

				boolean shouldSleepNormally = player.getCapability(CapabilityFatigue.INSTANCE).map(IFatigue::shouldSleepNormally).orElse(false);
				if (shouldSleepNormally) normalSleep++;
				else somniaSleep++;
			}

			if (allSleeping) {
				if (somniaSleep >= normalSleep) return SIMULATING;
			} else if (anySleeping) return WAITING;
		}

		return INACTIVE;
	}
}
