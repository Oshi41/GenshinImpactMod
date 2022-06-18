package com.gim.menu.base;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class GenshinContainer implements Container, INBTSerializable<CompoundTag>, SlotListener {
    protected final List<SlotListener> listeners = new ArrayList<>();
    protected final NonNullList<ItemStack> items;

    public GenshinContainer(int p_19150_) {
        this.items = NonNullList.withSize(p_19150_, ItemStack.EMPTY);
    }

    public void addListener(SlotListener p_19165_) {
        this.listeners.add(p_19165_);
    }

    public void removeListener(SlotListener p_19182_) {
        this.listeners.remove(p_19182_);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int p_19157_) {
        return p_19157_ >= 0 && p_19157_ < this.items.size() ? this.items.get(p_19157_) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        if (count < 1)
            return ItemStack.EMPTY;

        if (slotIndex < 0 || slotIndex >= getContainerSize())
            return ItemStack.EMPTY;

        ItemStack prev = items.get(slotIndex).copy();
        ItemStack itemstack = ContainerHelper.removeItem(this.items, slotIndex, count);
        if (!itemstack.isEmpty()) {
            onChange(slotIndex, prev, items.get(slotIndex), true);
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_19180_) {
        ItemStack itemstack = this.items.get(p_19180_);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(p_19180_, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int slotIndex, ItemStack stack) {
        if (slotIndex < 0 || slotIndex >= getContainerSize())
            return;

        ItemStack prev = items.get(slotIndex).copy();
        this.items.set(slotIndex, stack);


        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.onChange(slotIndex, prev, stack, true);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }

    public void onChange(int slotId, ItemStack prev, ItemStack current, boolean safe) {
        if (safe && ItemStack.matches(prev, current)) {
            return;
        }

        for (SlotListener slotListener : listeners) {
            slotListener.onChange(slotId, prev, current);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ContainerHelper.loadAllItems(nbt, items);
    }


    @Override
    public void onChange(int slotId, ItemStack prev, ItemStack current) {
        onChange(slotId, prev, current, true);
    }
}
