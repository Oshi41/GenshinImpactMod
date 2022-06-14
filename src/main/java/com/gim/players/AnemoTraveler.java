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
import com.gim.players.base.GenshinPhase;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AnemoTraveler extends GenshinPlayerBase {
    public static int SKILL_ANIM_TIME = 20 * 2;
    public static int BURST_ANIM_TIME = 20 * 2;

    public static final UUID RECHARGE_MODIFIER = UUID.fromString("a4509964-a79a-4386-becf-3f6fa3d7bfa6");

    public AnemoTraveler() {
        super(
                new TranslatableComponent(GenshinImpactMod.ModID + ".traveler.name"),
                () -> AttributeSupplier.builder()
                        .add(Attributes.defence, 3)
                        .add(Attributes.attack_bonus, 2)
                        .add(Attributes.burst_cost, 60d)
                        .add(Attributes.burst_cooldown, 20 * 15)
                        .add(Attributes.skill_cooldown, 20 * 8),
                List.of(
                        new Vec2(26, 30),
                        new Vec2(8, 85),
                        new Vec2(79, 47),
                        new Vec2(102, 81)
                )
        );
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
    protected void onSkillTick(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker, GenshinPhase phase) {
        double skillAdditive = Math.max(0, GenshinHeler.safeGetAttribute(holder, Attributes.skill_level)) / 7f;
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
                    // area explosion
                    GenshinDamageSource source = getElemental().create(holder).bySkill(this);
                    Vec3 vector = holder.getEyePosition().add(holder.getLookAngle().normalize().scale(2));
                    GenshinAreaSpreading spreading = new GenshinAreaSpreading(holder, vector, source, ((float) range));
                    Map<Entity, Float> entityFloatMap = spreading.explode();

                    // need to spawn energy orbs
                    if (entityFloatMap.size() > 0) {
                        double count = GenshinHeler.gaussian(holder.getRandom(), 2, 3.5);
                        // find first hitted entity
                        Entity entity = entityFloatMap.keySet().stream().findFirst().orElse(null);
                        for (int i = 0; i < count; i++) {
                            // adding energy in world
                            holder.getLevel().addFreshEntity(new Energy(holder, entity, 1, getElemental()));
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
