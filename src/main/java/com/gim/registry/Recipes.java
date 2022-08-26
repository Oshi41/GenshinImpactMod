package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.recipe.GenshinRecipeType;
import com.gim.recipe.ParametricTransformerRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(GenshinImpactMod.ModID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Recipes {
    public static final RecipeSerializer<ParametricTransformerRecipe> PARAMETRIC_TRANSFORMER_RECIPE_SERIALIZER = null;


    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().registerAll(
                new ParametricTransformerRecipe.ParametricTransformerRecipeSerializer()
                        .setRegistryName(GenshinImpactMod.ModID, "parametric_transformer")
        );
    }

    public static class Types {
        public static final RecipeType<ParametricTransformerRecipe> PARAMETRIC_TRANSFORMER = new GenshinRecipeType<>("parametric_transformer");
    }
}
