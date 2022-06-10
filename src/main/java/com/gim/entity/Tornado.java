package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Objects;

public class Tornado extends Projectile {
    private static final List<Elementals> SPREADING = ImmutableList.of(Elementals.HYDRO, Elementals.PYRO, Elementals.ELECTRO, Elementals.CRYO, Elementals.DENDRO);
    private static final EntityDataAccessor<String> ELEMENT = SynchedEntityData.defineId(Tornado.class, EntityDataSerializers.STRING);
    private int liveTime = Integer.MAX_VALUE;
    private Vec3 moveVec;

    public Tornado(EntityType<? extends Projectile> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    public Tornado(Entity owner, float yHeadRot, int maxLiveTime, Elementals source) {
        this(Entities.tornado_entity_type, owner.getLevel());
        liveTime = maxLiveTime;
        withElement(source);
        setOwner(owner);

        // player is above 2 blocks height
        Vec3 pos = owner.position().add(0, -2, 0);

        // looking forward to horizon
        Vec3 lookAngle = Vec3.directionFromRotation(new Vec2(7, yHeadRot));
        // spawn tornado a bit further than owner
        pos = pos.add(lookAngle.scale(2));
        setPos(pos);

        // launching tornado
        shoot(lookAngle.x, lookAngle.y, lookAngle.z, 0.2f, 0);
        moveVec = getDeltaMovement();
    }

    @Override
    public void tick() {
        super.tick();

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        // too old
        if (this.tickCount >= liveTime) {
            discard();
        }

        // middle of tornado
        Vec3 currentPosition = getEyePosition();

        if (!getLevel().isClientSide()) {
            // Sucking entities to tornado (except owner)
            for (Entity entity : getLevel().getEntities(getOwner(), getWorkingArea())) {
                // except tornado entity
                if (entity instanceof Tornado || Objects.equals(getOwner(), this)) {
                    continue;
                }

                Vec3 entityPosition = entity.position();
                double distance = currentPosition.distanceTo(entityPosition);
                Vec3 speed = currentPosition.subtract(entityPosition).normalize().scale(distance / 20);
                entity.push(speed.x, speed.y, speed.z);
            }
        }

        if (!isNoGravity()) {
            push(0, -ForgeMod.ENTITY_GRAVITY.get().getDefaultValue(), 0);
            hasImpulse = false;
        }

        if (!this.level.noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
        }

        this.move(MoverType.SELF, getDeltaMovement());

        if (moveVec != null) {
            setDeltaMovement(moveVec);
        }

        if (Elementals.ANEMO.equals(getElement())) {
            if (isInWater()) {
                withElement(Elementals.HYDRO);
            } else if (isInLava() || isOnFire()) {
                withElement(Elementals.PYRO);
            } else if (isInPowderSnow) {
                withElement(Elementals.CRYO);
            }
        }
    }

    @Override
    public int getMaxFallDistance() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public void setRemainingFireTicks(int ticks) {

        if (ticks > 0) {
            if (Elementals.ANEMO.equals(getElement())) {
                withElement(Elementals.PYRO);
            }

            // no flame
            ticks = 0;
        }

        super.setRemainingFireTicks(ticks);
    }

    @Override
    public boolean isPushable() {
        return isAlive();
    }

    @Override
    public void push(Entity entity) {
        if (Objects.equals(entity, getOwner()) || entity instanceof Tornado) {
            return;
        }

        Elementals element = getElement();
        // no element or no owner
        if (element == null) {
            return;
        }

        // trying to perform elemental reactions
        if (Objects.equals(element, Elementals.ANEMO) && entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            for (Elementals e : SPREADING) {
                // perform elemental infusion
                if (e.is(living)) {
                    withElement(e);
                    element = e;
                    break;
                }
            }
        }

        double skill = GenshinHeler.safeGetAttribute(getOwner(), Attributes.skill_level) + 1;
        double level = GenshinHeler.safeGetAttribute(getOwner(), Attributes.level) + 1;

        float damage = (float) (skill * level);

        // +30.7% percent of damage
        if (element != Elementals.ANEMO) {
            damage = damage * 1.307f;
        }

        DamageSource damageSource = element.create(getOwner() == null ? this : getOwner());
        entity.hurt(damageSource, damage);
    }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) {
        super.push(p_20286_, p_20287_, p_20288_);
        hasImpulse = false;
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(ELEMENT, Elementals.ANEMO.name());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        liveTime = tag.getInt("MaxAge");
        withElement(GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental")));

        if (getLevel().isClientSide() && tag.contains("OwnerID")) {
            int ownerID = tag.getInt("OwnerID");
            setOwner(getLevel().getEntity(ownerID));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("MaxAge", liveTime);
        tag.putString("Elemental", getElement().name());

        Entity entity = getOwner();
        if (entity != null) {
            tag.putInt("OwnerID", entity.getId());
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public Elementals getElement() {
        return GenshinHeler.safeGet(Elementals.class, getEntityData().get(ELEMENT));
    }

    public Tornado withElement(Elementals e) {
        if (e != null)
            getEntityData().set(ELEMENT, e.name());

        return this;
    }

    private AABB getWorkingArea() {
        Vec3 vec3 = new Vec3(2, .2, 2);

        AABB boundingBox = getBoundingBox();
        AABB result = boundingBox.expandTowards(vec3).expandTowards(vec3.reverse());

        return result;
    }
}
