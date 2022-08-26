package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;

public class Energy extends Entity implements IEntityAdditionalSpawnData {
    /**
     * Entity gravity
     */
    private final double GRAVITY = 0.01;

    /**
     * Scan peroid for checking following player
     */
    private static final int ENTITY_SCAN_PERIOD = 20;

    /**
     * Max following distance to player (2 chunks)
     */
    private static final int MAX_FOLLOWING_DISTANCE = 16 * 2;

    private Entity following;
    private int energy;
    private Elementals elementals;

    public Energy(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    public Energy(Entity owner, Entity victim, int energy, Elementals elementals) {
        super(Entities.energy_orb, owner.getLevel());

        following = owner;
        this.energy = energy;
        this.elementals = elementals;
        setPos(victim.getEyePosition()
                .add(random.nextGaussian(),
                        random.nextFloat() * 3,
                        random.nextGaussian())
        );

        setDeltaMovement(
                random.nextGaussian() * 0.01,
                random.nextFloat() + 0.01,
                random.nextGaussian() * 0.01
        );
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void tick() {
        super.tick();

        // doing gravity
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0, -GRAVITY, 0));
        }

        // finding nearest player
        if (tickCount % ENTITY_SCAN_PERIOD == 0) {
            if (this.following == null || this.following.distanceTo(this) > MAX_FOLLOWING_DISTANCE) {
                this.following = this.level.getNearestPlayer(this, MAX_FOLLOWING_DISTANCE);
            }
        }

        if (getLevel().isClientSide()) {
            getLevel().addParticle(new DustParticleOptions(new Vector3f(Vec3.fromRGB24(Color.white.getRGB())), 1), getX(), getY(), getZ(), 0, 0, 0);
        }

        // check current following entity
        if (following != null) {
            if (following.isSpectator() || (following instanceof LivingEntity && ((LivingEntity) following).isDeadOrDying())) {
                following = null;
            }
        }

        // following to entity (but at start we need to fly above)
        if (following != null && tickCount > 20) {
            Vec3 vec3 = new Vec3(this.following.getX() - this.getX(), this.following.getY() + (double) this.following.getEyeHeight() / 2.0D - this.getY(), this.following.getZ() - this.getZ());
            double d0 = vec3.length();
            if (d0 < MAX_FOLLOWING_DISTANCE) {
                double d1 = 1.0D - Math.sqrt(d0) / MAX_FOLLOWING_DISTANCE;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.1D)));
            }
        }

        // perform moving
        this.move(MoverType.SELF, this.getDeltaMovement());

        float f = 0.98F;
        this.setDeltaMovement(this.getDeltaMovement().scale(f));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        energy = tag.getInt("Energy");
        elementals = GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Energy", energy);
        tag.putString("Elemental", elementals == null ? "" : elementals.name());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void playerTouch(Player player) {
        IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (genshinInfo == null) {
            return;
        }

        genshinInfo.consumeEnergy(player, energy, elementals);
        discard();
    }

    public int getIcon() {
        return switch (energy) {
            // regular orb
            case 1 -> 3;
            // large orb
            case 3 -> 10;
            default -> 0;
        };
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(energy);
        buffer.writeUtf(elementals == null ? "" : elementals.name());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        energy = additionalData.readInt();
        elementals = GenshinHeler.safeGet(Elementals.class, additionalData.readUtf());
    }
}
