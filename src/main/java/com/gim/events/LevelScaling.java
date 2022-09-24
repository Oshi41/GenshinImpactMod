package com.gim.events;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

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
    public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn e) {
        // check type
        if (!(e.getEntity() instanceof LivingEntity livingEntity) || !GenshinHeler.acceptGenshinEffects(e.getEntity()))
            return;

        // find nearest player
        LevelAccessor world = e.getWorld();
        // !!! possible bottleneck !!!
        Player nearestPlayer = world.getNearestPlayer(e.getEntity(), 16);
        if (nearestPlayer == null)
            return;

        // getting player level
        double level = GenshinHeler.safeGetAttribute(nearestPlayer, Attributes.level);
        if (level < 1)
            return;

        // level can be 15% lower and 5% higher than player level
        double nextLevel = Math.ceil(level - (e.getWorld().getRandom().nextDouble(20) - 5) / 100 * level);
        if (nextLevel < 1)
            return;

        scaleLevel(livingEntity::getAttribute, nextLevel, livingEntity);
    }

    /**
     * Perform entity level scaling
     *
     * @param getAttribute - attribute getter
     * @param level        - entity level
     * @return - success of operation
     */
    public static boolean scaleLevel(Function<Attribute, AttributeInstance> getAttribute, double level, @Nullable LivingEntity livingEntity) {
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

        if (livingEntity != null) {
            attributeInstance = livingEntity.getAttribute(Attributes.defence);
            // minimum defence value for entities
            double minDefenceValue = Math.sqrt(livingEntity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue())
                    * level;

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

        return true;
    }

    /**
     * Safe adding modifier
     * uses MULTIPLY_BASE operation
     *
     * @param instance - attribute instance
     * @param id       - modifier unique ID
     * @param name     - modifier name
     * @param level    - modifier level
     */
    private static boolean addModifier(AttributeInstance instance, UUID id, String name, double level) {
        if (instance != null) {
            instance.removeModifier(id);
            double value = Math.pow(GenshinImpactMod.CONFIG.getKey().levelScaling.get(), level / Attributes.level.getMaxValue());
            instance.addPermanentModifier(new AttributeModifier(id, name, value, AttributeModifier.Operation.MULTIPLY_BASE));
            return true;
        }

        return false;
    }
}
