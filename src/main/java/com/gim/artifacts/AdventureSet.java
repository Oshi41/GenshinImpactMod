package com.gim.artifacts;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactSetBase;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import com.gim.registry.Items;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdventureSet extends ArtifactSetBase {
    private static final Lazy<ImmutableMultimap<Attribute, AttributeModifier>> healthAttrMap = Lazy.of(() -> {
        UUID healthIncrease = UUID.fromString("0e2b4a35-6027-4ced-b5b0-f8d2c0f5f258");
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.MAX_HEALTH, new AttributeModifier(healthIncrease, "adventure_health_increase", 70, AttributeModifier.Operation.ADDITION));
        ImmutableMultimap<Attribute, AttributeModifier> map = builder.build();
        return map;
    });

    private static final Lazy<List<Block>> healingBlocks = Lazy.of(() -> List.of(Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS));

    private int count;
    private final Lazy<Set<Item>> possible;

    public AdventureSet(int count) {
        super(
                new TranslatableComponent(String.format("%s.artifact_set.adventure%s.name", GenshinImpactMod.ModID, count)),
                new TranslatableComponent(String.format("%s.artifact_set.adventure%s.description", GenshinImpactMod.ModID, count))
        );
        this.count = count;
        possible = Lazy.of(() -> Set.of(Items.adventure_clock, Items.adventure_crown, Items.adventure_cup, Items.adventure_feather, Items.adventure_flower));

        if (count >= 4) {
            subscribeIfActive(BlockEvent.BreakEvent.class, breakEvent -> getCurrent(breakEvent.getPlayer()), this::handleBreak);
        }
    }

    private void handleBreak(BlockEvent.BreakEvent event, GenshinEntityData data) {
        if (healingBlocks.get().contains(event.getState().getBlock())) {
            event.getPlayer().heal(event.getPlayer().getMaxHealth() * 0.2f);
        }
    }

    @Override
    public void onWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
        super.onWearing(holder, info, data);

        if (count >= 2) {
            data.getAttributes().addTransientAttributeModifiers(healthAttrMap.get());
        }
    }

    @Override
    public void onTakeOff(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
        super.onTakeOff(holder, info, data);

        if (count >= 2) {
            data.getAttributes().removeAttributeModifiers(healthAttrMap.get());
            double maxHealth = data.getAttributes().getValue(Attributes.MAX_HEALTH);
            if (maxHealth < data.getHealth()) {
                data.setHealth(holder, ((float) maxHealth));
            }
        }
    }

    @Override
    public boolean isWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
        return isWearing(data.getArtifactsContainer(), possible.get(), count);
    }

    @Override
    public boolean partOf(Item item) {
        return possible.get().contains(item);
    }
}
