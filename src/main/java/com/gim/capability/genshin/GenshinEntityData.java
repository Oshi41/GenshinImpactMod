package com.gim.capability.genshin;

import com.gim.GenshinHeler;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.Registries;
import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class GenshinEntityData implements INBTSerializable<CompoundTag> {

    private final AttributeMap map;
    private final Map<MobEffect, MobEffectInstance> effects;
    private float health;
    private EnergyStorage energy;
    private IGenshinPlayer assotiatedPlayer;

    private int skillTicksAnim;
    private int burstTicksAnim;

    private CompoundTag additive = new CompoundTag();

    public GenshinEntityData(LivingEntity holder, IGenshinPlayer assotiatedPlayer, int energy) {
        // creating weak reference
        WeakReference<LivingEntity> weakReference = new WeakReference<>(holder);
        // creating base attributes map
        map = new GenshinAttributeMap(GenshinHeler.unionSupplier(holder.getAttributes(), assotiatedPlayer.cachedAttributes()),
                (attribute, attributeMap) -> this.afterAttributeChanged(weakReference, attribute, attributeMap));
        this.assotiatedPlayer = assotiatedPlayer;
        this.health = holder.getHealth();
        this.energy = new GenshinEnergyStorage(map, energy);
        effects = new ObservableMap<>(holder.getActiveEffectsMap(), (s, source) -> this.afterEffectsChanged(weakReference, s, source));
    }

    private void afterEffectsChanged(WeakReference<LivingEntity> reference, String operation, Map<MobEffect, MobEffectInstance> source) {
        // called from other changes
        if (effects != source) {
            return;
        }

        LivingEntity livingEntity = reference.get();
        // expired entity
        if (livingEntity == null)
            return;

        IGenshinInfo genshinInfo = livingEntity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        // no capability provided
        if (genshinInfo == null)
            return;

        // can apply to current entity
        if (Objects.equals(getAssotiatedPlayer(), genshinInfo.current())) {
            applyToEntity(livingEntity, ApplyTypes.EFFECTS);
        }
    }

    private void afterAttributeChanged(WeakReference<LivingEntity> reference, @Nullable Attribute attribute, AttributeMap source) {
        // called from other changes
        if (source != getAttributes()) {
            return;
        }

        LivingEntity livingEntity = reference.get();
        // expired entity
        if (livingEntity == null)
            return;

        IGenshinInfo genshinInfo = livingEntity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        // no capability provided
        if (genshinInfo == null)
            return;

        // can apply to current entity
        if (Objects.equals(getAssotiatedPlayer(), genshinInfo.current())) {
            applyToEntity(livingEntity, ApplyTypes.ATTRIBUTES);
        }
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
    public IEnergyStorage burstInfo() {
        return energy;
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
    public Map<MobEffect, MobEffectInstance> getGenshinEffects() {
        return effects;
    }

    /**
     * Returns current attributeMap
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
        tag.putInt("SkillAnim", getSkillTicksAnim());
        tag.putInt("BurstAnim", getBurstTicksAnim());
        tag.put("Energy", energy.serializeNBT());
        tag.put("Additional", getAdditional());

        if (!this.effects.isEmpty()) {
            ListTag listtag = new ListTag();

            for (MobEffectInstance mobeffectinstance : this.effects.values()) {
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
            MobEffectInstance effectInstance = MobEffectInstance.load(list.getCompound(i));
            getGenshinEffects().put(effectInstance.getEffect(), effectInstance);
        }

        this.assotiatedPlayer = Registries.characters().getValue(new ResourceLocation(nbt.getString("Character")));
        this.map.load(nbt.getList("Attributes", 10));

        this.burstTicksAnim = nbt.getInt("BurstAnim");
        this.skillTicksAnim = nbt.getInt("SkillAnim");
        energy.deserializeNBT(nbt.get("Energy"));
        setAdditional(nbt.getCompound("Additional"));
    }

    public void applyToEntity(LivingEntity entity) {
        applyToEntity(entity, ApplyTypes.values());
    }

    public void applyToEntity(LivingEntity entity, ApplyTypes... types) {
        if (entity == null || types == null || types.length == 0) {
            return;
        }

        for (ApplyTypes applyType : types) {
            switch (applyType) {
                case HEALTH:
                    entity.setHealth(getHealth());
                    break;

                case EFFECTS:
                    // unsubscribe from changes
                    if (entity.getActiveEffectsMap() instanceof ObservableMap<MobEffect, MobEffectInstance>) {
                        ((ObservableMap<MobEffect, MobEffectInstance>) entity.getActiveEffectsMap()).sync(null);
                    }

                    MapDifference<MobEffect, MobEffectInstance> effectsDiff = Maps.difference(entity.getActiveEffectsMap(), effects);

                    if (!effectsDiff.areEqual()) {
                        effectsDiff.entriesOnlyOnLeft().forEach((effect, effectInstance) -> entity.removeEffect(effect));
                        effectsDiff.entriesOnlyOnRight().forEach((effect, effectInstance) -> entity.addEffect(effectInstance));
                        effectsDiff.entriesDiffering().forEach((effect, mobEffectInstanceValueDifference) -> entity.addEffect(mobEffectInstanceValueDifference.rightValue()));
                    }


                    // subscribe again
                    if (entity.getActiveEffectsMap() instanceof ObservableMap<MobEffect, MobEffectInstance>) {
                        ((ObservableMap<MobEffect, MobEffectInstance>) entity.getActiveEffectsMap()).sync(effects);
                    }
                    break;

                case ATTRIBUTES:
                    AttributeMap entityMap = entity.getAttributes();

                    // unsubscribe from changes
                    if (entityMap instanceof GenshinAttributeMap) {
                        ((GenshinAttributeMap) entityMap).syncFor(null);
                    }

                    MapDifference<Attribute, AttributeInstance> attrDiff = Maps.difference(
                            GenshinAttributeMap.from(getAttributes()),
                            GenshinAttributeMap.from(entityMap),
                            new AttributeEquals());

                    if (!attrDiff.areEqual()) {
                        // we do not adding or removing attributes, only chanhing
                        attrDiff.entriesDiffering().forEach((attribute, diff) -> {
                            entity.getAttribute(attribute).replaceFrom(diff.leftValue());
                        });
                    }

                    // subscribe again
                    if (entityMap instanceof GenshinAttributeMap) {
                        ((GenshinAttributeMap) entityMap).syncFor(getAttributes());
                    }
                    break;
            }
        }
    }

    /**
     * Returns amount of ticks to perform skill animation
     *
     * @return
     */
    public int getSkillTicksAnim() {
        return skillTicksAnim;
    }

    /**
     * Setting skill animation length
     *
     * @param skillTicksAnim - animation length in ticks
     */
    public void setSkillTicksAnim(int skillTicksAnim) {
        this.skillTicksAnim = skillTicksAnim;
    }

    /**
     * Returns length of burst animation
     *
     * @return
     */
    public int getBurstTicksAnim() {
        return burstTicksAnim;
    }

    /**
     * Change burst animation length here
     *
     * @param burstTicksAnim
     */
    public void setBurstTicksAnim(int burstTicksAnim) {
        this.burstTicksAnim = burstTicksAnim;
    }

    /**
     * Returns additional info for current player
     */
    public CompoundTag getAdditional() {
        return additive;
    }

    /**
     * setting additional info for current player
     */
    public void setAdditional(CompoundTag additive) {
        this.additive = additive;
    }

    public enum ApplyTypes {
        ATTRIBUTES,
        HEALTH,
        EFFECTS,
    }
}
