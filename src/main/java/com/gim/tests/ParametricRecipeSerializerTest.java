package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@GameTestHolder(GenshinImpactMod.ModID)
public class ParametricRecipeSerializerTest {

    /**
     * Generates random incorrect data
     */
    private List<String> wrongTestData() {
        ArrayList<String> result = new ArrayList<>();

        result.addAll(List.of(
                "",
                "{",
                "{}",
                "[",
                "[]",
                "\"",
                "\"\""
        ));

        JsonObject object = randomCorrect();
        ArrayList<JsonElement> wrongs = new ArrayList<>();
        TestHelper.generateWrongData(wrongs, object, "tag");

        wrongs.forEach(x -> result.add(x.toString()));

        return result;
    }

    /**
     * Creates correct random data
     */
    private JsonObject randomCorrect() {
        JsonObject result = new JsonObject();
        Random random = new Random();

        // type of parser
        result.addProperty("type", "gim:parametric_transformer");
        // damage multiplier
        result.addProperty("damage", random.nextInt(100));
        // amount of result item
        result.addProperty("amount", random.nextInt(99) + 1);

        List<Item> allItems = ForgeRegistries.ITEMS.getValues().stream().filter(x -> x != Items.AIR).collect(Collectors.toList());
        List<TagKey<Item>> allTags = ForgeRegistries.ITEMS.tags().getTagNames().collect(Collectors.toList());

        Function<Integer, JsonArray> create = (end) -> {
            List<Ingredient> ingredients = new ArrayList<>();

            for (int i = 0; i < end; i++) {

                if (random.nextInt(5) == 0) {
                    int index = random.nextInt(allTags.size());
                    TagKey<Item> tagKey = allTags.get(index);
                    allTags.remove(index);
                    ingredients.add(Ingredient.of(tagKey));
                } else {
                    int index = random.nextInt(allItems.size());
                    Item item = allItems.get(index);
                    allItems.remove(index);
                    ingredients.add(Ingredient.of(item));
                }
            }

            JsonArray array = new JsonArray();

            for (Ingredient ingredient : ingredients) {
                JsonElement elem = ingredient.toJson();
                array.add(elem);
            }

            return array;
        };

        result.add("ingredients", create.apply(random.nextInt(40) + 5));
        result.add("results", create.apply(random.nextInt(40) + 5));

        return result;
    }


    @CustomGameTest(timeoutTicks = 10)
    public void parametricTransformer_serialize_wrong(GameTestHelper helper) {
        StringBuilder builder = new StringBuilder();
        List<String> wrongTestData = wrongTestData();

        for (String wrongJson : wrongTestData) {
            try {
                Recipe<?> recipe = RecipeManager.fromJson(new ResourceLocation("gim:rand"), GsonHelper.parse(wrongJson));
                builder.append(wrongJson);
                builder.append("\n\n");
            } catch (Exception e) {
            }
        }

        if (!builder.isEmpty()) {
            helper.fail(builder.toString());
        }
    }

    @CustomGameTest(timeoutTicks = 10)
    public void parametricTransformer_serialize_correct(GameTestHelper helper) {
        for (int i = 0; i < 100; i++) {
            JsonObject correct = randomCorrect();

            try {
                Recipe<?> recipe = RecipeManager.fromJson(new ResourceLocation("gim:rand"), correct);
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.error(e);
                GenshinImpactMod.LOGGER.debug(correct);
                helper.fail(e.getMessage());
            }
        }
    }
}
