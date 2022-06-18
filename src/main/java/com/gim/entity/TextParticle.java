package com.gim.entity;

import com.gim.registry.Entities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;

public class TextParticle extends Entity {

    public static int MAX_AGE = 20 * 2;

    // maxExp count for stats for same entity is 4 (3 for damage stats and one for reaction)
    public static int MAX_COUNT_OF_STATS = 4;
    private Vec3 offset;
    private Entity owner;

    public TextParticle(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);

        offset = new Vec3(random.nextGaussian() * 0.008, 0.03 + Math.abs(random.nextGaussian()) * 0.008, random.nextGaussian() * 0.008);
    }

    public TextParticle(Entity source, Component text) {
        this(Entities.text_particle_entity_type, source.getLevel());
        owner = source;

        setPos(new Vec3(owner.getX(), owner.getEyeY(), owner.getZ()).add(offset));
        setDeltaMovement(offset);

        setCustomName(text);
        setCustomNameVisible(true);

        // finding entities in range
        AABB aabb = new AABB(position().add(5, 5, 5), position().add(-5, -5, -5));
        List<? extends TextParticle> particles = source.getLevel().getEntitiesOfClass(getClass(), aabb);
        if (particles.size() > MAX_COUNT_OF_STATS) {
            // sort entities by it's live age
            particles.stream().sorted(Comparator.comparingInt(value -> value.tickCount))
                    // skipping the youngest ones
                    .skip(MAX_COUNT_OF_STATS)
                    // removeing the old ones
                    .forEach(Entity::discard);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (owner != null) {
            move(MoverType.SELF, getDeltaMovement().scale(0.99));
        }

        if (tickCount > MAX_AGE) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
