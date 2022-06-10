package com.gim.client.entity.players.anemo_traveler;

import com.gim.entity.Tornado;
import com.gim.registry.Elementals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class TornadoRenderer<T extends Tornado> extends EntityRenderer<T> {
    private final ResourceLocation WHITE = new ResourceLocation("forge", "textures/white.png");

    public TornadoRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(T entity, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
        super.render(entity, p_114486_, p_114487_, p_114488_, p_114489_, p_114490_);

        AABB boundingBox = entity.getBoundingBox();

        double sin1 = Math.sin(entity.tickCount / Math.PI);
        double cos1 = Math.cos(entity.tickCount / Math.PI);

        // with random
        Vec3 circleCenter = boundingBox.getCenter().add(cos1, boundingBox.getYsize() / 2d, sin1);
        double radius = boundingBox.getXsize() / 2d;
        ParticleOptions particleOptions = from(entity.getElement());
        double step = step(entity.getElement());

        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        for (double y = boundingBox.maxY; y > boundingBox.minY; y -= 0.5) {
            double yStep = (boundingBox.maxY - y) / boundingBox.getYsize();

            // iterating through circle
            for (double j = 0; j < Math.PI * 2; j += step) {
                double cos = Math.cos(j + yStep) * radius;
                double sin = Math.sin(j + yStep) * radius;

                double x = cos + circleCenter.x;
                double z = sin + circleCenter.z;


                if (particleOptions != null) {
                    entity.getLevel().addParticle(particleOptions, x + Math.cos(yStep * Math.PI * 2), y, z + Math.sin(yStep * Math.PI * 2),
                            0, 0, 0);
                }

                // skeleton for tornado
                if (j == 0) {
                    entity.getLevel().addParticle(ParticleTypes.SWEEP_ATTACK, x + Math.cos(yStep * Math.PI * 2), y, z + Math.sin(yStep * Math.PI * 2),
                            0, 0, 0);
                }
            }

            // every next circe is 90% of original
            radius *= 0.9;
        }
    }

    private ParticleOptions from(Elementals elementals) {
        ParticleOptions type = switch (elementals) {
            case PYRO -> ParticleTypes.LAVA;
            case HYDRO -> ParticleTypes.BUBBLE;
            case CRYO -> ParticleTypes.ITEM_SNOWBALL;
            case ELECTRO -> ParticleTypes.ELECTRIC_SPARK;
            case DENDRO -> new DustParticleOptions(new Vector3f(Vec3.fromRGB24(Color.green.getRGB())), 1);
            case ANEMO -> new DustParticleOptions(new Vector3f(Vec3.fromRGB24(Color.white.getRGB())), 1);

            default -> null;
        };

        return type;
    }

    private double step(Elementals elementals) {
        return switch (elementals) {
            case HYDRO -> 0.2;
            case ELECTRO -> 0.3;
            case CRYO -> 0.8;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return WHITE;
    }
}
