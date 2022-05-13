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

public class ShieldEntity extends Entity implements IEntityAdditionalSpawnData {

    private Elementals elemental;
    private int hp;

    public ShieldEntity(EntityType<?> type, Level level) {
        super(type, level);
        setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {

    }

    public ShieldEntity(Entity source, Elementals elemental, int hp) {
        this(Entities.shield_entity_type, source.getLevel());
        this.elemental = elemental;
        this.hp = hp;

        Vec3 vec3 = new Vec3(source.getRandomX(2), source.getY(), source.getRandomZ(2));
        setPos(vec3);
    }

    @Override
    public void tick() {
        super.tick();
        this.move(MoverType.SELF, getDeltaMovement().add(0, -0.04, 0));

        // too old
        if (tickCount > 17.5 * 20) {
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
                double majesty = GenshinHeler.majestyBonus(player);
                iShield.setShield(this.hp + (this.hp * (1 + majesty)), getElemental(), 17 * 20);

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
        elemental = GenshinHeler.safeGet(Elementals.class, tag.getString("Elemental"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ShieldHP", hp);
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

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        String elementalName = getElemental() != null
                ? getElemental().name()
                : "";

        buffer.writeUtf(elementalName);
        buffer.writeInt(getHp());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.elemental = GenshinHeler.safeGet(Elementals.class, additionalData.readUtf());
        this.hp = additionalData.readInt();
    }
}
