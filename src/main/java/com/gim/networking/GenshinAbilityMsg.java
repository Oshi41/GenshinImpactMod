package com.gim.networking;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class GenshinAbilityMsg {
    private Abilities type;

    public GenshinAbilityMsg(Abilities type) {
        this.type = type;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(type.name());
    }

    public static GenshinAbilityMsg decode(FriendlyByteBuf buf) {
        return new GenshinAbilityMsg(GenshinHeler.safeGet(Abilities.class, buf.readUtf()));
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        if (Objects.equals(context.getDirection().getReceptionSide(), LogicalSide.SERVER)) {
            if (context.getSender() != null) {
                if (onUse(context.getSender(), type)) {
                    GenshinImpactMod.CHANNEL.sendTo(this, context.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                    return true;
                }
            }
        } else {
            return handleClient(type);
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean handleClient(Abilities type) {
        return onUse(net.minecraft.client.Minecraft.getInstance().player, type);
    }

    public static boolean onUse(LivingEntity entity, Abilities type) {
        if (entity == null)
            return false;

        IGenshinInfo genshinInfo = entity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (genshinInfo == null)
            return false;

        switch (type) {
            case SKILL:
                genshinInfo.onSkill(entity);
                return true;

            case BURST:
                IEnergyStorage energyStorage = genshinInfo.getPersonInfo(genshinInfo.current()).energy();
                energyStorage.extractEnergy(energyStorage.getEnergyStored(), false);
                genshinInfo.onBurst(entity);
                return true;

            default:
                return false;
        }
    }

    public enum Abilities {
        SKILL,
        BURST
    }
}
