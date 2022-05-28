package com.gim.players.base;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Attributes;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class GenshinPlayerBase extends ForgeRegistryEntry<IGenshinPlayer> implements IGenshinPlayer {

    protected BaseComponent name;
    protected Lazy<AttributeSupplier> attributes;

    protected GenshinPlayerBase(BaseComponent name, Supplier<AttributeSupplier> attributes) {
        this.name = name;
        this.attributes = Lazy.of(() -> {
            AttributeSupplier supplier = attributes.get();

            // These attributes must have any character
            try {
                supplier.getValue(Attributes.burst_cooldown);
                supplier.getValue(Attributes.burst_cost);
                supplier.getValue(Attributes.skill_cooldown);
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.info(e);

                List<String> list = Stream.of(Attributes.burst_cooldown, Attributes.burst_cost, Attributes.skill_cooldown)
                        .map(x -> x.getRegistryName().toString())
                        .toList();
                String messageParam = String.join(", ", list);
                String msg = String.format("Character %s must have all of these attributes: %s", getName().getString(), messageParam);
                CrashReport report = CrashReport.forThrowable(e, msg);
                throw new ReportedException(report);
            }

            return supplier;
        });
    }

    @OnlyIn(Dist.CLIENT)
    protected net.minecraft.client.model.Model createModel() {
        return null;
    }

    @Override
    public AttributeSupplier getAttributes() {

        return attributes.get();
    }

    @Override
    public BaseComponent getName() {
        return name;
    }

    /**
     * Removing previous skill attack history
     *
     * @param holder         - current entity
     * @param data           - current character data
     * @param currentAttacks - attacks history
     */
    @Override
    public void performSkill(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks) {
        currentAttacks.removeIf(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isSkill());
        onSkillTick(holder, data, currentAttacks, GenshinPhase.START);
    }

    @Override
    public int ticksTillSkill(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks) {
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }

        CombatEntry skillUsages = attacks.stream()
                .filter(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isSkill())
                .max(Comparator.comparingInt(CombatEntry::getTime))
                .orElse(null);

        if (skillUsages == null) {
            return 0;
        }

        double cooldown = info.getAttributes().getValue(Attributes.skill_cooldown);

        return (int) Math.max(0, skillUsages.getTime() + cooldown - entity.tickCount);
    }

    /**
     * Removing previous burst attack history
     *
     * @param holder         - current entity
     * @param data           - entity data
     * @param currentAttacks - attacks history
     */
    @Override
    public void performBurst(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks) {
        currentAttacks.removeIf(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isBurst());
        onBurstTick(holder, data, currentAttacks, GenshinPhase.START);
    }

    @Override
    public int ticksTillBurst(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks) {
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }

        CombatEntry burstUsages = attacks.stream()
                .filter(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isBurst())
                .max(Comparator.comparingInt(CombatEntry::getTime))
                .orElse(null);

        if (burstUsages == null) {
            return 0;
        }

        double cooldown = info.getAttributes().getValue(Attributes.burst_cooldown);
        return (int) Math.max(0, burstUsages.getTime() + cooldown - entity.tickCount);
    }

    @Override
    public void onTick(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks) {
        GenshinEntityData data = info.getPersonInfo(this);
        if (data != null) {
            if (data.getBurstTicksAnim() > 0) {
                onBurstTick(holder, info, currentAttacks, GenshinPhase.TICK);
                data.setBurstTicksAnim(data.getBurstTicksAnim() - 1);

                if (data.getBurstTicksAnim() == 0) {
                    onBurstTick(holder, info, currentAttacks, GenshinPhase.END);
                }
            }

            if (data.getSkillTicksAnim() > 0) {
                onSkillTick(holder, info, currentAttacks, GenshinPhase.TICK);
                data.setSkillTicksAnim(data.getSkillTicksAnim() - 1);

                if (data.getSkillTicksAnim() == 0) {
                    onSkillTick(holder, info, currentAttacks, GenshinPhase.END);
                }
            }
        }
    }

    @Override
    public void onSwitch(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks, boolean isActive) {

    }

    /**
     * Called on every tick for player using skill
     *
     * @param entity         - current entity
     * @param info           - player info
     * @param currentAttacks - attack history
     * @param phase          - current tick phase
     */
    protected abstract void onSkillTick(LivingEntity entity, IGenshinInfo info, List<CombatEntry> currentAttacks, GenshinPhase phase);

    /**
     * Called on every tick for player using burst
     *
     * @param entity         - current entity
     * @param info           - player info
     * @param currentAttacks - attack history
     * @param phase          - current tick phase
     */
    protected abstract void onBurstTick(LivingEntity entity, IGenshinInfo info, List<CombatEntry> currentAttacks, GenshinPhase phase);
}
