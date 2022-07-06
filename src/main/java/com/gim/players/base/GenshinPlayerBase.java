package com.gim.players.base;

import com.gim.GenshinHeler;
import com.gim.attack.GenshinCombatTracker;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class GenshinPlayerBase extends ForgeRegistryEntry<IGenshinPlayer> implements IGenshinPlayer {

    protected BaseComponent name;
    protected Supplier<AttributeSupplier.Builder> builder;
    protected AttributeMap attributeMap;
    private List<Vec2> starPoses;

    private final Table<Integer, Integer, Double> strikes = HashBasedTable.create();

    /**
     * @param name              - localized name of character
     * @param attributes        - initial character attributes. Can be only Genshin attributes, no need to store health there
     * @param starPoses         - position of stars inside the constellation table
     * @param strikesOn0Level   - strike row multiplier on 0 skill level
     * @param strikesOnMaxLevel - strike row multiplier on max skill level
     */
    protected GenshinPlayerBase(BaseComponent name, Supplier<AttributeSupplier.Builder> attributes, List<Vec2> starPoses,
                                List<Double> strikesOn0Level, List<Double> strikesOnMaxLevel) {
        this.name = name;
        this.builder = attributes;
        this.starPoses = starPoses;

        if (strikesOn0Level.size() != strikesOnMaxLevel.size()) {
            throw new IllegalArgumentException("Strike stats for " + name.getString() + " should be the same size!");
        }

        for (int row = 0; row < strikesOn0Level.size(); row++) {
            Double min = strikesOn0Level.get(row);
            Double max = strikesOnMaxLevel.get(row);

            for (int column = 0; column <= Attributes.skill_level.getMaxValue(); column++) {
                double current = min + (max - min) / Attributes.skill_level.getMaxValue() * column;
                getStrikes().put(row, column, current);
            }
        }

        handleForgeEventForCurrentPlayer(LivingDamageEvent.class, e -> e.getSource().getEntity(), this::handleAttackInRaw);
    }

    /**
     * Handle row strikes
     *
     * @param event  - damage event
     * @param entity - genshin data holder
     * @param info   - genshin info
     */
    private void handleAttackInRaw(LivingDamageEvent event, LivingEntity entity, IGenshinInfo info) {
        // should be regular player attack
        if (!"player".equals(event.getSource().getMsgId())) {
            return;
        }

        GenshinEntityData genshinEntityData = info.getPersonInfo(this);
        List<Integer> attacks = genshinEntityData.getRowAttacks();

        // attacks is more than max row strike for character
        if (attacks.size() >= getStrikes().rowMap().size()) {
            attacks.clear();
        }

        // record current attack
        attacks.add(entity.tickCount);

        // index of current hit (row)
        int rowIndex = attacks.size() - 1;
        // skill level for current character (column)
        int column = (int) Math.max(0, GenshinHeler.safeGetAttribute(entity, Attributes.skill_level));
        if (getStrikes().contains(rowIndex, column)) {
            event.setAmount((float) (event.getAmount() * getStrikes().get(rowIndex, column)));
        }
    }

    /**
     * Subscribe to special event
     *
     * @param clazz    - event type
     * @param getOwner - get owner from entity
     * @param onHandle - handling event if current player enabled
     * @param <T>      - type of Forge event
     */
    protected <T extends Event> void handleForgeEventForCurrentPlayer(Class<T> clazz, Function<T, Entity> getOwner, TriConsumer<T, LivingEntity, IGenshinInfo> onHandle) {
        if (clazz == null || getOwner == null || onHandle == null)
            return;

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, clazz, e -> {
            Entity livingEntity = getOwner.apply(e);
            if (livingEntity instanceof LivingEntity) {
                livingEntity.getCapability(Capabilities.GENSHIN_INFO).ifPresent(iGenshinInfo -> {
                    if (equals(iGenshinInfo.current())) {
                        onHandle.accept(e, ((LivingEntity) livingEntity), iGenshinInfo);
                    }
                });
            }
        });
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
                    tracker.removeGenshinAttacks(x -> this.equals(x.burstOf()));

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
                    tracker.removeGenshinAttacks(x -> this.equals(x.skillOf()));

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

    /**
     * Table for strike hit multiplier.
     * Row - Hit row index
     * Column - skill level
     * Value - damage multiplier
     */
    protected Table<Integer, Integer, Double> getStrikes() {
        return strikes;
    }
}
