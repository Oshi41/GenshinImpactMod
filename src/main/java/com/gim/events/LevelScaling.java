package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelScaling {
    private static final UUID mainLevelModifierId = UUID.fromString("8e23d976-ef87-4170-a057-5d8eeba0bdcc");

    /**
     * List of scaling modifiers
     */
    public static final Lazy<Map<Attribute, UUID>> SCALING_MODIFIERS = Lazy.of(() -> ImmutableMap.of(
            net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, UUID.fromString("283728cf-94ce-4875-bb41-b1fb8b79a9b8"),
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, UUID.fromString("81dd41e0-d26f-432c-916d-671ccfeaf252"),
            Attributes.defence, UUID.fromString("f3d4c503-d4a3-4224-9255-81adc12bf637")
    ));

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

        // DEBUG
        currentWorldLevel = 20;


        if (currentWorldLevel < 1) {
            return;
        }

        float diff = (e.getWorld().getRandom().nextFloat() - 1) * 4;

        int entityLevel = (int) Math.max(1, currentWorldLevel + diff);
        LivingEntity livingEntity = (LivingEntity) e.getEntity();

        if (scaleLevel(livingEntity::getAttribute, entityLevel)) {

            AttributeInstance attributeInstance = livingEntity.getAttribute(Attributes.defence);
            // minimum defence value for entities
            double minDefenceValue = Math.sqrt(livingEntity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue())
                    * entityLevel;

            // adding defence values
            if (attributeInstance.getBaseValue() < minDefenceValue) {
                attributeInstance.setBaseValue(minDefenceValue);
            }

            // if contains armor attributes
            AttributeInstance armorAttr = livingEntity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
            if (armorAttr != null && armorAttr.getBaseValue() > 0) {
                // scaling up the defence value
                attributeInstance.setBaseValue(attributeInstance.getBaseValue() + armorAttr.getBaseValue());
            }

            // setting  health up to maximum
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

        // adding level modifier
        AttributeModifier modifier = new AttributeModifier(mainLevelModifierId, "level", level, AttributeModifier.Operation.ADDITION);
        attributeInstance.removeModifier(mainLevelModifierId);
        attributeInstance.addPermanentModifier(modifier);

        for (Map.Entry<Attribute, UUID> entry : SCALING_MODIFIERS.get().entrySet()) {
            Attribute attribute = entry.getKey();
            attributeInstance = getAttribute.apply(attribute);
            if (attributeInstance == null)
                continue;

            String lastName = Streams.findLast(Arrays.stream(attribute.getDescriptionId().split("\\."))).orElse(null);
            if (lastName == null)
                continue;

            addModifier(attributeInstance, entry.getValue(), "level_scaling." + lastName, level);
        }

        return true;
    }

    private static boolean addModifier(AttributeInstance instance, UUID id, String name, float level) {
        if (instance != null) {
            instance.removeModifier(id);
            double value = Math.pow(GenshinImpactMod.CONFIG.getKey().levelScaling.get(), level / Attributes.level.getMaxValue());
            instance.addPermanentModifier(new AttributeModifier(id, name, value, AttributeModifier.Operation.MULTIPLY_BASE));
            return true;
        }

        return false;
    }
}
