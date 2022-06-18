package com.gim.menu.base;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotListener {
    void onChange(int slotId, ItemStack prev, ItemStack current);
}
