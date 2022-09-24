package com.gim.menu;

import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WishMenu extends AbstractContainerMenu {
    private final ItemStack stack;

    public WishMenu(int containerId, ItemStack stack) {
        super(Menus.wish, containerId);
        this.stack = stack;
    }

    public WishMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
        this(windowId, data.readItem());
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // screen is closed
        if (id == 1) {
            // adding item as award
            player.getInventory().add(getStack());
            // close inventory
            removed(player);
            return true;
        }

        return super.clickMenuButton(player, id);
    }

    public ItemStack getStack() {
        return stack;
    }
}
