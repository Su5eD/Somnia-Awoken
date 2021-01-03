package mods.su5ed.somnia.common.util;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.server.ServerTickHandler;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;

public enum SomniaState
{
	IDLE,
	ACTIVE,
	WAITING_PLAYERS,
	EXPIRED,
	NOT_NOW,
	COOLDOWN;
	
	public static SomniaState getState(ServerTickHandler handler)
	{
		long totalWorldTime = handler.worldServer.getGameTime();
		
		if (!Somnia.validSleepPeriod.isTimeWithin(totalWorldTime % 24000))
			return NOT_NOW;

		List<ServerPlayerEntity> players = handler.worldServer.getPlayers();
		if (players.isEmpty())
			return IDLE;
		
		boolean sleeping, anySleeping = false, allSleeping = true;

		for (ServerPlayerEntity player : players) {
			sleeping = player.isSleeping() || ListUtils.containsRef(player, Somnia.instance.ignoreList);
			anySleeping |= sleeping;
			allSleeping &= sleeping;
		}
		
		if (allSleeping)
			return ACTIVE;
		else if (anySleeping)
			return WAITING_PLAYERS;
		else
			return IDLE;
	}
}
