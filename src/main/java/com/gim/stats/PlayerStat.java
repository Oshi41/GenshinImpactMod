package com.gim.stats;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerStat {
    public List<AttributeInstance> Attributes = new ArrayList<>();
    public MobEffect Element;
    public List<ItemStack> UpgradeItems = new ArrayList<>();
}
