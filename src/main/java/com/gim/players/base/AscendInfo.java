package com.gim.players.base;

import com.gim.GenshinImpactMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AscendInfo {
    public final NonNullList<ItemStack> materials;
    public final List<Component> info = new ArrayList<>();
    public final int playerLevels;
    public final long ticksTillLevel;

    public AscendInfo(int level, int playerLevels, @Nullable Attribute specialAttribute, @Nullable Component add, ItemStack... materials) {
        this.playerLevels = playerLevels;
        ticksTillLevel = (level + 1) * GenshinImpactMod.CONFIG.getKey().levelUpTime.get() * 60L * 24;
        if (level >= com.gim.registry.Attributes.level.getMaxValue()) {
            this.materials = NonNullList.create();
            info.add(new TextComponent("MAX").withStyle(ChatFormatting.DARK_GREEN));
            return;
        }

        this.materials = NonNullList.of(ItemStack.EMPTY, Arrays.stream(materials).filter(x -> x != null && !x.isEmpty()).toArray(ItemStack[]::new));

        info.add(new TranslatableComponent(GenshinImpactMod.ModID + ".level.upgrading", level, level + 1));

        List<Attribute> stats = Stream.of(Attributes.MAX_HEALTH, Attributes.ARMOR, Attributes.ATTACK_DAMAGE, specialAttribute)
                .filter(Objects::nonNull)
                .toList();

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
