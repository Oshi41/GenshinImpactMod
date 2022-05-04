package com.gim.entity;

import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ShieldEntity extends Entity {

    private Elementals elemental;
    private int hp;

    public ShieldEntity(Level level, Elementals elemental, int hp) {
        // todo
        super(EntityType.AREA_EFFECT_CLOUD, level);
        this.elemental = elemental;
        this.hp = hp;
        setInvulnerable(true);
    }

    @Override
    public void tick() {
        super.tick();

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
                double majesty = 0;

                AttributeInstance instance = player.getAttribute(Attributes.elemental_majesty);
                if (instance != null) {
                    majesty = 444 * instance.getValue() / (instance.getValue() + 1400);
                }

                iShield.setShield(this.hp + (this.hp * (1 + majesty)), getElemental(), 17 * 20);
                discard();
            });
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ShieldHP", hp);
        tag.putString("Elemental", elemental.name());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        hp = tag.getInt("ShieldHP");
        elemental = Enum.valueOf(Elementals.class, tag.getString("Elemental"));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public Elementals getElemental() {
        return elemental;
    }

    public int getHp() {
        return hp;
    }
}
