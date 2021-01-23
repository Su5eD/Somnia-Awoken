package mods.su5ed.somnia.common.util;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;

public enum SomniaState {
	INACTIVE,
	SIMULATING,
	WAITING,
	UNAVAILABLE;

	public static SomniaState forWorld(ServerWorld world) {
		if (!SomniaUtil.isValidSleepTime(world)) return UNAVAILABLE;
		List<ServerPlayerEntity> players = world.getPlayers();

		if (!players.isEmpty()) {
			boolean anySleeping = false, allSleeping = true;
			int somniaSleep = 0, normalSleep = 0;

			for (ServerPlayerEntity player : players) {
				boolean sleeping = player.isSleeping() || Somnia.instance.ignoreList.contains(player.getUniqueID());
				anySleeping |= sleeping;
				allSleeping &= sleeping;

				Optional<IFatigue> props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).resolve();
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
