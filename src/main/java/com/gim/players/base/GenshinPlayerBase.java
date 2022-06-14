package com.gim.players.base;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.gim.GenshinHeler;
import com.gim.attack.GenshinCombatTracker;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinAttributeMap;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.capability.genshin.ObservableMap;
import com.gim.registry.Attributes;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class GenshinPlayerBase extends ForgeRegistryEntry<IGenshinPlayer> implements IGenshinPlayer {

    protected BaseComponent name;
    protected Supplier<AttributeSupplier.Builder> builder;
    protected AttributeMap attributeMap;
    private List<Vec2> starPoses;

    protected GenshinPlayerBase(BaseComponent name, Supplier<AttributeSupplier.Builder> attributes, List<Vec2> starPoses) {
        this.name = name;
        this.builder = attributes;
        this.starPoses = starPoses;
    }

    @Override
    public AttributeMap cachedAttributes() {
        if (attributeMap == null) {

            attributeMap = GenshinHeler.union(
                    new AttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER)),
                    new AttributeMap(attributesBuilder().build())
            );

            List<RangedAttribute> attributeList = List.of(Attributes.burst_cooldown, Attributes.burst_cost, Attributes.skill_cooldown);

            for (RangedAttribute attribute : attributeList) {
                if (!attributeMap.hasAttribute(attribute)) {
                    String messageParam = String.join(", ", attributeList.stream().map(x -> x.getRegistryName().toString()).collect(Collectors.toList()));
                    String msg = String.format("Character %s must have all of these attributeMap: %s", getName().getString(), messageParam);
                    CrashReport report = CrashReport.forThrowable(new Exception(msg), msg);
                    throw new ReportedException(report);
                }
            }
        }

        return attributeMap;
    }

    @Override
    public BaseComponent getName() {
        return name;
    }

    @Override
    public List<Vec2> starPoses() {
        return starPoses;
    }

    @Override
    public AttributeSupplier.Builder attributesBuilder() {
        return builder.get();
    }

    /**
     * Removing previous skill attack history
     *
     * @param holder  - current entity
     * @param data    - current character data
     * @param tracker - attacks history
     */
    @Override
    public void performSkill(LivingEntity holder, IGenshinInfo data, GenshinCombatTracker tracker) {
        onSkillTick(holder, data, tracker, GenshinPhase.START);
    }

    @Override
    public int ticksTillSkill(LivingEntity entity, GenshinEntityData info, GenshinCombatTracker tracker) {
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }

        CombatEntry lastSkill = tracker.findFirstAttack(x -> this.equals(x.skillOf()));

        if (lastSkill == null) {
            return 0;
        }

        int ticksFromSkill = entity.tickCount - lastSkill.getTime();
        double cooldown = GenshinHeler.safeGetAttribute(info.getAttributes(), Attributes.skill_cooldown);

        return (int) Math.max(0, cooldown - ticksFromSkill);
    }

    /**
     * Removing previous burst attack history
     *
     * @param holder  - current entity
     * @param data    - entity data
     * @param tracker - attacks history
     */
    @Override
    public void performBurst(LivingEntity holder, IGenshinInfo data, GenshinCombatTracker tracker) {
        onBurstTick(holder, data, tracker, GenshinPhase.START);
    }

    @Override
    public int ticksTillBurst(LivingEntity entity, GenshinEntityData info, GenshinCombatTracker tracker) {
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }

        CombatEntry lastSkill = tracker.findFirstAttack(x -> this.equals(x.burstOf()));

        if (lastSkill == null) {
            return 0;
        }

        int ticksFromBurst = entity.tickCount - lastSkill.getTime();
        double cooldown = GenshinHeler.safeGetAttribute(info.getAttributes(), Attributes.burst_cooldown);

        return (int) Math.max(0, cooldown - ticksFromBurst);
    }

    @Override
    public void onTick(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker) {
        GenshinEntityData data = info.getPersonInfo(this);

        if (data != null) {
            if (data.getBurstTicksAnim() > 0) {
                onBurstTick(holder, info, tracker, GenshinPhase.TICK);
                data.setBurstTicksAnim(data.getBurstTicksAnim() - 1);

                if (data.getBurstTicksAnim() == 0) {
                    onBurstTick(holder, info, tracker, GenshinPhase.END);
                    // remove old burst attack history
                    tracker.removeAttacks(x -> this.equals(x.burstOf()));

                    // recording burst attack history
                    tracker.recordAttack(new GenshinDamageSource(DamageSource.GENERIC, null).byBurst(this), 0, 0);
                }
            }

            if (data.getSkillTicksAnim() > 0) {
                onSkillTick(holder, info, tracker, GenshinPhase.TICK);
                data.setSkillTicksAnim(data.getSkillTicksAnim() - 1);

                if (data.getSkillTicksAnim() == 0) {
                    onSkillTick(holder, info, tracker, GenshinPhase.END);
                    // remove old burst attack history
                    tracker.removeAttacks(x -> this.equals(x.skillOf()));

                    // recording skill attack history
                    tracker.recordAttack(new GenshinDamageSource(DamageSource.GENERIC, null).bySkill(this), 0, 0);
                }
            }
        }
    }

    @Override
    public void onSwitch(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker, boolean isActive) {
    }

    /**
     * Called on every tick for player using skill
     *
     * @param entity - current entity
     * @param info   - player info
     * @param phase  - current tick phase
     */
    protected abstract void onSkillTick(LivingEntity entity, IGenshinInfo info, GenshinCombatTracker tracker, GenshinPhase phase);

    /**
     * Called on every tick for player using burst
     *
     * @param entity  - current entity
     * @param info    - player info
     * @param tracker - attack history
     * @param phase   - current tick phase
     */
    protected abstract void onBurstTick(LivingEntity entity, IGenshinInfo info, GenshinCombatTracker tracker, GenshinPhase phase);
}
