package com.gim.recipe;

import com.gim.GenshinImpactMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class GenshinRecipeType<T extends Recipe<?>> implements RecipeType<T> {
    private final ResourceLocation loc;

    public GenshinRecipeType(String name) {
        this.loc = new ResourceLocation(GenshinImpactMod.ModID, name);
    }

    @Override
    public String toString() {
        return loc.toString();
    }
}
