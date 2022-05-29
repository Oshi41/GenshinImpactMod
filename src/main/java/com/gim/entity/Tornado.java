package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Tornado extends Projectile {
    private static final List<Elementals> SPREADING = ImmutableList.of(Elementals.HYDRO, Elementals.PYRO, Elementals.ELECTRO, Elementals.CRYO, Elementals.DENDRO);
    private static final EntityDataAccessor<String> ELEMENT = SynchedEntityData.defineId(Tornado.class, EntityDataSerializers.STRING);
    private int liveTime = Integer.MAX_VALUE;

    public Tornado(EntityType<? extends Projectile> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    public Tornado(Entity owner, int maxLiveTime, Elementals source) {
        this(Entities.tornado_entity_type, owner.getLevel());
        liveTime = maxLiveTime;
        withElement(source);
        setOwner(owner);

        setPos(owner.position().add(owner.getLookAngle()));
    }

    @Override
    public void tick() {
        super.tick();

        // too old
        if (this.tickCount >= liveTime) {
            discard();
            return;
        }

        if (getLevel().isClientSide())
            return;

        // middle of tornado
        Vec3 currentPosition = position();

        // Sucking entities to tornado (except owner)
        for (Entity entity : getLevel().getEntities(getOwner(), getWorkingArea())) {
            // except tornado entity
            if (entity == this) {
                continue;
            }

            // scale based on level
            double scale = (1 + GenshinHeler.safeGetAttribute(getOwner(), Attributes.skill_level)) / 4;
            Vec3 speed = currentPosition.subtract(entity.position()).normalize().scale(scale);
            entity.push(speed.x, speed.y, speed.z);
        }

        Vec3 vec3 = this.getDeltaMovement();
        this.move(MoverType.SELF, this.getDeltaMovement().add(0, -ForgeMod.ENTITY_GRAVITY.get().getDefaultValue(), 0));

        // slowing down by percent for tick
        // doing gravity here
        this.setDeltaMovement(vec3.scale(0.99F));
    }

    @Override
    public void gameEvent(GameEvent p_146856_, @Nullable Entity p_146857_, BlockPos p_146858_) {
        // no game events
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);

        Elementals element = getElement();
        // no element or no owner
        if (element == null) {
            return;
        }

        // trying to perform elemental reactions
        if (element == Elementals.ANEMO && entity instanceof LivingEntity) {
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

        double skill = GenshinHeler.safeGetAttribute(getOwner(), Attributes.skill_level);
        double level = GenshinHeler.safeGetAttribute(getOwner(), Attributes.level);

        float damage = (float) (skill * level);

        DamageSource damageSource = element.create(getOwner() == null ? this : getOwner());
        entity.hurt(damageSource, damage);
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(ELEMENT, Elementals.ANEMO.name());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        liveTime = tag.getInt("MaxAge");
        withElement(GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental")));

        if (getLevel().isClientSide() && tag.contains("OwnerID")) {
            int ownerID = tag.getInt("OwnerID");
            setOwner(getLevel().getEntity(ownerID));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
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
        float majestyBonus = GenshinHeler.majestyBonus(getOwner());
        double skillLevel = GenshinHeler.safeGetAttribute(getOwner(), Attributes.skill_level);

        double range = (1 + majestyBonus) * (1 + skillLevel) / Attributes.skill_level.getMaxValue() / 2 * 2.5;
        Vec3 vec3 = new Vec3(range, .2, range);

        return getBoundingBox().expandTowards(vec3).expandTowards(vec3.reverse());
    }
}
