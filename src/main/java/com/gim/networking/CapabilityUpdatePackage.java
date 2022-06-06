package com.gim.networking;

import com.gim.GenshinImpactMod;
import com.gim.registry.Capabilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class CapabilityUpdatePackage {

    private Capability capability;
    private CompoundTag compoundTag;

    public CapabilityUpdatePackage(Capability capability, INBTSerializable<CompoundTag> instance) {
        this(capability, instance.serializeNBT());
    }

    public CapabilityUpdatePackage(Capability cap, CompoundTag tag) {
        this.capability = cap;
        this.compoundTag = tag;
    }

    public static CapabilityUpdatePackage decode(FriendlyByteBuf buf) {
        Capability cap = fromString(buf.readUtf());
        CompoundTag tag = buf.readAnySizeNbt();

        return new CapabilityUpdatePackage(cap, tag);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(capability.getName());
        friendlyByteBuf.writeNbt(compoundTag);
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        if (Objects.equals(context.getDirection().getReceptionSide() , LogicalSide.CLIENT)) {
            updateClientCap();
        } else {
            updateCap(context.getSender());
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void updateClientCap() {
        updateCap(net.minecraft.client.Minecraft.getInstance().player);
    }

    private void updateCap(LivingEntity entity) {
        if (capability != null && compoundTag != null && !compoundTag.isEmpty()) {
            entity.getCapability(capability).ifPresent(instance -> {
                try {
                    if (instance instanceof INBTSerializable) {
                        ((INBTSerializable<CompoundTag>) instance).deserializeNBT(compoundTag);
                    }
                } catch (Exception e) {
                    GenshinImpactMod.LOGGER.debug(e);
                }
            });
        }
    }

    @Nullable
    private static Capability fromString(String capName) {
        if (Objects.equals(capName, Capabilities.SHIELDS.getName())) {
            return Capabilities.SHIELDS;
        }

        return null;
    }
}
