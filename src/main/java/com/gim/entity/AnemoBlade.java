package com.gim.entity;

import com.gim.attack.GenshinDamageSource;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AnemoBlade extends ThrowableProjectile {
    private double attack;

    public AnemoBlade(LivingEntity entity) {
        this(Entities.anemo_blade, entity.getLevel());
        attack = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);

        Vec3 lookAngle = entity.getLookAngle();
        setPos(entity.getEyePosition().add(lookAngle.scale(1.5)));
        setOwner(entity);

        shoot(lookAngle.x, lookAngle.y, lookAngle.z,
                0.5f, 0);
    }

    public AnemoBlade(EntityType<? extends ThrowableProjectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);

        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround) {
            Vec3 deltaMovement = getDeltaMovement();
            setDeltaMovement(deltaMovement.x, 0, deltaMovement.z);
        }

        if (getLevel().isClientSide()) {
            getLevel().addParticle(ParticleTypes.SWEEP_ATTACK,
                    getX(random.nextDouble()),
                    getY(random.nextDouble() * 2),
                    getZ(random.nextDouble()),
                    getDeltaMovement().x,
                    getDeltaMovement().y,
                    getDeltaMovement().z
            );
        }

        // too old
        if (tickCount >= 20)
            discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        GenshinDamageSource damageSource = Elementals.ANEMO.create(getOwner());
        hitResult.getEntity().hurt(damageSource, (float) attack);
        Vec3 moveVec = getDeltaMovement();

        hitResult.getEntity().push(moveVec.x / 2, moveVec.y * 4, moveVec.z / 2);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("Attack", attack);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attack = tag.getDouble("Attack");
    }
}
