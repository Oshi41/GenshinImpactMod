package com.gim.menu.base;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.google.common.collect.Iterators;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GenshinIterableMenuBase extends AbstractContainerMenu implements ContainerListener {
    protected final ContainerData containerData;
    protected Inventory playerInv;
    protected ContainerLevelAccess access;

    protected int firstPlayerSlot;
    protected int lastPlayerSlot;

    protected GenshinIterableMenuBase(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(menuType, containerID);
        this.playerInv = playerInv;
        this.access = access;

        containerData = new SimpleContainerData(1);
        addDataSlots(containerData);
    }

    public void changeIndex(int newIndex) {
        setData(0, newIndex);
    }

    /**
     * Returns current character
     *
     * @return
     */
    @Nullable
    public GenshinEntityData current() {
        int index = this.containerData.get(0);

        if (playerInv != null && playerInv.player != null) {
            IGenshinInfo genshinInfo = playerInv.player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (genshinInfo != null) {
                Collection<IGenshinPlayer> playerCollection = genshinInfo.getAllPersonages();
                index = Mth.clamp(index, 0, playerCollection.size() - 1);
                IGenshinPlayer player = Iterators.get(playerCollection.iterator(), index);
                return genshinInfo.getPersonInfo(player);
            }
        }

        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((p_38916_, p_38917_) -> checkBlock(p_38916_.getBlockState(p_38917_).getBlock())
                        && player.distanceToSqr(
                        (double) p_38917_.getX() + 0.5D,
                        (double) p_38917_.getY() + 0.5D,
                        (double) p_38917_.getZ() + 0.5D) <= 64.0D,
                true);
    }

    /**
     * Returns current index of player
     */
    public int getIndex() {
        return containerData.get(0);
    }

    /**
     * Checks for same block on position
     */
    protected abstract boolean checkBlock(Block pos);

    public abstract void refreshByIndex();

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int slotId, int value) {

        if (slotId == 0) {
            refreshByIndex();
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu p_39315_, int p_39316_, ItemStack p_39317_) {

    }

    protected void drawPlayersSlots(int barX, int barY) {
        drawPlayersSlots(barX, barY, barX, barY + 4 + 18 * 3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotId < firstPlayerSlot) {
                if (!this.moveItemStackTo(itemstack1, firstPlayerSlot + 1, lastPlayerSlot, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (this.moveItemStackTo(itemstack1, 0, firstPlayerSlot - 1, false)) { //Forge Fix Shift Clicking in beacons with stacks larger then 1.
                return ItemStack.EMPTY;
            } else if (slotId > firstPlayerSlot && slotId < lastPlayerSlot - 9) {
                if (!this.moveItemStackTo(itemstack1, lastPlayerSlot - 9, lastPlayerSlot, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotId >= lastPlayerSlot - 9 && slotId <= lastPlayerSlot) {
                if (!this.moveItemStackTo(itemstack1, firstPlayerSlot, lastPlayerSlot - 9, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, firstPlayerSlot, lastPlayerSlot, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);

            Container container = null;

            if (slot.container instanceof WrapperContainer) {
                container = ((WrapperContainer) slot.container).getInner();
            }

            if (container instanceof SlotListener) {
                ((SlotListener) slot.container).onChange(slotId, itemstack, itemstack1);
            }
        }

        return itemstack;
    }

    /**
     * Adding player slots
     *
     * @param barX    - start of bar slots X
     * @param barY    - start of bar slots Y*
     * @param hotbarX - where hotbar starts X. Usually its barX + 4 + 18*3
     * @param hotbarY - where hotbar starts Y. Usually same as barY
     */
    protected void drawPlayersSlots(int barX, int barY, int hotbarX, int hotbarY) {

        firstPlayerSlot = slots.size();

        for (int barIndex = 0; barIndex < 3; ++barIndex) {
            for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
                this.addSlot(new Slot(playerInv, slotIndex + barIndex * 9 + 9, barX + slotIndex * 18, barY + barIndex * 18));
            }
        }

        for (int hotSlotIndex = 0; hotSlotIndex < 9; ++hotSlotIndex) {
            this.addSlot(new Slot(playerInv, hotSlotIndex, hotbarX + hotSlotIndex * 18, hotbarY));
        }

        lastPlayerSlot = slots.size() - 1;
    }
}
