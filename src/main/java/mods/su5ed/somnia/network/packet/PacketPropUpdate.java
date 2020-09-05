package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.common.util.StreamUtils;
import mods.su5ed.somnia.network.PacketType;
import net.minecraft.network.PacketBuffer;

public class PacketPropUpdate extends PacketBase {
    private final int target;
    private final Object[] fields;

    public PacketPropUpdate(int target, Object... fields) {
        super(PacketType.UPDATE_FATIGUE.ordinal());
        this.target = target;
        this.fields = fields;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeByte(this.target);
        buffer.writeInt(this.fields.length/2);
        for (int i=0; i<this.fields.length; i++)
        {
            buffer.writeByte((Integer) this.fields[i]);
            StreamUtils.writeObject(this.fields[++i], buffer); //TODO: Crashed here because player can' be cast to int. recheck all constructor calls
        }
    }
}
