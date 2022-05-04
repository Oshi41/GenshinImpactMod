package com.gim.capability;

import com.gim.registry.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShieldProvider implements ICapabilitySerializable<CompoundTag> {
    final LazyOptional<IShield> instance;

    public ShieldProvider(Entity entity) {
        instance = LazyOptional.of(ShieldCapability::new);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.SHIELDS.orEmpty(cap, instance.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(NullPointerException::new).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
    }
}
