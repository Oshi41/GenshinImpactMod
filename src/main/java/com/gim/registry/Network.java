package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.networking.CapabilityUpdatePackage;
import com.gim.networking.GenshinAbilityMsg;
import com.gim.networking.SwitchToPlayerMsg;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {

    public static SimpleChannel createChannel() {
        SimpleChannel playChannel = net.minecraftforge.network.NetworkRegistry.ChannelBuilder.
                named(new ResourceLocation(GenshinImpactMod.ModID, "channel"))
                .clientAcceptedVersions(a -> true)
                .serverAcceptedVersions(a -> true)
                .networkProtocolVersion(() -> "1.0")
                .simpleChannel();

        playChannel.messageBuilder(CapabilityUpdatePackage.class, 0)
                .decoder(CapabilityUpdatePackage::decode)
                .encoder(CapabilityUpdatePackage::encode)
                .consumer(CapabilityUpdatePackage::consume)
                .add();

        playChannel.messageBuilder(SwitchToPlayerMsg.class, 1)
                .encoder(SwitchToPlayerMsg::encode)
                .decoder(SwitchToPlayerMsg::decode)
                .consumer(SwitchToPlayerMsg::consume)
                .add();

        playChannel.messageBuilder(GenshinAbilityMsg.class, 2)
                .encoder(GenshinAbilityMsg::encode)
                .decoder(GenshinAbilityMsg::decode)
                .consumer(GenshinAbilityMsg::consume)
                .add();

        return playChannel;
    }
}
