package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateSpeed;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.util.State;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static mods.su5ed.somnia.util.State.SIMULATING;

public class ServerTickHandler {
	public static final List<ServerTickHandler> HANDLERS = new ArrayList<>();
	private static int tickHandlers = 0;
	public ServerWorld worldServer;
	public State currentState;
	private int timer = 0;
	private double overflow = 0,
				   multiplier = SomniaConfig.baseMultiplier;
	
	public ServerTickHandler(ServerWorld worldServer) {
		this.worldServer = worldServer;
	}
	
	public void tickEnd() {
		if (++timer == 10) {
			timer = 0;
			State state = State.forWorld(worldServer);
			
			if (state != currentState) {
				if (currentState == SIMULATING) {
					tickHandlers--;
					if (state == State.UNAVAILABLE) closeGuiWithMessage(currentState.toString());
				}
				else if (state == SIMULATING) tickHandlers++;
			}
			
			if (state == SIMULATING || state == State.WAITING) {
				NetworkHandler.sendToDimension(new PacketUpdateSpeed(state == SIMULATING ? multiplier + overflow : 0), worldServer.dimension());
			}

			this.currentState = state;
		}

		if (currentState == SIMULATING) doMultipliedTicking();
	}
	
	private void closeGuiWithMessage(@Nullable String key) {
		worldServer.players().stream()
				.filter(LivingEntity::isSleeping)
				.forEach(player -> {
					NetworkHandler.sendToClient(new PacketWakeUpPlayer(), player);
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

		worldServer.players().stream()
				.map(player -> new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player))
				.forEach(ForgeEventHandler::onPlayerTick);

		worldServer.tick(worldServer.getServer()::haveTime);

		worldServer.getServer().getPlayerList().broadcastAll(new SUpdateTimePacket(worldServer.getGameTime(), worldServer.getDayTime(), worldServer.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), worldServer.dimension());

		BasicEventHooks.onPostWorldTick(worldServer);
	}
}