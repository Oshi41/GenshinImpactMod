package com.gim.menu;

import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.items.ConstellationItem;
import com.gim.registry.Menus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ConstellationMenu extends AbstractContainerMenu {
    private final Inventory playerInv;
    private final Player player;

    private final ContainerLevelAccess access;
    private final Container own;

    private ConstellationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.constellation, containerID);
        this.playerInv = playerInv;
        this.player = playerInv.player;
        this.access = access;
        own = new SimpleContainer(1);

        this.addSlot(new Slot(own, 0, 8, 139) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return super.mayPlace(itemStack) && itemStack.getItem() instanceof ConstellationItem;
            }
        });

        int xStart = 8;
        int yStart = 175;

        for (int barIndex = 0; barIndex < 3; ++barIndex) {
            for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
                this.addSlot(new Slot(playerInv, slotIndex + barIndex * 9 + 9, xStart + slotIndex * 18, yStart + barIndex * 18));
            }
        }

        // 3 bars + 4 spacing
        yStart += 18 * 3 + 4;

        for (int hotSlotIndex = 0; hotSlotIndex < 9; ++hotSlotIndex) {
            this.addSlot(new Slot(playerInv, hotSlotIndex, xStart + hotSlotIndex * 18, yStart));
        }
    }

    public ConstellationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    public ConstellationMenu(int containerID, Inventory playerInv, Player player, BlockState blockState, Level level, BlockHitResult hitResult, BlockPos blockPos) {
        this(containerID, playerInv, ContainerLevelAccess.create(level, blockPos));
    }

    @Override
    public boolean stillValid(Player other) {
        return stillValid(access, other);
    }

    protected static boolean stillValid(ContainerLevelAccess p_38890_, Player p_38891_) {
        return p_38890_.evaluate((p_38916_, p_38917_) -> p_38916_.getBlockState(p_38917_).getBlock() instanceof GenshinCraftingTableBlock && p_38891_
                        .distanceToSqr((double) p_38917_.getX() + 0.5D, (double) p_38917_.getY() + 0.5D, (double) p_38917_.getZ() + 0.5D) <= 64.0D,
                true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, own));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotId == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (this.moveItemStackTo(itemstack1, 0, 1, false)) { //Forge Fix Shift Clicking in beacons with stacks larger then 1.
                return ItemStack.EMPTY;
            } else if (slotId >= 1 && slotId < 28) {
                if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotId >= 28 && slotId < 37) {
                if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
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
        }

        return itemstack;
    }
}
