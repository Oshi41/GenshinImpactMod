package com.gim.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShieldEntity extends ItemEntity {
    public ShieldEntity(Level level, double x, double y, double z, ItemStack stack, double xMov, double yMov, double zMov) {
        super(level, x, y, z, stack, xMov, yMov, zMov);
    }
}
