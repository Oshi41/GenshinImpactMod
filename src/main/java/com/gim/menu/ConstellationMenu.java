package com.gim.menu;

import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.items.ConstellationItem;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ConstellationMenu extends GenshinIterableMenuBase {
    private final Container own = new SimpleContainer(1);

    public ConstellationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.constellation, containerID, playerInv, access);

        this.addSlot(new Slot(own, 0, 8, 139) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return super.mayPlace(itemStack) && itemStack.getItem() instanceof ConstellationItem;
            }
        });

        drawPlayersSlots(8, 175);
    }

    public ConstellationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    @Override
    protected boolean checkBlock(Block pos) {
        return pos instanceof GenshinCraftingTableBlock;
    }

    @Override
    public void refreshByIndex() {
        // ignored
    }
}
