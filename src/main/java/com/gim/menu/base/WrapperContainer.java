package com.gim.menu.base;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WrapperContainer implements Container {
    private final Container nullContainer = new SimpleContainer(0);

    private Container inner;

    public WrapperContainer() {
        with(null);
    }

    public WrapperContainer with(Container c) {
        this.inner = c;

        if (inner == null) {
            inner = nullContainer;
        }

        return this;
    }

    @Override
    public int getContainerSize() {
        return inner.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        return inner.getItem(p_18941_);
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        return inner.removeItem(p_18942_, p_18943_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return inner.removeItemNoUpdate(p_18951_);
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {
        inner.setItem(p_18944_, p_18945_);
    }

    @Override
    public void setChanged() {
        inner.setChanged();
    }

    public Container getInner() {
        return inner;
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return inner.stillValid(p_18946_);
    }

    @Override
    public void clearContent() {
        inner.clearContent();
    }
}
