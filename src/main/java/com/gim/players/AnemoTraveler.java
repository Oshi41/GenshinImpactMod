package com.gim.players;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinAreaSpreading;
import com.gim.attack.GenshinCombatTracker;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.entity.Energy;
import com.gim.entity.Tornado;
import com.gim.players.base.AscendInfo;
import com.gim.players.base.GenshinPhase;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.players.base.TalentAscendInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.registry.Elementals;
import com.gim.registry.Items;
import com.google.common.collect.Iterators;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AnemoTraveler extends GenshinPlayerBase {
    public static final int SKILL_ANIM_TIME = 20 * 2;
    public static final int BURST_ANIM_TIME = 20 * 2;
    public static final double SKILL_ZERO_MULTIPLIER = 1.92;
    public static final double SKILL_MAX_MULTIPLIER = 4.08;

    public static final UUID RECHARGE_MODIFIER = UUID.fromString("a4509964-a79a-4386-becf-3f6fa3d7bfa6");

    private final Lazy<List<Elementals>> swirling = Lazy.of(() -> List.of(Elementals.HYDRO, Elementals.ELECTRO, Elementals.PYRO, Elementals.CRYO));

    private final Map<Integer, String> passiveTalents = new LinkedHashMap<>() {{
        put(5, "gim.anemo_traveler.passive_skill_1");
        put(15, "gim.anemo_traveler.passive_skill_2");
    }};

    public AnemoTraveler() {
        super(
                new TranslatableComponent("gim.traveler.name"),
                () -> AttributeSupplier.builder()
                        .add(Attributes.defence, 3)
                        .add(Attributes.physical_bonus, 2)
                        .add(Attributes.burst_cost, 60d)
                        .add(Attributes.burst_cooldown, 20 * 15)
                        .add(Attributes.skill_cooldown, 20 * 8),
                List.of(
                        new Vec2(26, 30),
                        new Vec2(8, 85),
                        new Vec2(79, 47),
                        new Vec2(102, 81)
                ),
                List.of(
                        0.445,
                        0.434,
                        0.53,
                        0.583,
                        0.708
                ),
                List.of(
                        0.941,
                        0.919,
                        1.12,
                        1.23,
                        1.5
                ));

        handleForgeEventForCurrentPlayer(LivingAttackEvent.class, x -> x.getSource().getEntity(), this::handleAttack);
        handleForgeEventForCurrentPlayer(LivingDeathEvent.class, x -> x.getSource().getEntity(), this::handleDeath);
    }

    private void handleDeath(LivingDeathEvent event, LivingEntity holder, IGenshinInfo genshinInfo) {
        GenshinEntityData genshinEntityData = genshinInfo.getPersonInfo(this);
        double skillLevel = genshinEntityData.getAttributes().getInstance(Attributes.skill_level).getValue();
        if (skillLevel >= 14) {
            long lastSkillHealTime = genshinEntityData.getAdditional().getLong("LastSkillHealTime");
            // skill cooldown is 5 seconds
            int skillHealingCooldown = 5 * 20;

            if (lastSkillHealTime < holder.tickCount - skillHealingCooldown) {
                holder.heal(holder.getMaxHealth() * 0.02f);
                genshinEntityData.getAdditional().putInt("LastSkillHealTime", holder.tickCount);
            }
        }
    }

    private void handleAttack(LivingAttackEvent event, LivingEntity holder, IGenshinInfo info) {
        if (holder.getCombatTracker() instanceof GenshinCombatTracker) {
            GenshinCombatTracker genshinCombatTracker = (GenshinCombatTracker) holder.getCombatTracker();

            double skillLevel = info.getPersonInfo(this).getAttributes().getInstance(Attributes.skill_level).getValue();

            if (skillLevel >= 13) {
                int minAttacksCount = 5;

                // 4 attacks should be done within 2 seconds
                int earliestTime = holder.tickCount - 20 * 2;

                List<CombatEntry> entries = genshinCombatTracker.getAttacks()
                        .filter(x -> x.getTime() >= earliestTime)
                        .filter(x -> x.getSource() instanceof EntityDamageSource && "player".equals(x.getSource().msgId))
                        .sorted((o1, o2) -> Integer.compare(o2.getTime(), o1.getTime()))
                        .toList();


                if (entries.size() >= minAttacksCount) {
                    // removing previous ones
                    entries.stream().skip(minAttacksCount)
                            .collect(Collectors.toList()).stream().forEach(entries::remove);

                    Set<DamageSource> sources = entries.stream().map(x -> x.getSource()).collect(Collectors.toSet());

                    // adding anemo attack
                    event.getEntity().hurt(Elementals.ANEMO.create(holder), event.getAmount() * 0.6f);
                    // removing prev history
                    genshinCombatTracker.removeAttacks(sources::contains);
                }
            }
        }
    }

    @Override
    public boolean effectiveWeapon(LivingEntity entity, ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public Elementals getElemental() {
        return Elementals.ANEMO;
    }

    @Override
    public void onStarAdded(LivingEntity holder, IGenshinInfo info, int currentStarCount) {
        switch (currentStarCount) {

            case 2:
                GenshinEntityData entityData = info.getPersonInfo(this);
                if (entityData != null) {
                    AttributeInstance instance = entityData.getAttributes().getInstance(Attributes.recharge_bonus);
                    if (instance != null) {
                        instance.addPermanentModifier(new AttributeModifier(RECHARGE_MODIFIER, "anemo_traveler_recharge", 1.16, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    }
                }
                break;

        }
    }

    @Override
    public AscendInfo ascendingInfo(int level, GenshinEntityData data) {
        // special attribute starting to scale since 5 level
        Attribute special = level >= 5
                ? Attributes.physical_bonus
                : null;

        List<ItemStack> stacks = new ArrayList<>();

        stacks.add(new ItemStack(Blocks.wind_astra.asItem(), Mth.clamp((level + 1) * 3, 3, 64)));
        double halfLevel = Attributes.level.getMaxValue() / 2;
        boolean moreHalf = level >= halfLevel;
        double currentCount = 1 + (moreHalf ? level - halfLevel : level);


        stacks.add(new ItemStack(
                moreHalf
                        ? Items.brilliant_large
                        : Items.brilliant,
                (int) (2 * (currentCount))
        ));

        stacks.add(new ItemStack(
                moreHalf
                        ? Items.hard_mask
                        : Items.mask,
                (int) (Mth.clamp(3 * currentCount, 3, 64))
        ));

        Component text = null;

        if (passiveTalents.containsKey(level)) {
            text = new TranslatableComponent("gim.talent.unlocked",
                    new TranslatableComponent(String.format("%s.%s.passive.%s",
                            getRegistryName().getNamespace(),
                            getRegistryName().getPath(),
                            Iterators.indexOf(passiveTalents.keySet().iterator(), integer -> level == integer))));
        }

        int expLevels = (level + 1) * 3;
        if (level >= Attributes.level.getMaxValue()) {
            expLevels = -1;
        }

        return new AscendInfo(data.getAttributes(), level, expLevels, special, text, stacks.toArray(ItemStack[]::new));
    }

    @Override
    public TalentAscendInfo talentInfo(int levelRaw, GenshinEntityData data) {
        String openSymbol = "§a☑ ";
        String closedSymbol = "§c☒ ";

        NonNullList<ItemStack> materials = NonNullList.create();
        List<MutableComponent> info = new ArrayList<>();
        int expLevel = -1;
        int characterLevel = -1;
        DecimalFormat decimalFormat = new DecimalFormat("###.##%");

        // valid values
        final int level = (int) Mth.clamp(levelRaw, 0, Attributes.skill_level.getMaxValue());
        boolean maxLevel = level >= Attributes.skill_level.getMaxValue();

        if (!maxLevel) {
            // 3 ascending at least
            // then growing at arithmetical progression
            characterLevel = (int) Mth.clamp(3 + Math.floor(level) / 2, 3, Attributes.level.getMaxValue());
            expLevel = level;

            /////////////////////
            // Filling materials
            /////////////////////
            double max = 20;
            if (level <= max) {
                materials.add(new ItemStack(Items.mask, (int) Math.floor((6 + (ItemStack.EMPTY.getMaxStackSize() - 6) / max * level))));
                materials.add(new ItemStack(Items.resistance_scroll, (int) Math.floor(3 + (ItemStack.EMPTY.getMaxStackSize() - 3) / max * level)));
            } else {
                double maxPossible = Attributes.skill_level.getMaxValue();

                // max improved items amount
                double newMax = (Attributes.skill_level.getMaxValue() - max);
                double currentAmount = level - max;

                materials.add(new ItemStack(Items.hard_mask, (int) Math.floor((6 + (ItemStack.EMPTY.getMaxStackSize() - 6) / newMax * (currentAmount)))));
                materials.add(new ItemStack(Items.resistance_scroll_2, (int) Math.floor(3 + (ItemStack.EMPTY.getMaxStackSize() - 3) / newMax * (currentAmount))));

                currentAmount = maxPossible - level;

                // last 9 have claw(s)
                if (currentAmount <= 9) {
                    materials.add(new ItemStack(Items.dragon_claw, currentAmount >= 5 ? 1 : 2));
                }

                // last 3 have crown
                if (currentAmount <= 3) {
                    materials.add(new ItemStack(Items.crown, 1));
                }
            }
        }

        MutableComponent component = maxLevel
                ? new TextComponent(String.format("MAX (%s)", level))
                : new TranslatableComponent("gim.level", String.format("%s --> %s", level, level + 1));

        info.add(component.withStyle(ChatFormatting.BLACK));

        String current, result;

        //////////////////
        // strike attacks
        //////////////////
        // iterating through attack strikes
        for (Map.Entry<Integer, Map<Integer, Double>> entry : getStrikes(data).rowMap().entrySet()) {
            current = decimalFormat.format(entry.getValue().get(level));
            result = maxLevel ? current : (current + " --> " + decimalFormat.format(entry.getValue().get(level + 1)));

            info.add(new TranslatableComponent("gim.attack_" + (entry.getKey() + 1), result));
        }

        ////////
        // skill
        ////////
        current = decimalFormat.format(skillMultiplier(level));
        result = maxLevel ? current : (current + " --> " + decimalFormat.format(skillMultiplier(level + 1)));
        info.add(new TranslatableComponent("gim.anemo_traveler.vortex_damage", result));


        //////////////////
        // tornado scaling
        //////////////////
        current = decimalFormat.format(Tornado.getMultiplier(level));
        result = maxLevel ? current : (current + " --> " + decimalFormat.format(Tornado.getMultiplier(level + 1)));
        info.add(new TranslatableComponent("gim.tornado_damage", result));


        // character level is too low
        if (characterLevel > GenshinHeler.safeGetAttribute(data.getAttributes(), Attributes.level)) {
            info.add(new TranslatableComponent("gim.talent.low_character_level", characterLevel)
                    .withStyle(ChatFormatting.RED));
        }

        // experience info
        if (!maxLevel) {
            info.add(new TranslatableComponent("container.enchant.level.requirement", level + 1));
        }

        List<MutableComponent> skillInfo = new ArrayList<>();
        skillInfo.add(new TextComponent(openSymbol).append(new TranslatableComponent("gim.anemo_traveler.attack_skill"))
                .append("\n"));
        skillInfo.add(new TextComponent(openSymbol).append(new TranslatableComponent("gim.anemo_traveler.elemental_skill"))
                .append("\n"));
        skillInfo.add(new TextComponent(openSymbol).append(new TranslatableComponent("gim.anemo_traveler.burst_skill"))
                .append("\n"));

        for (Map.Entry<Integer, String> entry : passiveTalents.entrySet()) {
            String txtId = entry.getValue();
            Integer lvl = entry.getKey();

            boolean valid = lvl <= level;
            MutableComponent txt = new TextComponent(valid ? openSymbol : closedSymbol).append(new TranslatableComponent(txtId));
            if (!valid) {
                txt.append("\n")
                        .append(new TranslatableComponent("gim.will_open_on_level", lvl).withStyle(ChatFormatting.GRAY));
            }
            txt.append("\n");

            skillInfo.add(txt);
        }

        return new TalentAscendInfo(materials, info, expLevel, characterLevel, skillInfo);
    }

    @Override
    protected void onSkillTick(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker, GenshinPhase phase) {
        double skillAdditive = Math.max(0, GenshinHeler.safeGetAttribute(holder, Attributes.skill_level)) / Attributes.skill_level.getMaxValue() * 1.5;
        double starCount = Math.max(0, GenshinHeler.safeGetAttribute(holder, Attributes.constellations));
        double range = 0;

        if (starCount > 0) {
            range += 5 + skillAdditive;
        }

        switch (phase) {
            // starting skill animation
            case START:
                GenshinEntityData personInfo = info.getPersonInfo(this);
                if (personInfo != null) {
                    personInfo.setSkillTicksAnim(SKILL_ANIM_TIME);
                }
                break;

            // sucking entities
            case TICK:
                if (!holder.getLevel().isClientSide()) {
                    Vec3 center = holder.position().add(0, 1, 0);
                    List<Entity> entities = getAffectedEntities(holder, range);

                    for (Entity entity : entities) {
                        Vec3 movement = center.subtract(entity.position()).normalize();
                        entity.setDeltaMovement(movement.scale(1 / 6f + skillAdditive));
                    }
                } else {
                    for (int i = 0; i < 10; ++i) {
                        double d0 = holder.getX() + (holder.getLevel().getRandom().nextDouble() - 0.5) * 3;
                        double d1 = holder.getY() + (holder.getLevel().getRandom().nextDouble() - 0.5) * 3;
                        double d2 = holder.getZ() + (holder.getLevel().getRandom().nextDouble() - 0.5) * 3;
                        double d3 = ((double) holder.getLevel().getRandom().nextFloat() - 0.5D) * 0.5D;
                        double d4 = ((double) holder.getLevel().getRandom().nextFloat() - 0.5D) * 0.5D;
                        double d5 = ((double) holder.getLevel().getRandom().nextFloat() - 0.5D) * 0.5D;
                        int j = holder.getLevel().getRandom().nextInt(2) * 2 - 1;
                        if (holder.getLevel().getRandom().nextBoolean()) {
                            d0 = holder.getX() + 0.5D + 0.25D * (double) j;
                            d3 = holder.getLevel().getRandom().nextFloat() * 2.0F * (float) j;
                        } else {
                            d2 = holder.getZ() + 0.5D + 0.25D * (double) j;
                            d5 = holder.getLevel().getRandom().nextFloat() * 2.0F * (float) j;
                        }

                        holder.getLevel().addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
                    }
                }
                break;

            // final exploding
            case END:
                // only on client
                if (holder.getLevel().isClientSide()) {
                    holder.getLevel().addParticle(ParticleTypes.EXPLOSION, holder.getX() + 0.5D, holder.getY() + 1, holder.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
                } else {
                    Elementals elemental = getElemental();

                    List<LivingEntity> affectedEntities = getAffectedEntities(holder, range).stream().filter(x -> x instanceof LivingEntity)
                            .map(x -> ((LivingEntity) x)).toList();

                    elemental = swirling.get().stream().filter(x -> affectedEntities.stream().anyMatch(x::is)).findFirst().orElse(elemental);


                    // area explosion
                    GenshinDamageSource source = elemental.create(holder).bySkill(this);
                    Vec3 vector = holder.getEyePosition().add(holder.getLookAngle().normalize().scale(2));
                    GenshinAreaSpreading spreading = new GenshinAreaSpreading(holder, vector, source, ((float) range),
                            (diameter, attackPercent) -> this.createDamage(diameter, attackPercent, GenshinHeler.safeGetAttribute(holder, Attributes.skill_level), starCount,
                                    GenshinHeler.safeGetAttribute(holder, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)));
                    Map<Entity, Float> entityFloatMap = spreading.explode();

                    // need to spawn energy orbs
                    if (entityFloatMap.size() > 0) {
                        double count = GenshinHeler.gaussian(holder.getRandom(), 2, 3.5);
                        // find first hitted entity
                        Entity entity = entityFloatMap.keySet().stream().findFirst().orElse(null);
                        for (int i = 0; i < count; i++) {
                            // adding energy in world
                            holder.getLevel().addFreshEntity(new Energy(holder, entity, 1, elemental));
                        }
                    }

                    List<Entity> entities = getAffectedEntities(holder, range);

                    for (Entity entity : entities) {
                        Vec3 movement = holder.getLookAngle().multiply(2, 2, 2).subtract(entity.position()).normalize().scale(1.2f + skillAdditive);
                        entity.push(movement.x, movement.y, movement.z);
                    }
                }
                break;
        }
    }

    /**
     * Calculating damage for anemo skill
     *
     * @param diameter      - explosion diameter
     * @param attackPercent - attack percent for explosion
     * @param skill         - skill level
     * @param starCount     - star count
     * @param attackDamage  - holder attack damage
     * @return - calculated damage per entity
     */
    private float createDamage(float diameter, double attackPercent, double skill, double starCount, double attackDamage) {
        double attackBonus = skillMultiplier((int) skill);
        double result = attackDamage * attackBonus * attackPercent * (1 + diameter);
        return (float) result;
    }

    /**
     * Calculates vortex attack multiplier
     *
     * @param skill - current skill level
     * @return
     */
    private static double skillMultiplier(int skill) {
        double minAttackBonus = SKILL_ZERO_MULTIPLIER;
        double maxAttackBonus = SKILL_MAX_MULTIPLIER;

        double attackBonus = minAttackBonus + (maxAttackBonus - minAttackBonus) / Attributes.skill_level.getMaxValue() * skill;
        return attackBonus;
    }

    /**
     * Returns affected entities by skill usage
     *
     * @param holder - current attacking entity
     * @param range  - range of skill
     * @return list of affected entitities
     */
    private List<Entity> getAffectedEntities(LivingEntity holder, double range) {
        Vec3 lookAngle = holder.getLookAngle();
        AABB boundingBox = holder.getBoundingBox()
                .expandTowards(lookAngle.scale(range))
                // backwards is not so effective, so it has only half of power
                .expandTowards(lookAngle.reverse().scale(1 / 2d));

        return holder.getLevel().getEntities(holder, boundingBox);
    }

    @Override
    protected void onBurstTick(LivingEntity entity, IGenshinInfo info, GenshinCombatTracker tracker, GenshinPhase phase) {

        switch (phase) {
            case START:
                GenshinEntityData personInfo = info.getPersonInfo(this);
                if (personInfo != null) {
                    personInfo.setBurstTicksAnim(BURST_ANIM_TIME);
                    entity.setInvulnerable(true);
                    // storing y rotation
                    personInfo.getAdditional().putFloat("YHeadRot", entity.getYHeadRot());
                }

                break;

            case TICK:
                double ySpeed = Math.max(0, GenshinHeler.safeGetAttribute(entity, ForgeMod.ENTITY_GRAVITY.get())) + 0.004;
                entity.setDeltaMovement(0, ySpeed, 0);
                break;

            case END:
                entity.setInvulnerable(false);

                personInfo = info.getPersonInfo(this);
                if (personInfo != null && !entity.getLevel().isClientSide()) {
                    // launching tornado by saved rotation
                    Tornado tornado = new Tornado(entity, personInfo.getAdditional().getFloat("YHeadRot"), 20 * 6, Elementals.ANEMO);
                    entity.getLevel().addFreshEntity(tornado);
                }

                break;
        }
    }
}
