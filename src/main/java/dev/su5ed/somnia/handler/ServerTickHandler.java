package dev.su5ed.somnia.handler;

import dev.su5ed.somnia.core.SomniaConfig;
import dev.su5ed.somnia.network.PlayerWakeUpPacket;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.SpeedUpdatePacket;
import dev.su5ed.somnia.util.State;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.su5ed.somnia.util.State.SIMULATING;

public class ServerTickHandler {
    public static final List<ServerTickHandler> HANDLERS = new ArrayList<>();
    private static int tickHandlers = 0;
    public ServerLevel serverLevel;
    public State currentState;
    private int timer = 0;
    private double overflow = 0;
    private double multiplier = SomniaConfig.COMMON.baseMultiplier.get(); // TODO

    public ServerTickHandler(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public void tickEnd() {
        if (++timer == 10) {
            timer = 0;
            State state = State.forWorld(serverLevel);

            if (state != currentState) {
                if (currentState == SIMULATING) {
                    tickHandlers--;
                    if (state == State.UNAVAILABLE) closeGuiWithMessage(currentState.toString());
                }
				else if (state == SIMULATING) tickHandlers++;
            }

            if (state == SIMULATING || state == State.WAITING) {
                SomniaNetwork.sendToDimension(new SpeedUpdatePacket(state == SIMULATING ? multiplier + overflow : 0), serverLevel.dimension());
            }

            this.currentState = state;
        }

        if (currentState == SIMULATING) doMultipliedTicking();
    }

    private void closeGuiWithMessage(@Nullable String key) {
        serverLevel.players().stream()
            .filter(LivingEntity::isSleeping)
            .forEach(player -> {
                SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
                if (key != null) player.sendMessage(new TranslatableComponent("somnia.status." + key), UUID.randomUUID());
            });
    }

    private void doMultipliedTicking() {
        double target = multiplier + overflow;
        int flooredTarget = (int) target;
        overflow = target - flooredTarget;

        long timeMillis = System.currentTimeMillis();

        for (int i = 0; i < flooredTarget; i++) doMultipliedServerTicking();

        multiplier += (System.currentTimeMillis() - timeMillis <= SomniaConfig.COMMON.delta.get() / tickHandlers) ? 0.1 : -0.1;

        double multiplierCap = SomniaConfig.COMMON.multiplierCap.get();
        double baseMultiplier = SomniaConfig.COMMON.baseMultiplier.get();
        if (multiplier > multiplierCap) multiplier = multiplierCap;
        if (multiplier < baseMultiplier) multiplier = baseMultiplier;
    }

    private void doMultipliedServerTicking() {
        ForgeEventFactory.onPreWorldTick(serverLevel, () -> false);

        serverLevel.players().stream()
            .map(player -> new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player))
            .forEach(ForgeEventHandler::onPlayerTick);

        serverLevel.tick(serverLevel.getServer()::haveTime);

        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverLevel.dimension());

        ForgeEventFactory.onPostWorldTick(serverLevel, () -> false);
    }
}