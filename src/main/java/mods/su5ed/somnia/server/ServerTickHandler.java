package mods.su5ed.somnia.server;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.common.util.SomniaState;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketPropUpdate;
import mods.su5ed.somnia.network.packet.PacketWakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;

import static mods.su5ed.somnia.common.util.SomniaState.ACTIVE;

public class ServerTickHandler
{
	public static final String TRANSLATION_FORMAT = "somnia.status.%s";
			
	private static int activeTickHandlers = 0;
	
	public ServerWorld worldServer;
	public SomniaState currentState;
	
	public long 	lastSleepStart,
					currentSleepPeriod; 			// Incremented while mbCheck is true, reset when state is changed
	public long		checkTimer 			= 0, 		// Used to schedule GUI update packets and sleep state checks
					lastTpsMillis		= 0,
					liTps 				= 0, 		// Counts ticks
					tps					= 0;		// Set per second to liTPS, used to work out actual multiplier to send to clients
	
	private double 	multiplier 			= SomniaConfig.baseMultiplier;
	
	public ServerTickHandler(ServerWorld worldServer)
	{
		this.worldServer = worldServer;
	}
	
	public void tickStart()
	{
		if (++checkTimer == 10)
		{
			checkTimer = 0;
			
			SomniaState prevState = currentState;
			currentState = SomniaState.getState(this);
			
			if (prevState != currentState)
			{
				currentSleepPeriod = 0;
				if (currentState == ACTIVE) // acceleration started
				{
					lastSleepStart = worldServer.getGameTime();
					activeTickHandlers++;
				}
				else if (prevState == ACTIVE) // acceleration stopped
				{
					activeTickHandlers--;
					
					if (currentState == SomniaState.EXPIRED || currentState == SomniaState.NOT_NOW)
						closeGuiWithMessage(currentState.toString());
				}
			}
			
			if (currentState == ACTIVE || currentState == SomniaState.WAITING_PLAYERS || currentState == SomniaState.COOLDOWN)
			{
				Pair<PacketBuffer, Integer> packet = new PacketPropUpdate(
						0x00,
						0x00, currentState == ACTIVE ? (double)tps/20d : .0d,
						0x01, currentState == ACTIVE ? Somnia.timeStringForWorldTime(worldServer.getDayTime()) : "f:"+currentState.toString()
				).buildPacket();

				NetworkHandler.sendToDimension(packet.getLeft(), packet.getRight(), worldServer);
			}
		}
		
		if (currentState == ACTIVE)
			doMultipliedTicking();
	}
	
	private void closeGuiWithMessage(@Nullable String key)
	{
		Pair<PacketBuffer, Integer> packet = new PacketWakePlayer().buildPacket();

		Iterator<ServerPlayerEntity> iter = worldServer.getPlayers().iterator();
		ServerPlayerEntity player;
		while (iter.hasNext())
		{
			player = iter.next();
			if (player.isSleeping())
			{
				NetworkHandler.sendToClient(packet.getLeft(), packet.getRight(), player);
				if (player.isSleeping()) // this if might stop random teleporting when players have already woken
				{
					player.wakeUp(); // Stop clients ignoring GUI close packets (major hax)
				}
				if (key != null) player.sendMessage(new TranslationTextComponent(String.format(TRANSLATION_FORMAT, key)), UUID.randomUUID());
			}
		}
	}

	private void incrementCounters()
	{
		liTps++;
		currentSleepPeriod++;
	}
	
	private double overflow = .0d;
	private void doMultipliedTicking()
	{
		/*
		 * We can't run 0.5 of a tick,
		 * so we floor the multiplier and store the difference as overflow to be ran on the next tick
		 */
		double target = multiplier + overflow;
		int liTarget = (int) Math.floor(target);
		overflow = target - liTarget;
		
		long delta = System.currentTimeMillis();
		for (int i=0; i<liTarget; i++)
			doMultipliedServerTicking();
		delta = System.currentTimeMillis() - delta;

		MinecraftServer server = worldServer.getServer();
		if (server == null) return;
		server.getPlayerList().func_232642_a_(new SUpdateTimePacket(worldServer.getGameTime(), worldServer.getDayTime(), worldServer.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), worldServer.func_234923_W_());
		
		if (delta > (SomniaConfig.delta/activeTickHandlers))
			multiplier -= .1d;
		else
			multiplier += .1d;
		
		if (multiplier > SomniaConfig.multiplierCap)
			multiplier = SomniaConfig.multiplierCap;
		
		if (multiplier < SomniaConfig.baseMultiplier)
			multiplier = SomniaConfig.baseMultiplier;
		
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis-lastTpsMillis > 1000)
		{
			tps = liTps;
			liTps = 0;
			lastTpsMillis = currentTimeMillis;
		}
	}
	
	private void doMultipliedServerTicking()
	{
		BasicEventHooks.onPreWorldTick(worldServer);
		worldServer.tick(worldServer.getServer()::isAheadOfTime);
		BasicEventHooks.onPostWorldTick(worldServer);
		
		/*
		 * Work around for making sure fatigue is updated with every tick (including Somnia ticks)
		 */
		for (PlayerEntity player : worldServer.getPlayers())
			Somnia.forgeEventHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
		
		incrementCounters();
	}
}