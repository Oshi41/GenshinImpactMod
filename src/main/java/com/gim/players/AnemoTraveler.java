package com.gim.players;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinAreaSpreading;
import com.gim.attack.GenshinDamageSource;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.BadRespawnPointDamage;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AnemoTraveler extends GenshinPlayerBase {
    public AnemoTraveler() {
        super(
                new TranslatableComponent(GenshinImpactMod.ModID + ".traveler.name"),
                new ResourceLocation(GenshinImpactMod.ModID, "textures/players/anemo_traveler/burst.png"),
                new ResourceLocation(GenshinImpactMod.ModID, "textures/players/anemo_traveler/icon.png"),
                null,
                new ResourceLocation(GenshinImpactMod.ModID, "textures/players/anemo_traveler/skill.png"));
    }

    @Override
    public AttributeSupplier.Builder getAttributes() {
        return Player.createAttributes()
                .add(Attributes.defence, 3)
                .add(Attributes.attack_bonus, 2)
                .add(Attributes.burst_cost, 60d)
                .add(Attributes.burst_cooldown, 20 * 15)
                .add(Attributes.skill_cooldown, 20 * 8);
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
    public void onSkill(LivingEntity holder, GenshinEntityData data, List<CombatEntry> currentAttacks) {
        // only on server
        if (!holder.getLevel().isClientSide()) {
            GenshinDamageSource source = ((GenshinDamageSource) getElemental().create(holder)).bySkill();
            Vec3 vector = holder.position().add(0, holder.getY() / 2, 0);
            GenshinAreaSpreading spreading = new GenshinAreaSpreading(holder.getLevel(), vector, source, 3);
            spreading.explode();
        }

        holder.animationSpeed = 0.5f;
        super.onSkill(holder, data, currentAttacks);
    }

    @Override
    public void onBurst(LivingEntity holder, GenshinEntityData data, List<CombatEntry> currentAttacks) {
        super.onBurst(holder, data, currentAttacks);
    }
}
