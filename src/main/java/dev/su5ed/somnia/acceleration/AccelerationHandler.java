package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.PlayerWakeUpPacket;
import dev.su5ed.somnia.network.client.SpeedUpdatePacket;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Locale;
import java.util.UUID;

public class AccelerationHandler {
    public final ServerLevel level;
    private final double minMultiplier = SomniaConfig.COMMON.minMultiplier.get();
    private final double maxMultiplier = SomniaConfig.COMMON.maxMultiplier.get();
    
    private AccelerationState state;
    private int timer;
    
    private double multiplier = this.minMultiplier;

    public AccelerationHandler(ServerLevel level) {
        this.level = level;
    }

    public boolean isActive() {
        return this.state == AccelerationState.SIMULATING;
    }

    public void tickEnd() {
        if (this.timer++ % 10 == 0) {
            this.state = getNewState();
        }
        
        if (this.state == AccelerationState.SIMULATING) {
            accelerateTicks();
        }
    }

    private AccelerationState getNewState() {
        AccelerationState state = AccelerationState.forLevel(this.level);

        if (this.state == AccelerationState.SIMULATING && state == AccelerationState.UNAVAILABLE) {
            wakeUpPlayers();
        }
        else if (state == AccelerationState.SIMULATING || state == AccelerationState.WAITING) {
            SomniaNetwork.sendToDimension(new SpeedUpdatePacket(state == AccelerationState.SIMULATING ? this.multiplier : 0), this.level.dimension());
        }

        return state;
    }

    private void accelerateTicks() {
        int count = (int) this.multiplier;
        long startMillis = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            tickLevel();
        }
        long deltaMillis = System.currentTimeMillis() - startMillis;
        
        double tickLength = deltaMillis / (double) count;
        double accelerationRatio = SomniaConfig.COMMON.delta.get() / AccelerationManager.getActiveHandlers();
        double available = (accelerationRatio - deltaMillis) / tickLength / 5.0;
        this.multiplier = Mth.clamp(this.multiplier + available, this.minMultiplier, this.maxMultiplier);
    }

    private void tickLevel() {
        MinecraftServer server = this.level.getServer();
        Packet<?> packet = new ClientboundSetTimePacket(this.level.getGameTime(), this.level.getDayTime(), this.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT));
        server.getPlayerList().broadcastAll(packet, this.level.dimension());
        
        ForgeEventFactory.onPreWorldTick(this.level, server::haveTime);

        this.level.players().forEach(ServerPlayer::tick);
        this.level.tick(server::haveTime);

        ForgeEventFactory.onPostWorldTick(this.level, server::haveTime);
    }
    
    private void wakeUpPlayers() {
        this.level.players().stream()
            .filter(LivingEntity::isSleeping)
            .forEach(player -> {
                SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
                String key = "somnia.status." + this.state.toString().toLowerCase(Locale.ROOT);
                player.sendMessage(new TranslatableComponent(key), UUID.randomUUID());
            });
    }
}
