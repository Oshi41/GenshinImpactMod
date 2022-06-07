package com.gim.networking;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CapabilityUpdatePackage {
    private String capability;
    private CompoundTag compoundTag;

    public CapabilityUpdatePackage(Capability capability, INBTSerializable<CompoundTag> instance) {
        this(capability.getName(), instance.serializeNBT());
    }

    public CapabilityUpdatePackage(String capabilityName, CompoundTag tag) {
        this.capability = capabilityName;
        this.compoundTag = tag;
    }

    public static CapabilityUpdatePackage decode(FriendlyByteBuf buf) {
        String capName = buf.readUtf();
        CompoundTag tag = buf.readAnySizeNbt();

        return new CapabilityUpdatePackage(capName, tag);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(capability);
        friendlyByteBuf.writeNbt(compoundTag);
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        if (Objects.equals(context.getDirection().getReceptionSide(), LogicalSide.CLIENT)) {
            return updateClientCap();
        } else {
            return updateCap(context.getSender());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean updateClientCap() {
        return updateCap(net.minecraft.client.Minecraft.getInstance().player);
    }

    private boolean updateCap(LivingEntity entity) {
        if (capability == null || compoundTag == null || compoundTag.isEmpty()) {
            return false;
        }

        ICapabilityProvider provider = GenshinHeler.from(entity, capability);
        if (!(provider instanceof INBTSerializable)) {
            return false;
        }

        try {
            INBTSerializable serializable = (INBTSerializable) provider;
            serializable.deserializeNBT(compoundTag);
            return true;
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.debug(String.format("Error during %s.updateCap", getClass()));
            GenshinImpactMod.LOGGER.debug(e);
            return false;
        }
    }
}
