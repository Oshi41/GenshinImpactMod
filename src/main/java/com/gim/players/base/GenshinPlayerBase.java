package com.gim.players.base;

import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GenshinPlayerBase extends ForgeRegistryEntry<IGenshinPlayer> implements IGenshinPlayer {

    protected BaseComponent name;
    protected ResourceLocation burstIcon;
    protected ResourceLocation icon;
    protected ResourceLocation skin;
    protected ResourceLocation skillIcon;

    @OnlyIn(Dist.CLIENT)
    protected net.minecraft.client.model.Model model;

    protected GenshinPlayerBase(BaseComponent name, ResourceLocation burstIcon, ResourceLocation icon, ResourceLocation skin, ResourceLocation skillIcon) {
        this.name = name;
        this.burstIcon = burstIcon;
        this.icon = icon;
        this.skin = skin;
        this.skillIcon = skillIcon;

        if (FMLLoader.getDist().isClient()) {
            initClient();
        }
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
    public void onTick(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks) {

    }

    @Override
    public void onSwitch(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks, boolean isActive) {

    }
}
