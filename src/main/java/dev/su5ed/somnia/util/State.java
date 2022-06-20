package dev.su5ed.somnia.util;

import dev.su5ed.somnia.core.SomniaCommand;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.IFatigue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;

public enum State {
	INACTIVE,
	SIMULATING,
	WAITING,
	UNAVAILABLE;

	public static State forWorld(ServerWorld world) {
		if (!SomniaUtil.isValidSleepTime(world)) return UNAVAILABLE;
		List<ServerPlayerEntity> players = world.players();

		if (!players.isEmpty()) {
			boolean anySleeping = false, allSleeping = true;
			int somniaSleep = 0, normalSleep = 0;

			for (ServerPlayerEntity player : players) {
				boolean sleeping = player.isSleeping() || SomniaCommand.OVERRIDES.contains(player.getUUID());
				anySleeping |= sleeping;
				allSleeping &= sleeping;

				Optional<IFatigue> props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).resolve();
				if (props.isPresent() && props.get().shouldSleepNormally()) normalSleep++;
				else somniaSleep++;
			}

			if (allSleeping) {
				if (somniaSleep >= normalSleep) return SIMULATING;
			} else if (anySleeping) return WAITING;
		}

		return INACTIVE;
	}
}
