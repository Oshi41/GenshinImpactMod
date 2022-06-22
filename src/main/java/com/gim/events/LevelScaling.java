package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelScaling {
    private static final UUID levelHealthId = UUID.fromString("283728cf-94ce-4875-bb41-b1fb8b79a9b8");
    private static final UUID levelAttackId = UUID.fromString("81dd41e0-d26f-432c-916d-671ccfeaf252");
    private static final UUID levelDefId = UUID.fromString("f3d4c503-d4a3-4224-9255-81adc12bf637");

    @SubscribeEvent
    public static void onEntityAdded(EntityJoinWorldEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }

        Integer levelUpInMuntes = GenshinImpactMod.CONFIG.getKey().levelUpTime.get();
        long gameTime = e.getWorld().getGameTime();
        long ticks = levelUpInMuntes * 60 * 20;
        long currentWorldLevel = gameTime / ticks;
        if (currentWorldLevel < 1) {
            return;
        }

        // DEBUG
        currentWorldLevel = 4;

        float diff = (e.getWorld().getRandom().nextFloat() - 1) * 4;

        float entityLevel = Math.max(1, currentWorldLevel + diff);
        scaleLevel(((LivingEntity) e.getEntity()), entityLevel);
    }

    /**
     * Perform entity level scaling
     *
     * @param entity - current entity
     * @param level  - entity level
     * @return - success of operation
     */
    public static boolean scaleLevel(LivingEntity entity, float level) {
        if (entity == null || level < 0)
            return false;

        AttributeInstance attributeInstance = entity.getAttribute(Attributes.level);
        if (attributeInstance == null)
            return false;

        attributeInstance.setBaseValue(level);

        return addModifier(
                entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH),
                levelHealthId,
                "level.helath.scaling",
                level
        )
                &&
                addModifier(
                        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                        levelAttackId,
                        "level.attack.scaling",
                        level
                )
                &&
                addModifier(
                        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR),
                        levelDefId,
                        "level.armor.scaling",
                        level
                );
    }

    private static boolean addModifier(AttributeInstance instance, UUID id, String name, float level) {
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, name, GenshinImpactMod.CONFIG.getKey().levelScaling.get() * level, AttributeModifier.Operation.MULTIPLY_BASE));
            return true;
        }

        return false;
    }
}
