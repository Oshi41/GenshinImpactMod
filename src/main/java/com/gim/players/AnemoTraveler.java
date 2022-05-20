package com.gim.players;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinAreaSpreading;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.GenshinPhase;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class AnemoTraveler extends GenshinPlayerBase {
    public AnemoTraveler() {
        super(
                new TranslatableComponent(GenshinImpactMod.ModID + ".traveler.name"),
                new ResourceLocation(GenshinImpactMod.ModID, "players/anemo_traveler/burst"),
                new ResourceLocation(GenshinImpactMod.ModID, "textures/players/anemo_traveler/icon.png"),
                null,
                new ResourceLocation(GenshinImpactMod.ModID, "players/anemo_traveler/skill"),
                () -> AttributeSupplier.builder()
                        .add(Attributes.defence, 3)
                        .add(Attributes.attack_bonus, 2)
                        .add(Attributes.burst_cost, 60d)
                        .add(Attributes.burst_cooldown, 20 * 15)
                        .add(Attributes.skill_cooldown, 20 * 8)
                        .build());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected net.minecraft.client.model.Model createModel() {
        return new com.gim.client.models.AnemoTravelerModel();
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
    public void performSkill(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks) {
        super.performSkill(holder, data, currentAttacks);
        GenshinEntityData personInfo = data.getPersonInfo(this);
        if (personInfo != null) {
            personInfo.setSkillTicksAnim(20 * 2);
        }
    }

    @Override
    public void performBurst(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks) {
        super.performBurst(holder, data, currentAttacks);

        GenshinEntityData personInfo = data.getPersonInfo(this);
        if (personInfo != null) {
            personInfo.setBurstTicksAnim(20 * 3);
        }
    }

    @Override
    protected void onSkillTick(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks, GenshinPhase phase) {
        double skillAdditive = GenshinHeler.safeGetAttribute(holder, Attributes.skill_level) / 8f;
        double range = 3f + skillAdditive;

        switch (phase) {
            case START:
                break;

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

            case END:
                // only on client
                if (holder.getLevel().isClientSide()) {
                    holder.getLevel().addParticle(ParticleTypes.EXPLOSION, holder.getX() + 0.5D, holder.getY() + 1, holder.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
                } else {
                    // area explosion
                    GenshinDamageSource source = ((GenshinDamageSource) getElemental().create(holder)).bySkill();
                    Vec3 vector = holder.position().add(0, holder.getY() / 2, 0);
                    GenshinAreaSpreading spreading = new GenshinAreaSpreading(holder.getLevel(), vector, source, ((float) range));
                    spreading.explode();

                    Vec3 center = holder.position().add(0, 1, 0);
                    List<Entity> entities = getAffectedEntities(holder, range);

                    for (Entity entity : entities) {
                        Vec3 movement = entity.getLookAngle().subtract(center).normalize();
                        entity.setDeltaMovement(movement.scale(1.2f + skillAdditive));
                    }
                }

                // adding current attack in attack history
                currentAttacks.add(new CombatEntry(((GenshinDamageSource) getElemental().create(holder)).bySkill(),
                        holder.tickCount, 0, holder.getHealth(), null, holder.fallDistance));
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
    protected void onBurstTick(LivingEntity entity, IGenshinInfo info, List<CombatEntry> currentAttacks, GenshinPhase phase) {

    }
}
