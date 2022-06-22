package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.Somnia;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class AccelerationManager {
    public static final List<AccelerationHandler> HANDLERS = new ArrayList<>();
    
    public static int getActiveHandlers() {
        return (int) HANDLERS.stream().filter(AccelerationHandler::isActive).count();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            Somnia.LOGGER.info("Registering acceleration handler for loading world");
            HANDLERS.add(new AccelerationHandler(serverLevel));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            HANDLERS.stream()
                .filter(handler -> handler.level == serverLevel)
                .findFirst()
                .ifPresent(handler -> {
                    Somnia.LOGGER.info("Removing acceleration handler for unloading world!");
                    HANDLERS.remove(handler);
                });
        }
    }
    
    @SubscribeEvent
    public static void onTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HANDLERS.forEach(AccelerationHandler::tickEnd);
        }
    }
}
