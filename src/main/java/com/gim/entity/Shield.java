package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.networking.CapabilityUpdatePackage;
import com.gim.registry.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import java.util.Random;

public class Shield extends Entity implements IEntityAdditionalSpawnData {

    private Elementals elemental;
    private int hp;
    private double effectivity;
    private int duration;

    public Shield(EntityType<?> type, Level level) {
        super(type, level);
        setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {

    }

    public Shield(Entity source, Elementals elemental, int hp, double effectivity, int duration) {
        this(Entities.shield, source.getLevel());
        this.elemental = elemental;
        this.hp = hp;
        this.effectivity = effectivity;
        this.duration = duration;

        Vec3 vec3 = new Vec3(source.getRandomX(2), source.getY(), source.getRandomZ(2));
        setPos(vec3);
    }

    @Override
    public void tick() {
        super.tick();
        this.move(MoverType.SELF, getDeltaMovement().add(0, -0.04, 0));

        // too old
        if (tickCount > duration * 1.1) {
            discard();
        }
    }

    @Override
    public void onClientRemoval() {
        SimpleParticleType type = switch (this.getElemental()) {
            case HYDRO -> ParticleTypes.HYDRO;
            case CRYO -> ParticleTypes.CRYO;
            case ELECTRO -> ParticleTypes.ELECTRO;
            case DENDRO -> ParticleTypes.DENDRO;
            case ANEMO -> ParticleTypes.ANEMO;
            case GEO -> ParticleTypes.GEO;
            case PYRO -> ParticleTypes.PYRO;
            default -> null;
        };

        if (type != null) {
            // taken from nether portal
            Random random = getLevel().getRandom();
            for (int i = 0; i < 25; ++i) {
                double d0 = getRandomX(3);
                double d1 = getRandomY();
                double d2 = getRandomZ(3);
                double d3 = ((double) random.nextFloat() - 0.5D) * 0.5D;
                double d4 = ((double) random.nextFloat() - 0.5D) * 0.5D;
                double d5 = ((double) random.nextFloat() - 0.5D) * 0.5D;
                int j = random.nextInt(2) * 2 - 1;
                if (i % 2 == 0) {
                    d0 = getX() + 0.5D + 0.25D * (double) j;
                    d3 = random.nextFloat() * 2.0F * (float) j;
                } else {
                    d2 = getZ() + 0.5D + 0.25D * (double) j;
                    d5 = random.nextFloat() * 2.0F * (float) j;
                }

                getLevel().addParticle(type, d0, d1, d2, d3, d4, d5);
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        // skipping tick delay
        if (tickCount > 3 * 20) {
            player.getCapability(Capabilities.SHIELDS).ifPresent(iShield -> {
                iShield.setShield(this.hp, this.effectivity, getElemental(), duration);

                if (player instanceof ServerPlayer) {
                    GenshinImpactMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CapabilityUpdatePackage(Capabilities.SHIELDS, iShield));
                }

                discard();
            });
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        hp = tag.getInt("ShieldHP");
        duration = tag.getInt("Duration");
        effectivity = tag.getDouble("Effectivity");
        elemental = GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ShieldHP", hp);
        tag.putInt("Duration", duration);
        tag.putDouble("Effectivity", effectivity);
        tag.putString("Elemental", getElemental() != null ? getElemental().name() : "");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public Elementals getElemental() {
        return elemental;
    }

    public int getHp() {
        return hp;
    }

    public double getEffectivity() {
        return effectivity;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        String elementalName = getElemental() != null
                ? getElemental().name()
                : "";

        buffer.writeUtf(elementalName);
        buffer.writeInt(getHp());
        buffer.writeInt(getDuration());
        buffer.writeDouble(getEffectivity());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.elemental = GenshinHeler.safeGet(Elementals.class, additionalData.readUtf());
        this.hp = additionalData.readInt();
        this.duration = additionalData.readInt();
        this.effectivity = additionalData.readDouble();
    }
}
