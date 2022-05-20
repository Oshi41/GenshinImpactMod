package com.gim.capability.genshin;

import com.gim.registry.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class GenshinProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<IGenshinInfo> instance;
    private final WeakReference<LivingEntity> reference;

    public GenshinProvider(LivingEntity entity) {
        reference = new WeakReference<>(entity);
        instance = LazyOptional.of(() -> new GenshinInfo(reference.get()));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.GENSHIN_INFO.orEmpty(cap, instance.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(NullPointerException::new).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        LivingEntity living = reference.get();
        if (living != null) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt, living);
        } else {
            throw new IllegalArgumentException("Capability is not attached to entity");
        }
    }
}
