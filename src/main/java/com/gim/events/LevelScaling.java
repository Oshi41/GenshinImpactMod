package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.registry.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelScaling {
    private static final UUID levelHealthId = UUID.fromString("283728cf-94ce-4875-bb41-b1fb8b79a9b8");
    private static final UUID levelAttackId = UUID.fromString("81dd41e0-d26f-432c-916d-671ccfeaf252");
    private static final UUID levelDefId = UUID.fromString("f3d4c503-d4a3-4224-9255-81adc12bf637");
    private static final UUID mainLevelModifierId = UUID.fromString("8e23d976-ef87-4170-a057-5d8eeba0bdcc");

    @SubscribeEvent
    public static void onEntityAdded(EntityJoinWorldEvent e) {
        // setting level for all living entities but players
        if (!(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof Player) {
            return;
        }

        Integer levelUpInMuntes = GenshinImpactMod.CONFIG.getKey().levelUpTime.get();
        long gameTime = e.getWorld().getGameTime();
        long ticks = levelUpInMuntes * 60 * 20;
        long currentWorldLevel = gameTime / ticks;
        if (currentWorldLevel < 1) {
            return;
        }

        float diff = (e.getWorld().getRandom().nextFloat() - 1) * 4;

        float entityLevel = Math.max(1, currentWorldLevel + diff);
        LivingEntity livingEntity = (LivingEntity) e.getEntity();

        if (scaleLevel(livingEntity::getAttribute, entityLevel)) {
            livingEntity.setHealth(livingEntity.getMaxHealth());
        }
    }

    /**
     * Perform entity level scaling
     *
     * @param getAttribute - attribute getter
     * @param level        - entity level
     * @return - success of operation
     */
    public static boolean scaleLevel(Function<Attribute, AttributeInstance> getAttribute, float level) {
        if (getAttribute == null || level < 0)
            return false;

        AttributeInstance attributeInstance = getAttribute.apply(Attributes.level);
        if (attributeInstance == null)
            return false;

        AttributeModifier modifier = new AttributeModifier(mainLevelModifierId, "level", level, AttributeModifier.Operation.ADDITION);
        attributeInstance.removeModifier(mainLevelModifierId);
        attributeInstance.addPermanentModifier(modifier);

        return addModifier(
                getAttribute.apply(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH),
                levelHealthId,
                "level.helath.scaling",
                level
        )
                &&
                addModifier(
                        getAttribute.apply(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                        levelAttackId,
                        "level.attack.scaling",
                        level
                )
                &&
                addModifier(
                        getAttribute.apply(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR),
                        levelDefId,
                        "level.armor.scaling",
                        level
                );
    }

    private static boolean addModifier(AttributeInstance instance, UUID id, String name, float level) {
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, name, GenshinImpactMod.CONFIG.getKey().levelScaling.get() * level, AttributeModifier.Operation.MULTIPLY_TOTAL));
            return true;
        }

        return false;
    }
}
