package com.gim.players.base;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Attributes;
import com.google.common.collect.Iterables;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class GenshinPlayerBase extends ForgeRegistryEntry<IGenshinPlayer> implements IGenshinPlayer {

    protected BaseComponent name;
    protected ResourceLocation burstIcon;
    protected ResourceLocation icon;
    protected ResourceLocation skin;
    protected ResourceLocation skillIcon;

    @OnlyIn(Dist.CLIENT)
    protected net.minecraft.client.model.Model model;
    protected Lazy<AttributeSupplier> attributes;

    protected GenshinPlayerBase(BaseComponent name, ResourceLocation burstIcon, ResourceLocation icon, ResourceLocation skin, ResourceLocation skillIcon, Supplier<AttributeSupplier> attributes) {
        this.name = name;
        this.burstIcon = burstIcon;
        this.icon = icon;
        this.skin = skin;
        this.skillIcon = skillIcon;
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

        if (FMLLoader.getDist().isClient()) {
            initClient();
        }
    }

    @Override
    public AttributeSupplier getAttributes() {

        return attributes.get();
    }

    /**
     * Do some client stuff here
     */
    protected void initClient() {

    }

    @Override
    public BaseComponent getName() {
        return name;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final ResourceLocation getIcon() {
        return icon;
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public final ResourceLocation getSkin() {
        return skin;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final ResourceLocation getSkillIcon() {
        return skillIcon;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final ResourceLocation getBurstIcon() {
        return burstIcon;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public final net.minecraft.client.model.Model getModel() {
        return model;
    }

    /**
     * Removing previous skill attack history
     *
     * @param holder         - current entity
     * @param data           - current character data
     * @param currentAttacks - attacks history
     */
    @Override
    public void onSkill(LivingEntity holder, GenshinEntityData data, List<CombatEntry> currentAttacks) {
        currentAttacks.removeIf(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isSkill());
    }

    @Override
    public int ticksTillSkill(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks) {
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
    public void onBurst(LivingEntity holder, GenshinEntityData data, List<CombatEntry> currentAttacks) {
        currentAttacks.removeIf(x -> x.getSource() instanceof GenshinDamageSource && ((GenshinDamageSource) x.getSource()).isBurst());
    }

    @Override
    public int ticksTillBurst(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks) {
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

    }

    @Override
    public void onSwitch(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks, boolean isActive) {

    }
}
