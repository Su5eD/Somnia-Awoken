package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.network.PacketType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;

public class PacketRideEntity extends PacketBase {
    private final Entity target;

    public PacketRideEntity(Entity target) {
        super(PacketType.RIDE_ENTITY.ordinal());
        this.target = target;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(this.target.getEntityId());
    }
}
