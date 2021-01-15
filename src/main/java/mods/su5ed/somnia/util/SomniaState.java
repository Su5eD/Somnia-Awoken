package mods.su5ed.somnia.util;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.server.ServerTickHandler;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;

public enum SomniaState {
	IDLE,
	ACTIVE,
	WAITING_PLAYERS,
	EXPIRED,
	NOT_NOW,
	COOLDOWN;

	public static SomniaState getState(ServerTickHandler handler) {
		if (!SomniaUtil.isValidSleepTime((int) (handler.worldServer.getGameTime() % 24000))) return NOT_NOW;
		List<ServerPlayerEntity> players = handler.worldServer.getPlayers();
		if (!players.isEmpty()) {
			boolean anySleeping = false, allSleeping = true;
			int somniaSleep = 0, normalSleep = 0;

			for (ServerPlayerEntity player : players) {
				boolean sleeping = player.isSleeping() || Somnia.instance.ignoreList.contains(player.getUniqueID());
				anySleeping |= sleeping;
				allSleeping &= sleeping;

				Optional<IFatigue> props = player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).resolve();
				if (props.isPresent() && props.get().shouldSleepNormally()) normalSleep++;
				else somniaSleep++;
			}

			if (allSleeping) {
				if (somniaSleep >= normalSleep) return ACTIVE;
			} else if (anySleeping) return WAITING_PLAYERS;
		}

		return IDLE;
	}
}
