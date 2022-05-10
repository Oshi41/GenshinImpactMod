package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.gim.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.nio.charset.Charset;

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
        this(EntityRegistry.shield_entity_type, source.getLevel());
        this.elemental = elemental;
        this.hp = hp;

        Vec3 vec3 = new Vec3(source.getRandomX(1), source.getRandomY(), source.getRandomZ(1));
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
    public void playerTouch(Player player) {
        // skipping tick delay
        if (tickCount > 3 * 20) {
            player.getCapability(Capabilities.SHIELDS).ifPresent(iShield -> {
                double majesty = GenshinHeler.majestyBonus(player);
                iShield.setShield(this.hp + (this.hp * (1 + majesty)), getElemental(), 17 * 20);
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
