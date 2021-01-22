package mods.su5ed.somnia.server;

import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.common.network.NetworkHandler;
import mods.su5ed.somnia.common.network.packet.PacketUpdateSpeed;
import mods.su5ed.somnia.common.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.common.util.SomniaState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import javax.annotation.Nullable;
import java.util.UUID;

import static mods.su5ed.somnia.common.util.SomniaState.ACTIVE;

public class ServerTickHandler {
	public static final String TRANSLATION_FORMAT = "somnia.status.%s";
			
	private static int activeTickHandlers = 0;
	
	public ServerWorld worldServer;
	public SomniaState currentState;
	
	public long lastSleepStart,
				currentSleepPeriod; // Incremented while mbCheck is true, reset when state is changed
	public long	checkTimer = 0, 	// Used to schedule GUI update packets and sleep state checks
				lastTpsMillis = 0,
				liTps = 0, 			// Counts ticks
				tps	= 0;			// Set per second to liTPS, used to work out actual multiplier to send to clients
	
	private double multiplier = SomniaConfig.baseMultiplier;
	
	public ServerTickHandler(ServerWorld worldServer) {
		this.worldServer = worldServer;
	}
	
	public void tickStart() {
		if (++checkTimer == 10) {
			checkTimer = 0;

			SomniaState prevState = currentState;
			currentState = SomniaState.getState(this);
			
			if (prevState != currentState) {
				currentSleepPeriod = 0;
				if (currentState == ACTIVE) { // acceleration started
					lastSleepStart = worldServer.getGameTime();
					activeTickHandlers++;
				}
				else if (prevState == ACTIVE) { // acceleration stopped
					activeTickHandlers--;
					
					if (currentState == SomniaState.NOT_NOW) {
						closeGuiWithMessage(currentState.toString());
					}
				}
			}
			
			if (currentState == ACTIVE || currentState == SomniaState.WAITING_PLAYERS) {
				NetworkHandler.sendToDimension(new PacketUpdateSpeed(this.currentState == ACTIVE ? (double)tps/20D : 0), worldServer.getDimensionKey());
			}
		}

		if (currentState == ACTIVE) {
			long time = worldServer.getGameTime();
			doMultipliedTicking();
			System.out.println("Time diff: " + (worldServer.getGameTime() - time));
			System.out.println("Multiplier: " + (multiplier + overflow));
		}
	}
	
	private void closeGuiWithMessage(@Nullable String key) {
		worldServer.getPlayers().stream()
				.filter(LivingEntity::isSleeping)
				.forEach(player -> {
					NetworkHandler.sendToClient(new PacketWakeUpPlayer(), player);
					if (player.isSleeping()) player.wakeUp();
					if (key != null) player.sendMessage(new TranslationTextComponent(String.format(TRANSLATION_FORMAT, key)), UUID.randomUUID());
				});
	}
	
	private double overflow = 0;

	private void doMultipliedTicking() {
		double target = multiplier + overflow;
		double flooredTarget = Math.floor(target);
		overflow = target - flooredTarget;
		
		long delta = System.currentTimeMillis();
		for (int i = 0; i < flooredTarget; i++) doMultipliedServerTicking();
		delta = System.currentTimeMillis() - delta;

		if (delta > SomniaConfig.delta / activeTickHandlers) multiplier -= 0.1;
		else multiplier += 0.1;
		
		if (multiplier > SomniaConfig.multiplierCap) multiplier = SomniaConfig.multiplierCap;
		if (multiplier < SomniaConfig.baseMultiplier) multiplier = SomniaConfig.baseMultiplier;
		
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastTpsMillis > 1000) {
			tps = liTps;
			liTps = 0;
			lastTpsMillis = currentTimeMillis;
		}
	}
	
	private void doMultipliedServerTicking() {
		BasicEventHooks.onPreWorldTick(worldServer);
		worldServer.tick(worldServer.getServer()::isAheadOfTime);
		BasicEventHooks.onPostWorldTick(worldServer);

		worldServer.getPlayers().stream()
			.map(player -> new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player))
			.forEach(Somnia.forgeEventHandler::onPlayerTick);

		worldServer.getServer().getPlayerList().func_232642_a_(new SUpdateTimePacket(worldServer.getGameTime(), worldServer.getDayTime(), worldServer.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), worldServer.getDimensionKey());

		liTps++;
		currentSleepPeriod++;
	}
}