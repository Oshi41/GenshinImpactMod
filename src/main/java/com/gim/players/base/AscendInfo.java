package com.gim.players.base;

import com.gim.GenshinImpactMod;
import com.gim.events.LevelScaling;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class AscendInfo {
    public final NonNullList<ItemStack> materials;
    public final List<Component> info = new ArrayList<>();
    public final int playerLevels;
    public final long ticksTillLevel;

    public AscendInfo(@Nullable AttributeMap map, int level, int playerLevels, @Nullable Attribute specialAttribute, @Nullable Component add, ItemStack... materials) {
        this.playerLevels = playerLevels;
        // based on world level up time but 10 times faster
        ticksTillLevel = (level + 1) * GenshinImpactMod.CONFIG.getKey().levelUpTime.get() * 60L * 24 / 10;

        // all characters increases this stat
        List<Attribute> stats = new ArrayList<>(LevelScaling.SCALING_MODIFIERS.get().keySet());
        if (specialAttribute != null) {
            stats.add(specialAttribute);
        }

        // already at max level
        if (level >= com.gim.registry.Attributes.level.getMaxValue()) {
            // no materials
            this.materials = NonNullList.create();
            // show only MAX LEVEL
            info.add(new TranslatableComponent(GenshinImpactMod.ModID + ".max_level").withStyle(ChatFormatting.DARK_GREEN));
            return;
        }

        // adding materials
        this.materials = NonNullList.of(ItemStack.EMPTY, Arrays.stream(materials).filter(x -> x != null && !x.isEmpty()).toArray(ItemStack[]::new));

        // level scaling, for exapmle 2 --> 3
        info.add(new TranslatableComponent(GenshinImpactMod.ModID + ".level", String.format("%s --> %s", level, level + 1)));


        // using same modifier for all attributes, need to show them
        for (Attribute attribute : stats) {
            Double scale = GenshinImpactMod.CONFIG.getKey().levelScaling.get();

            Component component = new TextComponent("+").withStyle(ChatFormatting.YELLOW)
                    .append(new TranslatableComponent("attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_BASE.toValue(),
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(scale),
                            new TranslatableComponent(attribute.getDescriptionId()))
                            .withStyle(ChatFormatting.YELLOW));

            info.add(component);
        }

        if (add != null) {
            info.add(add);
        }
    }
}
