package com.gim.capability.genshin;

import com.gim.GenshinImpactMod;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.GenshinCharacters;
import com.gim.registry.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GenshinEntityData implements INBTSerializable<CompoundTag> {

    private AttributeMap map;
    private List<MobEffectInstance> effects;
    private float health;
    private float energy;
    private IGenshinPlayer assotiatedPlayer;

    public GenshinEntityData(LivingEntity entity, GenshinEntityData source) {
        this(entity);
        deserializeNBT(source.serializeNBT());
    }

    public GenshinEntityData(LivingEntity entity) {
        this(
                entity.getAttributes(),
                entity.getActiveEffects(),
                entity.getHealth(),
                90,
                getCurrent(entity));
    }

    private static IGenshinPlayer getCurrent(LivingEntity e) {
        if (e != null) {
            IGenshinInfo info = e.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (info != null) {
                return info.current();
            }
        }

        return GenshinCharacters.ANEMO_TRAVELER;
    }

    public GenshinEntityData() {
        this(new AttributeMap(AttributeSupplier.builder().build()), new ArrayList<>(), 0, 0, GenshinCharacters.ANEMO_TRAVELER);
    }

    public GenshinEntityData(AttributeMap map, Collection<MobEffectInstance> effects, float health, float energy, IGenshinPlayer assotiatedPlayer) {
        this.map = map;
        this.effects = effects.stream().filter(x -> x.getEffect().getRegistryName().getNamespace().equals(GenshinImpactMod.ModID)).collect(Collectors.toList());
        this.health = health;
        this.energy = energy;
        this.assotiatedPlayer = assotiatedPlayer;
    }

    /**
     * Returns health of entity
     */
    public float getHealth() {
        return health;
    }

    /**
     * Returns current energy count
     */
    public float getEnergy() {
        return energy;
    }

    /**
     * Settings current energy count
     *
     * @param energy
     */
    public void setEnergy(float energy) {
        this.energy = energy;
    }

    /**
     * returns current character
     */
    public IGenshinPlayer getAssotiatedPlayer() {
        return assotiatedPlayer;
    }

    /**
     * Returns only Genshin active effects
     */
    public List<MobEffectInstance> getGenshinEffects() {
        return effects;
    }

    /**
     * Returns current attributes
     */
    public AttributeMap getAttributes() {
        return map;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Attributes", map.save());
        tag.putFloat("Health", health);
        tag.putString("Character", assotiatedPlayer.getRegistryName().toString());

        if (!this.effects.isEmpty()) {
            ListTag listtag = new ListTag();

            for (MobEffectInstance mobeffectinstance : this.effects) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }

            tag.put("Effects", listtag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        health = nbt.getFloat("Health");

        this.effects.clear();
        ListTag list = nbt.getList("Effects", 0);
        for (int i = 0; i < list.size(); i++) {
            this.effects.add(MobEffectInstance.load(list.getCompound(i)));
        }

        this.assotiatedPlayer = Registries.CHARACTERS.get().getValue(new ResourceLocation(nbt.getString("Character")));
        // new instance
        this.map = new AttributeMap(new AttributeSupplier.Builder(assotiatedPlayer.getAttributes()).build());
        this.map.load(nbt.getList("Attributes", 0));
    }

    public void applyToEntity(LivingEntity entity) {
        // setting health
        entity.setHealth(getHealth());

        // removing all genshin effects
        for (MobEffectInstance instance : entity.getActiveEffects().stream().filter(x -> x.getEffect().getRegistryName().getNamespace().equals(GenshinImpactMod.ModID)).toList()) {
            entity.removeEffect(instance.getEffect());
        }

        // adding all genshin effects
        effects.forEach(entity::addEffect);

        // player attributes
        AttributeMap entityMap = entity.getAttributes();

        // switching all base values
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            AttributeInstance old = entityMap.getInstance(attribute);

            // switching off all genshin attributes
            if (attribute.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID) && old != null) {
                old.setBaseValue(attribute.getDefaultValue());
            }

            // find instance to apply on entity
            AttributeInstance replacing = this.map.getInstance(attribute);

            // can replace old one
            if (old != null && replacing != null) {
                old.setBaseValue(replacing.getBaseValue());
            }
        }
    }
}
