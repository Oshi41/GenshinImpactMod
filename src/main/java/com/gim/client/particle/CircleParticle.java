package com.gim.client.particle;

import com.gim.registry.ParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class CircleParticle extends TextureSheetParticle {
    protected CircleParticle(ClientLevel p_10832level_, double x, double y, double z) {
        super(p_10832level_, x, y, z);
        hasPhysics = false;
        gravity = 0;
        lifetime = 10;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Provider(SpriteSet p_105793_) {
            this.sprite = p_105793_;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double p_107426_, double p_107427_, double p_107428_) {
            TextureSheetParticle particle = new CircleParticle(level, x, y, z);

            if (Objects.equals(type, ParticleTypes.DEFENCE_DEBUFF)) {
                particle.scale(2);
            }

            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
