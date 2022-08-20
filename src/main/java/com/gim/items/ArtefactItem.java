package com.gim.items;

import com.gim.GenshinHeler;
import com.gim.artifacts.base.*;
import com.gim.registry.CreativeTabs;
import com.gim.registry.Registries;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArtefactItem extends Item {
    private static final String tagName = "ArtifactData";
    private final ArtifactSlotType type;
    private final Lazy<List<IArtifactSet>> sets;

    public ArtefactItem(ArtifactSlotType type) {
        super(new Properties()
                .tab(CreativeTabs.ARTIFACTS)
                .setNoRepair()
                .fireResistant()
                .stacksTo(1));

        this.type = type;
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
        components.add(new TranslatableComponent("gim.level", properties.getRarity().getLevel(properties.getExp())));

        components.add(TextComponent.EMPTY);
        components.add(new TranslatableComponent("item.modifiers.artifacts").withStyle(ChatFormatting.GRAY));

        List<Component> from = GenshinHeler.from(getAttributeModifiers(null, stack));
        from.add(2, TextComponent.EMPTY);
        components.addAll(from);

        List<IArtifactSet> setList = sets.get();
        if (!setList.isEmpty()) {
            components.add(TextComponent.EMPTY);
            components.add(new TranslatableComponent("genshin.set_bonus").withStyle(ChatFormatting.GRAY));

            for (IArtifactSet artifactSet : setList) {
                components.add(artifactSet.name().plainCopy().withStyle(ChatFormatting.WHITE));
                components.add(artifactSet.description().plainCopy().withStyle(ChatFormatting.DARK_GREEN));
                components.add(TextComponent.EMPTY);
            }
        }
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        if (!allowdedIn(tab))
            return;

        for (ArtifactStat primal : type.getPrimal().keySet()) {
            List<ArtifactStat> subStats = type.getSub().keySet().stream().filter(x -> x != primal).collect(Collectors.toList());

            while (!subStats.isEmpty()) {
                ArtifactProperties props = new ArtifactProperties()
                        .withPrimal(primal)
                        .withRarity(ArtifactRarity.FIVE)
                        .addExp((int) ArtifactRarity.FIVE.getMinExp());

                List<ArtifactStat> currentSubstats = subStats.stream().limit(4).toList();
                currentSubstats.forEach(props::withSub);
                subStats.removeAll(currentSubstats);

                ItemStack stack = getDefaultInstance();
                this.save(stack, props);
                stacks.add(stack);
            }
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

        // our special value
        if (slot == null) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(tagName);
            Multimap<Attribute, AttributeModifier> builder = LinkedHashMultimap.create();
            new ArtifactProperties(tag).addModifiers(builder, getType());
            return builder;
        }

        return super.getAttributeModifiers(slot, stack);
    }

    @Nullable
    public ArtifactProperties from(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag().getCompound(tagName);
        if (compoundTag.isEmpty())
            return null;

        return new ArtifactProperties(compoundTag);
    }

    public void save(ItemStack stack, ArtifactProperties props) {
        if (!stack.isEmpty() && props != null) {
            stack.getOrCreateTag().put(tagName, props.serializeNBT());
        }
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
