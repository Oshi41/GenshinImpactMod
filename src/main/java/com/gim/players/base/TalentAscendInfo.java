package com.gim.players.base;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @param materials         - talent scale material
 * @param info              - info for current scaling
 * @param expLevel          - needed EXP level
 * @param minCharacterLevel - needed character level
 */
public record TalentAscendInfo(NonNullList<ItemStack> materials, List<MutableComponent> info, int expLevel,
                               int minCharacterLevel, List<MutableComponent> skillsInfo) {
}
