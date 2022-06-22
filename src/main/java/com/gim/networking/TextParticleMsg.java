package com.gim.networking;

import com.gim.events.ShowDamage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TextParticleMsg {
    private final ShowDamage.TextParticle particle;

    public TextParticleMsg(ShowDamage.TextParticle particle) {
        this.particle = particle;
    }

    public void encode(FriendlyByteBuf buf) {
        particle.write(buf);
    }

    public static TextParticleMsg decode(FriendlyByteBuf buf) {
        return new TextParticleMsg(new ShowDamage.TextParticle(buf));
    }

    public boolean consume(Supplier<NetworkEvent.Context> contextSupplier) {

        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            ShowDamage.addTextParticle(particle);
        }

        return true;
    }
}
