package com.gim.networking;

import com.gim.GenshinImpactMod;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundLevelParticlesPacketHandler {
    public static void consume(ClientboundLevelParticlesPacket packet, Supplier<NetworkEvent.Context> supplier) {
        if (supplier.get().getDirection().getReceptionSide().isClient()) {
            handleClient(supplier.get(), packet);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(NetworkEvent.Context ctx, ClientboundLevelParticlesPacket packet) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null) {
            GenshinImpactMod.LOGGER.warn("Could not spawn particle effect {} because Minecraft.player is null", packet.getParticle());
        }

        Level level = minecraft.player.level;


        if (packet.getCount() == 0) {
            double d0 = packet.getMaxSpeed() * packet.getXDist();
            double d2 = packet.getMaxSpeed() * packet.getYDist();
            double d4 = packet.getMaxSpeed() * packet.getZDist();

            try {
                minecraft.levelRenderer.addParticle(packet.getParticle(), packet.isOverrideLimiter(), packet.getX(), packet.getY(), packet.getZ(), d0, d2, d4);
            } catch (Throwable throwable1) {
                GenshinImpactMod.LOGGER.warn("Could not spawn particle effect {}", packet.getParticle());
            }
        } else {
            for (int i = 0; i < packet.getCount(); ++i) {
                double d1 = minecraft.player.getRandom().nextGaussian() * (double) packet.getXDist();
                double d3 = minecraft.player.getRandom().nextGaussian() * (double) packet.getYDist();
                double d5 = minecraft.player.getRandom().nextGaussian() * (double) packet.getZDist();
                double d6 = minecraft.player.getRandom().nextGaussian() * (double) packet.getMaxSpeed();
                double d7 = minecraft.player.getRandom().nextGaussian() * (double) packet.getMaxSpeed();
                double d8 = minecraft.player.getRandom().nextGaussian() * (double) packet.getMaxSpeed();

                try {
                    level.addParticle(packet.getParticle(), packet.isOverrideLimiter(), packet.getX() + d1, packet.getY() + d3, packet.getZ() + d5, d6, d7, d8);
                } catch (Throwable throwable) {
                    GenshinImpactMod.LOGGER.warn("Could not spawn particle effect {}", packet.getParticle());
                    return;
                }
            }
        }
    }
}
