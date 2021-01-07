package mods.su5ed.somnia.common;

import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class PlayerSleepTickHandler
{
	/*
	 * A sided state for caching player data 
	 */
	public static class State
	{
		public boolean sleepOverride = false;
	}
	
	public static State clientState = new State(), serverState = new State();
	
	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		State state = event.side == LogicalSide.CLIENT ? clientState : serverState;
		if (event.phase == TickEvent.Phase.START)
			tickStart(state, event.player);
		else
			tickEnd(state, event.player);
	}

	//private static final ResourceLocation CHARM_SLEEP = new ResourceLocation("darkutils", "charm_sleep");
	public void tickStart(State state, PlayerEntity player)
	{
		if (player.isSleeping())
		{
			//BlockPos pos = player.getBedLocation(player.dimension);

			//Reset fatigue in case you pick the charm up while sleeping. Doesn't trigger otherwise, because Somnia keeps the sleep timer below 100
			/*if (player.getSleepTimer() > 99 && Loader.isModLoaded("darkutils") && InvUtil.hasItem(player, CHARM_SLEEP)) {
				player.sleepTimer = 98;
			}*/ //TODO: DarkUtils compat

			/*if (!CompatModule.isBed(player, pos)) {
				state.sleepOverride = false;
				return;
			}*/

			state.sleepOverride = true;
			//player.stopSleepInBed(true, true);
			
			if (SomniaConfig.fading)
			{
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99)
					sleepTimer = 98;
				player.sleepTimer = sleepTimer;
			}
		}
	}

	public void tickEnd(State state, PlayerEntity player)
	{
		if (state.sleepOverride)
		{
			player.startSleeping(player.getBedPosition().orElse(player.getPosition()));
			state.sleepOverride = false;
		}
	}
}