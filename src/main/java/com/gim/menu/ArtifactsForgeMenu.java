package com.gim.menu;

import com.gim.artifacts.base.ArtifactProperties;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.artifacts.base.ArtifactStat;
import com.gim.items.ArtefactItem;
import com.gim.menu.base.GenshinContainer;
import com.gim.menu.base.GenshinMenuBase;
import com.gim.registry.Blocks;
import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ArtifactsForgeMenu extends GenshinMenuBase {
    private final GenshinContainer own = new GenshinContainer(7);
    private final ContainerData data = new SimpleContainerData(10);

    public ArtifactsForgeMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    public ArtifactsForgeMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.artifacts_forge, containerID, playerInv, access);

        this.addDataSlots(data);

        for (int i = 0; i < 7; i++) {
            int x = 8 + (Math.max(0, i - 1) * 18);
            int y = i == 0 ? 90 : 112;

            addSlot(new Slot(own, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    // not an artifact
                    if (!(itemStack.getItem() instanceof ArtefactItem)) {
                        return false;
                    }

                    // if artifact can be upgraded
                    if (getArtifactExpToMaxLevel() > 0
                            // have any items to consume
                            && getApplyingExp() > 0
                            // if already can upgrade to max level
                            && getArtifactExpToMaxLevel() <= getArtifactExp() + getApplyingExp()) {
                        return false;
                    }

                    return true;
                }
            });
        }

        // draw player slots
        drawPlayersSlots(8, 134);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((p_39371_, p_39372_) -> {
            this.clearContainer(player, own);
        });
    }

    @Override
    public void slotChanged(AbstractContainerMenu p_39315_, int slotIndex, ItemStack p_39317_) {
        // any artifact possible
        if (own.getItem(0).isEmpty()) {
            setSlotType(null);
            // if putting very first
        } else if (getSlotType() == null) {

            ItemStack itemStack = own.getItem(0);
            if (itemStack.getItem() instanceof ArtefactItem) {
                // finding very first artifact and detecting its
                setSlotType(((ArtefactItem) itemStack.getItem()).getType());
            }
        }

        // upgrading item
        if (slotIndex == 0) {
            setArtifactExp(-1);
            setArtifactLevel(-1);
            setArtifactExpToNextLevel(-1);
            setArtifactExpToMaxLevel(-1);

            ItemStack upgradingItem = own.getItem(slotIndex);
            // have any artifact to upgrade
            if (!upgradingItem.isEmpty()) {
                ArtefactItem artefactItem = (ArtefactItem) upgradingItem.getItem();
                ArtifactProperties properties = artefactItem.from(upgradingItem);
                // have propertly nbt tags
                if (properties != null) {
                    // getting artifact level
                    setArtifactLevel(properties.getRarity().getLevel(properties.getExp()));
                    // if can upgrade artifact (not max level)
                    if (getArtifactLevel() < properties.getRarity().getMaxLevel()) {
                        // current artifact level count reminder from artifact level
                        // imagine, artifact with ✯✯✯✯✯, level 5
                        // we need A exp for upgrading from 0 to 5 level
                        // and if artifact containing more than A exp,
                        // put here amount above level
                        setArtifactExp(properties.getRarity().getXpAmountForLevel(properties.getExp()));

                        // how much exp we need to upgrade from artifact level to next one
                        setArtifactExpToNextLevel(properties.getRarity().getXpForLevel(getArtifactLevel() + 1));

                        // max possible upgrade amount
                        setArtifactExpToMaxLevel(properties.getRarity().getAmount(getArtifactLevel(), properties.getRarity().getMaxLevel()));
                    }
                }
            }
            // if consumable artifact slots changes
        } else if (slotIndex < own.getContainerSize()) {
            setApplyingExp(-1);
            setNeededPlayerExpLevels(-1);
            setApplyingLevels(-1);

            for (int i = 1; i < own.getContainerSize(); i++) {
                ItemStack itemStack = own.getItem(i);
                if (!itemStack.isEmpty()) {
                    ArtifactProperties properties = ((ArtefactItem) itemStack.getItem()).from(itemStack);
                    // artifacts consume only 80% of value
                    setApplyingExp((int) (getApplyingExp() + properties.getExp() * 0.8));
                }
            }

            // if currently can upgrade item
            if (getApplyingExp() > 0 && getArtifactExpToNextLevel() > 0) {
                // adding xp levels for player
                setNeededPlayerExpLevels((int) Math.ceil(getApplyingExp() / 500.));

                // if upgrading to next levels
                if (getApplyingExp() + getArtifactExp() >= getArtifactExpToNextLevel()) {
                    // some null checks
                    if (!own.getItem(0).isEmpty()) {
                        ArtifactProperties properties = ((ArtefactItem) own.getItem(0).getItem()).from(own.getItem(0));

                        // current applying exp
                        int totalApplying = getApplyingExp() - getArtifactExpToNextLevel() + getArtifactExp();
                        int totalLevels = 1;
                        int maxLevelScale = properties.getRarity().getMaxLevel() - getArtifactLevel();


                        while (totalApplying > 0 && totalLevels <= maxLevelScale) {
                            // xp for next level
                            int xpForLevel = properties.getRarity().getXpForLevel(getArtifactLevel() + totalLevels + 1);
                            // if we're applying more/equal than level capacity
                            if (xpForLevel <= totalApplying) {
                                // adding current level as possible yo upgrade
                                totalLevels++;
                            }

                            // subtracting  exp amount for level
                            totalApplying -= xpForLevel;
                        }

                        // can't go futher than max level, so need to be limited
                        totalLevels = Math.min(totalLevels, maxLevelScale);

                        setApplyingLevels(totalLevels);
                    }
                }
            }
        }


        setCanApply((getNeededPlayerExpLevels() <= playerInv.player.experienceLevel || playerInv.player.isCreative())
                && getApplyingExp() > 0
                && getArtifactExpToNextLevel() > 0);
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        // xp is enough
        if ((player.experienceLevel >= getNeededPlayerExpLevels() || player.isCreative())
                // contains artifact for upgrading
                && !own.getItem(0).isEmpty()
                // have exp to upgrade
                && getApplyingExp() > 0) {


            float random = player.getRandom().nextFloat();
            int toApply = getApplyingExp();
            if (random < 0.01) {
                toApply *= 5;
                setMultiplier(5);
            } else if (random < 0.1) {
                toApply *= 2;
                setMultiplier(2);
            } else {
                setMultiplier(0);
            }

            ItemStack artifact = own.getItem(0);
            ArtefactItem artifactItem = (ArtefactItem) artifact.getItem();
            ArtifactProperties properties = artifactItem.from(artifact);
            if (properties != null) {
                Map<ArtifactStat, Integer> result = properties.addExp(toApply, artifactItem.getType(), player.getRandom(), artifact);

                // extracting xp levels
                if (!player.isCreative())
                    player.giveExperienceLevels(-getNeededPlayerExpLevels());

                // removing all artifacts
                for (int i = 1; i < own.getContainerSize(); i++) {
                    own.setItem(i, ItemStack.EMPTY);
                }

                slotChanged(this, 0, getSlot(0).getItem());

                return true;
            }
        }

        return true;
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return state.getBlock().equals(Blocks.artifacts_forge);
    }

    @Nullable
    public ArtifactSlotType getSlotType() {
        int index = data.get(0);
        if (index < 0) {
            return null;
        }

        return ArtifactSlotType.values()[index];
    }

    public void setSlotType(@Nullable ArtifactSlotType slotType) {
        setData(0, slotType != null ? slotType.ordinal() : -1);
    }

    /**
     * Current artifact level
     */
    public int getArtifactLevel() {
        return data.get(1);
    }

    public void setArtifactLevel(int artifactLevel) {
        setData(1, artifactLevel);
    }

    /**
     * Artifact xp for current level
     */
    public int getArtifactExp() {
        return this.data.get(2);
    }

    public void setArtifactExp(int artifactExp) {
        setData(2, artifactExp);
    }

    /**
     * Xp amount to achieve next level
     */
    public int getArtifactExpToNextLevel() {
        return data.get(3);
    }

    public void setArtifactExpToNextLevel(int artifactExpToNextLevel) {
        setData(3, artifactExpToNextLevel);
    }

    public int getArtifactExpToMaxLevel() {
        return data.get(4);
    }

    public void setArtifactExpToMaxLevel(int artifactExpToMaxLevel) {
        setData(4, artifactExpToMaxLevel);
    }

    /**
     * Amount of xp to upgrade artifact
     */
    public int getApplyingExp() {
        return data.get(5);
    }

    public void setApplyingExp(int applyingExp) {
        setData(5, applyingExp);
    }

    /**
     * Player exp needed to upgrade the artifact
     */
    public int getNeededPlayerExpLevels() {
        return data.get(6);
    }

    public void setNeededPlayerExpLevels(int neededPlayerExpLevels) {
        setData(6, neededPlayerExpLevels);
    }

    public boolean isCanApply() {
        return data.get(7) > 0;
    }

    public void setCanApply(boolean canApply) {
        setData(7, canApply ? 1 : 0);
    }

    public int getApplyingLevels() {
        return data.get(8);
    }

    public void setApplyingLevels(int levels) {
        data.set(8, levels);
    }

    /**
     * Returns exp applying multiplier
     */
    public int getMultiplier() {
        return data.get(9);
    }

    public void setMultiplier(int val) {
        setData(9, val);
    }
}
