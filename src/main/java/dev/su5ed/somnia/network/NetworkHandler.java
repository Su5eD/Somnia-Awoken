package dev.su5ed.somnia.network;

import dev.su5ed.somnia.network.packet.*;
import dev.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.network.packet.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Somnia.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        INSTANCE.messageBuilder(PacketOpenGUI.class, id++)
                .encoder((msg, buf) -> {})
                .decoder(buf -> new PacketOpenGUI())
                .consumer(PacketOpenGUI::handle)
                .add();
        INSTANCE.messageBuilder(PacketWakeUpPlayer.class, id++)
                .encoder((msg, buf) -> {})
                .decoder(buf -> new PacketWakeUpPlayer())
                .consumer(PacketWakeUpPlayer::handle)
                .add();
        INSTANCE.messageBuilder(PacketUpdateSpeed.class, id++)
                .encoder(PacketUpdateSpeed::encode)
                .decoder(PacketUpdateSpeed::new)
                .consumer(PacketUpdateSpeed::handle)
                .add();
        INSTANCE.messageBuilder(PacketResetSpawn.class, id++)
                .encoder(PacketResetSpawn::encode)
                .decoder(PacketResetSpawn::new)
                .consumer(PacketResetSpawn::handle)
                .add();
        INSTANCE.messageBuilder(PacketUpdateFatigue.class, id++)
                .encoder(PacketUpdateFatigue::encode)
                .decoder(PacketUpdateFatigue::new)
                .consumer(PacketUpdateFatigue::handle)
                .add();
        INSTANCE.messageBuilder(PacketActivateBlock.class, id++)
                .encoder(PacketActivateBlock::encode)
                .decoder(PacketActivateBlock::new)
                .consumer(PacketActivateBlock::handle)
                .add();
        INSTANCE.messageBuilder(PacketUpdateWakeTime.class, id++)
                .encoder(PacketUpdateWakeTime::encode)
                .decoder(PacketUpdateWakeTime::new)
                .consumer(PacketUpdateWakeTime::handle)
                .add();
    }

    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToDimension(Object packet, RegistryKey<World> dimension) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dimension), packet);
    }
}
