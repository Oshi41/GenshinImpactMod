package com.gim.recipe;

import com.gim.registry.Recipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Parametric Transformer Recipe.
 * <p>
 * Contains possible ingredients and possible results.
 * During processing algorithm selects getAmount() of items randomly selected from results.
 * Random should be applied to day of month, so it should give predictable results
 * Quality affects recipe probability by 3 times rarer by level. It was made for more valuable recipes
 * Damage means how much damage Parametric Transformer Entity receive per each elemental attack
 */
public class ParametricTransformerRecipe implements Recipe<Container> {

    // region Static

    /**
     * Min recipe energy to start transforming
     */
    public static final int RECIPE_ENERGY = 150;


    /**
     * Helping method to calculate energy from item
     *
     * @param stack - catalyst
     */
    public static int getEnergy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        return stack.getCount() * (1 + stack.getRarity().ordinal());
    }

    // endregion

    // region fields and getters

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final NonNullList<Ingredient> results;
    private final int amount;
    private final int damage;
    private final int quality;

    /**
     * Amount of items possibly can be obtained from recipe results
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Get amount of damage Parametric Transformer Entity receive from elemental damage source
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Recipe quality.
     * Bigger quality chooses 3 times rarer by level
     */
    public int getQuality() {
        return quality;
    }

    // endregion

    // region Overrides

    @Override
    public boolean matches(Container container, Level level) {
        return getRecipeEnergy(container, level) > 0;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Recipes.PARAMETRIC_TRANSFORMER_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return Recipes.Types.PARAMETRIC_TRANSFORMER;
    }

    // endregion

    /**
     * Gets energy for current recipe
     *
     * @param container - current container
     * @param level     - current level
     */
    public int getRecipeEnergy(Container container, Level level) {
        int energy = 0;

        // iterate through all container items
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (match(item)) {
                energy += getEnergy(item);
            }
        }

        return energy;
    }

    /**
     * generates random items from recipe
     * Ideally should return predictable results if we use random with same seed
     */
    public List<ItemStack> getRandomItems(Random random) {
        ArrayList<ItemStack> result = new ArrayList<>();

        // all possible result items
        List<ItemStack> itemStacks = results.stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());
        // max items here
        int max = getAmount();

        while (max > 0 && !itemStacks.isEmpty()) {
            // find random index
            int index = random.nextInt(itemStacks.size());
            // add as result item
            result.add(itemStacks.get(index).copy());

            // remove this item and decreasing amount
            itemStacks.remove(index);
            max -= 1;
        }

        return result;
    }

    /**
     * If provided stack accepted by current recips
     *
     * @param stack - current stack
     */
    public boolean match(ItemStack stack) {
        for (Ingredient ingredient : getIngredients()) {
            if (ingredient.test(stack)) {
                return true;
            }
        }


        return false;
    }

    /**
     * All possible catalysts for recipe
     */
    public List<ItemStack> getAllCatalysts() {
        List<ItemStack> itemStacks = getIngredients().stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());
        return itemStacks;
    }

    public ParametricTransformerRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, NonNullList<Ingredient> results, int amount, int damage, int quality) {

        this.id = id;
        this.ingredients = ingredients;
        this.results = results;
        this.amount = amount;
        this.damage = damage;
        this.quality = quality;
    }

    public static class ParametricTransformerRecipeSerializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ParametricTransformerRecipe> {

        @Override
        public ParametricTransformerRecipe fromJson(ResourceLocation location, JsonObject jsonObject) {
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
            NonNullList<Ingredient> results = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "results"));
            int amount = GsonHelper.getAsInt(jsonObject, "amount");
            int damage = GsonHelper.getAsInt(jsonObject, "damage");
            int quality = GsonHelper.getAsInt(jsonObject, "quality");
            return new ParametricTransformerRecipe(location, ingredients, results, amount, damage, quality);
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < p_44276_.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i));
                if (net.minecraftforge.common.ForgeConfig.SERVER.skipEmptyShapelessCheck.get() || !ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        @Nullable
        @Override
        public ParametricTransformerRecipe fromNetwork(ResourceLocation location, FriendlyByteBuf buf) {
            int amount = buf.readInt();
            int damage = buf.readInt();
            int quality = buf.readInt();

            NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(buf.readInt());
            NonNullList<Ingredient> results = NonNullList.createWithCapacity(buf.readInt());

            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            results.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            return new ParametricTransformerRecipe(location, ingredients, results, amount, damage, quality);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ParametricTransformerRecipe recipe) {
            buf.writeInt(recipe.getAmount());
            buf.writeInt(recipe.getDamage());
            buf.writeInt(recipe.getQuality());

            buf.writeInt(recipe.ingredients.size());
            buf.writeInt(recipe.results.size());

            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buf);
            }

            for (Ingredient ingredient : recipe.results) {
                ingredient.toNetwork(buf);
            }
        }
    }
}
