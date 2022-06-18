package com.gim.items;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactProperties;
import com.gim.artifacts.base.ArtifactRarity;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.artifacts.base.IArtifactSet;
import com.gim.registry.CreativeTabs;
import com.gim.registry.Registries;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArtefactItem extends Item {
    private static final String tagName = "ArtifactData";
    private final ArtifactSlotType type;
    private final Lazy<CompoundTag> defaultProps;
    private final Lazy<List<IArtifactSet>> sets;

    public ArtefactItem(ArtifactSlotType type) {
        super(new Properties()
                .tab(CreativeTabs.ARTIFACTS)
                .setNoRepair()
                .fireResistant()
                .stacksTo(1));

        this.type = type;
        defaultProps = Lazy.of(() -> new ArtifactProperties(ArtifactRarity.FIVE, this.type, new Random()).serializeNBT());
        sets = Lazy.of(() -> Registries.artifacts().getValues().stream().filter(x -> x.partOf(this)).toList());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        CompoundTag tag = stack.getOrCreateTag().getCompound(tagName);
        if (tag.isEmpty())
            return;

        ArtifactProperties properties = new ArtifactProperties(tag);
        int count = Arrays.stream(ArtifactRarity.values()).toList().indexOf(properties.getRarity()) + 1;
        components.add(new TextComponent("✯".repeat(count)));

        components.add(TextComponent.EMPTY);
        components.add(new TranslatableComponent("item.modifiers.artifacts").withStyle(ChatFormatting.GRAY));

        Multimap<Attribute, AttributeModifier> modifiers = getAttributeModifiers(null, stack);

        int i = 0;

        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            AttributeModifier attributemodifier = entry.getValue();
            double d0 = attributemodifier.getAmount();
            boolean flag = false;

            double d1;
            if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                    d1 = d0 * 10.0D;
                } else {
                    d1 = d0;
                }
            } else {
                d1 = d0 * 100.0D;
            }

            if (i == 0) {
                components.add(new TranslatableComponent(GenshinImpactMod.ModID + ".main_stat"));
            }

            if (flag) {
                components.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            } else if (d0 > 0.0D) {
                components.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0D) {
                d1 *= -1.0D;
                components.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.RED));
            }

            if (i == 0) {
                components.add(TextComponent.EMPTY);
            }

            i++;
        }

        List<IArtifactSet> setList = sets.get();
        if (!setList.isEmpty()) {
            components.add(TextComponent.EMPTY);
            components.add(new TranslatableComponent("genshin.set_bonus").withStyle(ChatFormatting.GRAY));

            for (IArtifactSet artifactSet : setList) {
                components.add(artifactSet.name().withStyle(ChatFormatting.WHITE));
                components.add(artifactSet.description().withStyle(ChatFormatting.BLACK));
            }
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().put(tagName, defaultProps.get());
        return stack;
    }

    public void fillItemCategory(CreativeModeTab p_41391_, NonNullList<ItemStack> p_41392_) {
        if (this.allowdedIn(p_41391_)) {
            p_41392_.add(getDefaultInstance());
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

        // our special value
        if (slot == null) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(tagName);
            Multimap<Attribute, AttributeModifier> builder = LinkedHashMultimap.create();
            new ArtifactProperties(tag).addModifiers(builder);
            return builder;
        }

        return super.getAttributeModifiers(slot, stack);
    }

    public ItemStack create(ArtifactProperties props) {
        ItemStack stack = getDefaultInstance();
        stack.getOrCreateTag().put(tagName, props.serializeNBT());
        return stack;
    }

    public ArtifactSlotType getType() {
        return type;
    }
}
