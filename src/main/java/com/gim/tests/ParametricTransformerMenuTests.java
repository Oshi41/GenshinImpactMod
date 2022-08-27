package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.menu.ParametricTransformerMenu;
import com.gim.recipe.ParametricTransformerRecipe;
import com.gim.registry.Items;
import com.gim.registry.Recipes;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@GameTestHolder(GenshinImpactMod.ModID)
public class ParametricTransformerMenuTests {

    @CustomGameTest(timeoutTicks = 10, setupTicks = 1)
    public void parametricTransformer_checkAllRecipes(GameTestHelper helper) {
        ServerPlayer player = TestHelper.createFakePlayer(helper, false);
        helper.setBlock(player.blockPosition(), Blocks.AIR);
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.parametric_transformer.getDefaultInstance());

        List<ParametricTransformerRecipe> recipeList = helper.getLevel().getServer().getRecipeManager().getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER);
        if (recipeList.isEmpty()) {
            helper.fail("No parametric recipe founded");
        }

        player.gameMode.useItemOn(player, player.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(player.position(), Direction.NORTH, player.blockPosition(), false));

        if (!(player.containerMenu instanceof ParametricTransformerMenu)) {
            helper.fail(String.format("Wrong container menu, should be type of %s, but found %s", ParametricTransformerMenu.class,
                    player.containerMenu == null ? "" : player.containerMenu.getClass().getName()));
        }

        ParametricTransformerMenu transformerMenu = (ParametricTransformerMenu) player.containerMenu;

        // iterating through all recipes
        for (ParametricTransformerRecipe recipe : recipeList) {

            // all items accepted by current recipe
            List<ItemStack> allPossible = recipe.getIngredients().stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());
            if (allPossible.isEmpty()) {
                helper.fail(String.format("No ingredients for %s recipe", recipe.getId()));
            }

            for (ItemStack stack : allPossible) {
                int count = (int) Math.ceil(150. / (stack.getRarity().ordinal() + 1));

                for (int val : List.of(count - 10, count - 1, count, count + 1, count + 10)) {
                    transformerMenu.slots.get(0).container.clearContent();

                    // custom stack size, should skip it
                    if (Math.floor((double) val / stack.getMaxStackSize()) > transformerMenu.slots.get(0).container.getContainerSize()) {
                        GenshinImpactMod.LOGGER.warn(String.format("Item %s has limited stack size %s so we need %s stacks but have only %s. Skipping",
                                stack.getDisplayName().getString(),
                                stack.getMaxStackSize(),
                                Math.floor((double) val / stack.getMaxStackSize()),
                                transformerMenu.slots.get(0).container.getContainerSize()));
                        break;
                    }

                    if (!transformerMenu.slots.get(0).container.isEmpty()) {
                        helper.fail("transformer menu container is not empty!");
                    }

                    ItemStack copy = stack.copy();
                    copy.setCount(val);

                    for (int i = 0; i < 9 && !copy.isEmpty(); i++) {
                        copy = transformerMenu.getSlot(i).safeInsert(copy);
                    }

                    if (!copy.isEmpty()) {
                        helper.fail(String.format("[RECIPE %s] cannot insert [%s] %s",
                                recipe.getId(),
                                copy.getCount(),
                                copy.getDisplayName().getString()));
                    }

                    boolean shouldUse = val >= count;
                    boolean actuallyCanUse = transformerMenu.getEnergy() >= 150;

                    if (shouldUse != actuallyCanUse) {
                        helper.fail(String.format("[RECIPE %s] Parametric transformer %s start, but actually %s. Filled with item %s, count:%s",
                                recipe.getId(),
                                actuallyCanUse ? "can" : "can't",
                                shouldUse ? "can" : "can't",
                                stack.getDisplayName().getString(),
                                val));
                    }

                }
            }

            // clear container
            transformerMenu.slots.get(0).container.clearContent();

            // checking putting all items in empty container
            for (ItemStack stack : allPossible) {
                for (int i = 0; i < 9; i++) {
                    if (!transformerMenu.getSlot(i).mayPlace(stack)) {
                        helper.fail(String.format("[NO RECIPE] Transformer menu can't insert [%s] %s in %s slot", stack.getCount(), stack.getDisplayName().getString(), i));
                    }
                }
            }

            // clear container
            transformerMenu.slots.get(0).container.clearContent();

            // TODO
            // 1) Check any set of items and check safe insert
            // 2) Calculate items that cannot insert for current recipe(s)

//            for (int i = 0; i < allPossible.size(); i++) {
//                ItemStack origin = allPossible.get(i);
//            }
//
//            // inserting at first slot
//            transformerMenu.getSlot(0).set(allPossible.get(0));
//            allPossible.remove(0);
//
//
//
//            // checking for current recipe
//            for (ItemStack stack : allPossible) {
//                for (int i = 0; i < 9; i++) {
//                    if (!transformerMenu.getSlot(i).mayPlace(stack)) {
//                        helper.fail(String.format("[WITH RECIPE %s] Transformer menu can't insert %s in %s slot", recipe.getId(), stack.getDisplayName().getString(), i));
//                    }
//                }
//            }

            // TODO
            // detect which one item we should not place

//            // checking all other items
//            for (Item item : other) {
//                for (int i = 0; i < 9; i++) {
//                    if (transformerMenu.getSlot(i).mayPlace(item.getDefaultInstance())) {
//                        helper.fail(String.format("[WITH RECIPE %s] Transformer menu can insert %s in %s slot, but actually should not!",
//                                recipe.getId(),
//                                item.getDefaultInstance().getDisplayName().getString(),
//                                i));
//                    }
//                }
//            }
        }


    }
}
