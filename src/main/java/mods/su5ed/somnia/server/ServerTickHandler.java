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

import static mods.su5ed.somnia.common.util.SomniaState.SIMULATING;

public class ServerTickHandler {
	private static int tickHandlers = 0;
	public ServerWorld worldServer;
	public SomniaState currentState;
	public long timer = 0;
	private double overflow = 0;
	private double multiplier = SomniaConfig.baseMultiplier;
	
	public ServerTickHandler(ServerWorld worldServer) {
		this.worldServer = worldServer;
	}
	
	public void tickStart() {
		if (++timer == 10) {
			timer = 0;

			SomniaState prevState = currentState;
			currentState = SomniaState.forWorld(worldServer);
			
			if (prevState != currentState) {
				if (currentState == SIMULATING) {
					tickHandlers++;
				}
				else if (prevState == SIMULATING) {
					tickHandlers--;
					
					if (currentState == SomniaState.UNAVAILABLE) {
						closeGuiWithMessage(currentState.toString());
					}
				}
			}
			
			if (currentState == SIMULATING || currentState == SomniaState.WAITING) {
				NetworkHandler.sendToDimension(new PacketUpdateSpeed(this.currentState == SIMULATING ? multiplier + overflow : 0), worldServer.getDimensionKey());
			}
		}

		if (currentState == SIMULATING) doMultipliedTicking();
	}
	
	private void closeGuiWithMessage(@Nullable String key) {
		worldServer.getPlayers().stream()
				.filter(LivingEntity::isSleeping)
				.forEach(player -> {
					NetworkHandler.sendToClient(new PacketWakeUpPlayer(), player);
					if (player.isSleeping()) player.wakeUp();
					if (key != null) player.sendMessage(new TranslationTextComponent("somnia.status." + key), UUID.randomUUID());
				});
	}

	private void doMultipliedTicking() {
		double target = multiplier + overflow;
		int flooredTarget = (int) target;
		overflow = target - flooredTarget;
		
		long timeMillis = System.currentTimeMillis();

		for (int i = 0; i < flooredTarget; i++) doMultipliedServerTicking();

		multiplier += (System.currentTimeMillis() - timeMillis <= SomniaConfig.delta / tickHandlers) ? 0.1 : -0.1;
		
		if (multiplier > SomniaConfig.multiplierCap) multiplier = SomniaConfig.multiplierCap;
		if (multiplier < SomniaConfig.baseMultiplier) multiplier = SomniaConfig.baseMultiplier;
	}
	
	private void doMultipliedServerTicking() {
		BasicEventHooks.onPreWorldTick(worldServer);

		worldServer.getPlayers().stream()
				.map(player -> new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player))
				.forEach(Somnia.forgeEventHandler::onPlayerTick);

		worldServer.tick(worldServer.getServer()::isAheadOfTime);

		worldServer.getServer().getPlayerList().func_232642_a_(new SUpdateTimePacket(worldServer.getGameTime(), worldServer.getDayTime(), worldServer.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), worldServer.getDimensionKey());

		BasicEventHooks.onPostWorldTick(worldServer);
	}
}