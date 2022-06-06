package com.gim.attack;

import com.google.common.collect.Maps;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GenshinAreaSpreading {
    private final Level level;
    private final Entity entity;
    private final Vec3 center;
    private final DamageSource source;
    private final float radius;

    public GenshinAreaSpreading(Entity entity, Vec3 center, DamageSource source, float radius) {
        this.entity = entity;
        this.level = entity.getLevel();
        this.center = center;
        this.source = source;
        this.radius = radius;
    }

    /**
     * Creating explosion
     * Not damaging blocks
     * Returns hitted entities
     *
     * @return
     */
    public Map<Entity, Float> explode() {
        float f2 = this.radius * 2.0F;
        int k1 = Mth.floor(this.center.x - (double) f2 - 1.0D);
        int l1 = Mth.floor(this.center.x + (double) f2 + 1.0D);
        int i2 = Mth.floor(this.center.y - (double) f2 - 1.0D);
        int i1 = Mth.floor(this.center.y + (double) f2 + 1.0D);
        int j2 = Mth.floor(this.center.z - (double) f2 - 1.0D);
        int j1 = Mth.floor(this.center.z + (double) f2 + 1.0D);

        List<Entity> list = this.level.getEntities(null, new AABB(k1, i2, j2, l1, i1, j1));
        HashMap<Entity, Float> result = Maps.newHashMap();

        for (int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);
            if (!entity.ignoreExplosion() && !Objects.equals(entity, this.entity)) {
                double d12 = Math.sqrt(entity.distanceToSqr(this.center)) / (double) f2;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.center.x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.center.y;
                    double d9 = entity.getZ() - this.center.z;
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        double d14 = Explosion.getSeenPercent(this.center, entity);
                        double d10 = (1.0D - d12) * d14;
                        float damage = ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f2 + 1.0D));
                        entity.hurt(this.source, damage);
                        result.put(entity, damage);
                    }
                }
            }
        }

        if (this.level instanceof ServerLevel) {
            ((ServerLevel) this.level).sendParticles(
                    radius > 2 ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION,
                    this.center.x,
                    this.center.y,
                    this.center.z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }


        return result;
    }
}
