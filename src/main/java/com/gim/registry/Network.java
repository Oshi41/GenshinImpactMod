package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.networking.*;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
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

        playChannel.messageBuilder(ClientboundLevelParticlesPacket.class, 3)
                .encoder(ClientboundLevelParticlesPacket::write)
                .decoder(ClientboundLevelParticlesPacket::new)
                .consumer(ClientboundLevelParticlesPacketHandler::consume)
                .add();

        playChannel.messageBuilder(TextParticleMsg.class, 4)
                .encoder(TextParticleMsg::encode)
                .decoder(TextParticleMsg::decode)
                .consumer(TextParticleMsg::consume)
                .add();

        playChannel.messageBuilder(AnimatePacket.class, 5)
                .encoder(AnimatePacket::encode)
                .decoder(AnimatePacket::decode)
                .consumer(AnimatePacket::consume)
                .add();

        return playChannel;
    }
}
