package com.gim.player;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.gim.GenshinHeler.safeAddModifier;
import static com.gim.GenshinHeler.safeGetAttribute;

public class Diluc {

    public final Map<Attribute, Double> initAttributes = new HashMap<>() {{
        put(Attributes.physical_bonus, 7d);
        put(Attributes.defence, 10d);

        put(Attributes.skill_cooldown, 5 * 20d);
        put(Attributes.burst_cooldown, 12 * 20d);
        put(Attributes.burst_cost, 40d);
    }};

    public final ResourceLocation icon = new ResourceLocation(GenshinImpactMod.ModID, "icon/diluc");
    public final ResourceLocation skin = new ResourceLocation(GenshinImpactMod.ModID, "skin/diluc");
    public final String name = "Diluc";
    public final UUID id = UUID.fromString("a4509964-a79a-4386-becf-3f6fa3d7bfa6");

    public void onLevelChanged(@NotNull LivingEntity entity, int level) {
        safeAddModifier(entity, Attributes.physical_bonus, new AttributeModifier(id, name, level + 1, AttributeModifier.Operation.MULTIPLY_BASE));
        safeAddModifier(entity, Attributes.defence, new AttributeModifier(id, name, level, AttributeModifier.Operation.MULTIPLY_BASE));

        // special stat
        safeAddModifier(entity, Attributes.crit_rate, new AttributeModifier(id, name, level * 0.05, AttributeModifier.Operation.ADDITION));
    }

    public void onSkillPressed(LivingEntity entity, int step) {
        LivingEntity mob = entity.getLastHurtByMob();
        if (mob != null) {
            Vec3 vectorTo = entity.position().vectorTo(mob.position()).normalize();
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, vectorTo);
        }

        EntityDamageSource source = new EntityDamageSource("diluc_" + step, entity);
        double multiplier = step == 0 ? 0.94
                : step == 1
                ? 0.97
                : 1.28;

        entity.hurt(source, (float) (Math.max(0, safeGetAttribute(entity, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) * multiplier * 1.08));
    }

    public void onBurstPressed(LivingEntity entity) {

    }
}
