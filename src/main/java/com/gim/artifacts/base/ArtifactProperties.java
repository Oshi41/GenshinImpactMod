package com.gim.artifacts.base;

import com.gim.GenshinHeler;
import com.gim.items.ArtefactItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ArtifactProperties implements INBTSerializable<CompoundTag> {
    private ArtifactStat primal;
    private final List<ArtifactProperties> subStats = new ArrayList<>();
    private ArtifactRarity rarity;
    private int exp;

    /**
     * Generating random artifact
     *
     * @param rarity - current rarity
     * @param type   - slot type
     */
    public ArtifactProperties(ArtifactRarity rarity, ArtifactSlotType type, Random random) {
        exp = (int) rarity.getMinExp();
        this.rarity = rarity;

        // primal
        primal = type.getRandomPrimal(random);

        // adding sub stats
        ArtifactStat randomSub = type.getRandomSub(random, primal, subStats.stream().map(ArtifactProperties::getPrimal));
        withSub(randomSub);
    }

    /**
     * Default ctor
     */
    public ArtifactProperties() {

    }

    public ArtifactProperties(CompoundTag tag) {
        deserializeNBT(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Rarity", rarity.name());
        tag.putInt("Exp", exp);
        tag.putString("Primal", getPrimal().name());

        ListTag keys = new ListTag();
        tag.put("Stats", keys);
        subStats.forEach(x -> keys.add(x.serializeNBT()));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        subStats.clear();

        withRarity(GenshinHeler.safeGet(ArtifactRarity.class, nbt.getString("Rarity")));
        exp = nbt.getInt("Exp");
        primal = GenshinHeler.safeGet(ArtifactStat.class, nbt.getString("Primal"));

        Tag raw = nbt.get("Stats");
        if (raw instanceof ListTag) {
            ListTag attributes = (ListTag) raw;
            attributes.stream()
                    .filter(x -> x instanceof CompoundTag)
                    .map(x -> ((CompoundTag) x))
                    .map(ArtifactProperties::new)
                    .forEach(subStats::add);
        }
    }

    /**
     * Set current rarity for item
     */
    public ArtifactProperties withRarity(ArtifactRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    /**
     * Adding modifier
     */
    public ArtifactProperties withPrimal(ArtifactStat stat) {
        primal = stat;
        return this;
    }

    /**
     * Adding substat for current artifact
     *
     * @param stat - substat
     * @return
     */
    public ArtifactProperties withSub(ArtifactStat stat) {
        ArtifactProperties properties = new ArtifactProperties()
                .withRarity(getRarity())
                // adding init level
                .addExp((int) getRarity().getMinExp(), null, null, null)
                .withPrimal(stat);

        subStats.add(properties);
        return this;
    }

    /**
     * Primal artifact stat
     */
    public ArtifactStat getPrimal() {
        return primal;
    }

    /**
     * Adding experience and perform leveling
     *
     * @param toAdd  - amount of exp to add
     * @param type   - possible slot type. Null to disable leveling
     * @param random - possible random. Null to disable leveling
     * @param stack  - possible itemstack to save levelling. Can bu null. Null value doe not disable levelling
     * @return this
     */
    public ArtifactProperties addExp(int toAdd, @Nullable ArtifactSlotType type, @Nullable Random random, @Nullable ItemStack stack) {
        // saving old level
        int oldLevel = getRarity().getLevel(getExp());

        // adding exp to artifact
        this.exp = (int) Math.min(getRarity().getMaxExp(), this.exp + toAdd);

        // retrieving new level
        int current = getRarity().getLevel(getExp());

        // if level was changed
        if (current > oldLevel && type != null && random != null) {
            // iterating through all levels
            for (int i = oldLevel + 1; i <= current; i++) {
                // if
                if (i % 4 == 0) {
                    addSubStat(type, random);
                }
            }
        }

        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ArtefactItem) {
            ((ArtefactItem) stack.getItem()).save(stack, this);
        }

        return this;
    }

    private void addSubStat(ArtifactSlotType type, Random random) {
        if (subStats.size() < 4) {
            ArtifactStat randomSub = type.getRandomSub(random, primal, subStats.stream().map(ArtifactProperties::getPrimal));
            withSub(randomSub);
        } else {
            ArtifactProperties properties = subStats.get(random.nextInt(subStats.size()));
            int currentLevel = getRarity().getLevel(properties.getExp());
            int toAdd = getRarity().getAmount(currentLevel, currentLevel + 1);
            properties.addExp(toAdd, null, null, null);
        }
    }

    public ArtifactRarity getRarity() {
        return rarity;
    }

    public int getExp() {
        return exp;
    }

    /**
     * Returns amount of sub stats
     */
    public ImmutableList<ArtifactProperties> getSubModifiers() {
        return ImmutableList.copyOf(subStats);
    }

    public void addModifiers(Multimap<Attribute, AttributeModifier> builder, ArtifactSlotType type) {
        addModifiers(builder, 1, type);
    }

    /**
     * Applying modifiers to builder
     *
     * @param builder  - builder
     * @param modifier - applying modifier
     * @param type
     */
    private void addModifiers(Multimap<Attribute, AttributeModifier> builder, double modifier, ArtifactSlotType type) {
        if (primal != null)
            primal.apply(builder, this, modifier, type);

        for (ArtifactProperties properties : subStats) {
            properties.addModifiers(builder, modifier / ArtifactStat.SUB_STAT_MODIFIER, type);
        }
    }


}
