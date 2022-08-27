package com.gim.recipe;

import com.gim.registry.Recipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ParametricTransformerRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private NonNullList<Ingredient> results;
    private int amount;
    private int elementalDamage;

    /**
     * recipe for parametric transformer
     *
     * @param id              - recipe id
     * @param ingredients     - possible ingredients
     * @param results         - possible results
     * @param amount          - how much stacks we should take from possible results
     * @param elementalDamage - how much damage deals elemental attack. Usually it's 5, so we need 20 elemental hits to finish process.
     *                        1 value causes hard process (100 attacks) and 100 means finishing with one punch
     *                        Possible values [1, 100]
     */
    public ParametricTransformerRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, NonNullList<Ingredient> results, int amount, int elementalDamage) {
        this.id = id;
        this.ingredients = ingredients;
        this.results = results;
        this.amount = amount;
        this.elementalDamage = Mth.clamp(elementalDamage, 1, 100);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return getEnergy(container, level) >= 150;
    }

    /**
     * Returns energy for current recipe
     *
     * @param container - current container
     * @param level     - current level
     */
    public int getEnergy(Container container, Level level) {
        int energy = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty() && isAcceptableAsCatalyst(item)) {
                energy += (item.getRarity().ordinal() + 1) * item.getCount();
            }
        }

        return energy;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return getResultItem();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Container p_44004_) {
        return Recipe.super.getRemainingItems(p_44004_);
    }

    /**
     * Checks for possible catalyst
     *
     * @param stack - catalyst
     * @return
     */
    public boolean isAcceptableAsCatalyst(ItemStack stack) {
        return getIngredients().stream().anyMatch(x -> x.test(stack));
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    /**
     * All possible results. Same instances but new list
     */
    public List<ItemStack> getResultItems() {
        return results.stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());
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

    /**
     * Amount of needed elemental damage
     *
     * @return
     */
    public int getElementalDamage() {
        return elementalDamage;
    }

    /**
     * Returns result for recipe
     */
    public List<ItemStack> randomResultForDay() {
        ArrayList<ItemStack> result = new ArrayList<>();

        if (amount > 0) {
            List<ItemStack> stacks = results.stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());

            if (!stacks.isEmpty()) {
                // random based on day of month
                Random random = new Random(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                for (int i = 0; i < amount && !stacks.isEmpty(); i++) {
                    int index = random.nextInt(stacks.size());
                    result.add(stacks.get(index).copy());
                    stacks.remove(index);
                }
            }
        }

        return result;
    }

    public static class ParametricTransformerRecipeSerializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ParametricTransformerRecipe> {

        @Override
        public ParametricTransformerRecipe fromJson(ResourceLocation location, JsonObject jsonObject) {
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "ingredients"));
            NonNullList<Ingredient> results = itemsFromJson(GsonHelper.getAsJsonArray(jsonObject, "results"));
            int amount = GsonHelper.getAsInt(jsonObject, "amount");
            int damage = GsonHelper.getAsInt(jsonObject, "damage");
            return new ParametricTransformerRecipe(location, ingredients, results, amount, damage);
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

            NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(buf.readInt());
            NonNullList<Ingredient> results = NonNullList.createWithCapacity(buf.readInt());

            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            results.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            return new ParametricTransformerRecipe(location, ingredients, results, amount, damage);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ParametricTransformerRecipe recipe) {
            buf.writeInt(recipe.amount);
            buf.writeInt(recipe.elementalDamage);

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
