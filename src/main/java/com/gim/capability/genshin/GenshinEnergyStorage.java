package com.gim.capability.genshin;

import com.gim.GenshinHeler;
import com.gim.registry.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.energy.EnergyStorage;

public class GenshinEnergyStorage extends EnergyStorage {
    public GenshinEnergyStorage(AttributeMap map, int energy) {
        super((int) GenshinHeler.safeGetAttribute(map, Attributes.burst_cost), Integer.MAX_VALUE, Integer.MAX_VALUE, energy);
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("Energy", this.energy);
        tag.putInt("Capacity", this.capacity);
        tag.putInt("MaxReceive", this.maxReceive);
        tag.putInt("MaxExtract", this.maxExtract);

        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            this.energy = ((CompoundTag) nbt).getInt("Energy");
            this.capacity = ((CompoundTag) nbt).getInt("Capacity");
            this.maxReceive = ((CompoundTag) nbt).getInt("MaxReceive");
            this.maxExtract = ((CompoundTag) nbt).getInt("MaxExtract");
        } else {
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        }
    }
}
