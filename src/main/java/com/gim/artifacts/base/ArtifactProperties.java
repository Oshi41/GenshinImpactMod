package com.gim.artifacts.base;

import com.gim.GenshinHeler;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        exp = (int) rarity.minExp;
        this.rarity = rarity;

        // primal
        primal = type.getRandomPrimal(random);

        // adding sub stats
        int end = rarity.getInititalSubstats(random);
        for (int i = 0; i < end; i++) {
            withSub(type.getRandomSub(random, primal));
        }
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
        tag.putString("Primal", withPrimal().name());

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
                .addExp((int) getRarity().minExp, null, null)
                .withPrimal(stat);

        subStats.add(properties);
        return this;
    }

    /**
     * Primal artifact stat
     */
    private ArtifactStat withPrimal() {
        return primal;
    }

    /**
     * Adding experience and perform leveling
     *
     * @param toAdd  - amount of exp to add
     * @param type   - possible slot type. Null to disable leveling
     * @param random - possible random. Null to disable leveling
     * @return this
     */
    public ArtifactProperties addExp(int toAdd, @Nullable ArtifactSlotType type, @Nullable Random random) {
        int oldLevel = getRarity().getLevel(getExp());

        this.exp += toAdd;

        int current = getRarity().getLevel(getExp());

        if (current > oldLevel && type != null && random != null) {
            if (current % 4 == 0) {
                performLeveling(type, random);
            }
        }

        return this;
    }

    private void performLeveling(ArtifactSlotType type, Random random) {
        if (subStats.size() < 4) {
            // adding possible stat
            withSub(type.getRandomSub(random, primal));
        } else {
            ArtifactProperties properties = subStats.get(random.nextInt(subStats.size()));
            int currentLevel = getRarity().getLevel(properties.getExp());
            int toAdd = getRarity().getAmount(currentLevel, currentLevel + 1);
            properties.addExp(toAdd, null, null);
        }
    }

    public ArtifactRarity getRarity() {
        return rarity;
    }

    public int getExp() {
        return exp;
    }

    public void addModifiers(Multimap<Attribute, AttributeModifier> builder) {
        addModifiers(builder, 1);
    }

    /**
     * Applying modifiers to builder
     *
     * @param builder  - builder
     * @param modifier - applying modifier
     */
    private void addModifiers(Multimap<Attribute, AttributeModifier> builder, double modifier) {
        if (primal != null)
            primal.apply(builder, this, modifier);

        for (ArtifactProperties properties : subStats) {
            properties.addModifiers(builder, modifier / 3.4);
        }
    }
}
