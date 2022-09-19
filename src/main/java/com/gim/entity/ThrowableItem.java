package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.security.interfaces.ECKey;

public class ThrowableItem extends ThrowableItemProjectile {
    private Elementals elemental;
    private float damage;

    public ThrowableItem(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrowableItem(LivingEntity entity, ItemStack stack, double damageMultiplier, @Nullable Elementals elem) {
        super(Entities.throwable_item, entity, entity.getLevel());

        setItem(stack);

        shootFromRotation(entity, entity.getXRot(), entity.getYHeadRot(), 0.0F, 1.5F, 1.0F);
        elemental = elem;
        damage = (float) (GenshinHeler.safeGetAttribute(entity, Attributes.ATTACK_DAMAGE) * damageMultiplier);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    @Override
    public void handleEntityEvent(byte p_37402_) {
        if (p_37402_ == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; ++i) {
                getLevel().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        elemental = GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental"));
        damage = tag.getFloat("Damage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putString("Elemental", elemental == null ? "" : elemental.name());
        tag.putFloat("Damage", damage);
    }

    @Override
    protected void onHit(HitResult p_37406_) {
        super.onHit(p_37406_);
        if (!getLevel().isClientSide) {
            getLevel().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (damage <= 0)
            return;

        Entity entity = entityHitResult.getEntity();
        DamageSource damageSource = elemental == null
                ? DamageSource.thrown(this, getOwner())
                : elemental.create(getOwner());

        entity.hurt(damageSource, damage);
    }

    private ParticleOptions getParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, getItem());
    }
}
