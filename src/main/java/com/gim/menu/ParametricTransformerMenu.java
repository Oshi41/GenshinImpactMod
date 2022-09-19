package com.gim.menu;

import com.gim.GenshinHeler;
import com.gim.entity.ParametricTransformer;
import com.gim.items.ParametricTransformerItem;
import com.gim.menu.base.GenshinContainer;
import com.gim.menu.base.GenshinMenuBase;
import com.gim.recipe.ParametricTransformerRecipe;
import com.gim.registry.Items;
import com.gim.registry.Menus;
import com.gim.registry.Recipes;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParametricTransformerMenu extends GenshinMenuBase {

    private final GenshinContainer container = new GenshinContainer(9);
    private final ContainerData data = new SimpleContainerData(1);

    protected ParametricTransformerMenu(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(menuType, containerID, playerInv, access);

        addDataSlots(data);

        for (int i = 0; i < 9; i++) {
            int y = 35;
            int x = 8 + i * 18;

            addSlot(new Slot(container, i, x, y) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getEnergy() < ParametricTransformerRecipe.RECIPE_ENERGY && accepted(stack);
                }

                @Override
                public void setChanged() {
                    super.setChanged();

                    int energy = 0;

                    for (int i = 0; i < container.getContainerSize(); i++) {
                        energy += ParametricTransformerRecipe.getEnergy(container.getItem(i));
                    }

                    setEnergy(energy);
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    int totalEnergy = getEnergy() + ParametricTransformerRecipe.getEnergy(stack);
                    // if energy overflows
                    if (totalEnergy > ParametricTransformerRecipe.RECIPE_ENERGY) {
                        int perItem = ParametricTransformerRecipe.getEnergy(stack) / stack.getCount();
                        int overflow = totalEnergy - ParametricTransformerRecipe.RECIPE_ENERGY;

                        // if actually overflow
                        // so trying to put useless items
                        if (overflow > perItem) {
                            int reduce = (int) Math.floor(overflow / (double) perItem);
                            int count = Math.max(0, stack.getCount() - reduce);

                            // returning min of stack size
                            return Math.min(count, super.getMaxStackSize());
                        }
                    }

                    return super.getMaxStackSize(stack);
                }
            });
        }

        container.addListener((int slotId, ItemStack prev, ItemStack current) -> getSlot(slotId).setChanged());
        drawPlayersSlots(8, 84);
    }

    public ParametricTransformerMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(Menus.parametric_transformer, containerID, playerInv, ContainerLevelAccess.create(playerInv.player.getLevel(), pos));
    }

    public ParametricTransformerMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(Menus.parametric_transformer, containerID, playerInv, ContainerLevelAccess.NULL);
    }

    /**
     * Is stack accepted by current recipe
     *
     * @param stack - current stack
     */
    private boolean accepted(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            RecipeManager recipeManager = GenshinHeler.getRecipeManager(this.playerInv.player);
            if (recipeManager != null) {
                for (ParametricTransformerRecipe recipe : recipeManager.getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)) {
                    if (recipe.match(stack)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof ParametricTransformerItem
                && player.getMainHandItem().getCount() == 1;
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if (index == 0 && getEnergy() >= ParametricTransformerRecipe.RECIPE_ENERGY) {
            access.execute((level, blockPos) -> {
                // items for current roll
                Tuple<List<ItemStack>, Integer> result = generateRandomResult(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                // remove parametric transformer
                player.getMainHandItem().shrink(1);

                // clear all catalysts
                container.clearContent();

                // placing entity in word
                level.addFreshEntity(new ParametricTransformer(player, result.getA(), result.getB(), blockPos));
                removed(player);
            });

            return true;
        }

        return false;
    }

    /**
     * Generates random items for current ran
     *
     * @param day - day of month
     * @return - tuple: 1) generated items, 2) damage for Parameter Transformer entity
     */
    private Tuple<List<ItemStack>, Integer> generateRandomResult(int day) {
        Map<ParametricTransformerRecipe, Integer> recipeMap = new HashMap<>();
        List<ItemStack> result = Lists.newArrayList(Items.parametric_transformer.getDefaultInstance());
        int damage = 5;

        // correcting value
        day = Mth.clamp(day, 1, 31);

        // obtaining recipe manager
        RecipeManager recipeManager = GenshinHeler.getRecipeManager(this.playerInv.player);
        if (recipeManager != null) {
            // find all possible recipes
            for (ParametricTransformerRecipe recipe : recipeManager.getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)) {
                // recipe energy value
                int value = recipe.getRecipeEnergy(this.container, this.playerInv.player.getLevel());
                if (value > 0) {
                    // if can apply current recipe
                    if (recipe.getQuality() > 0) {
                        // every level quality lower possibility by 3 times
                        value /= recipe.getQuality() * 3;
                    }

                    recipeMap.put(recipe, value);
                }
            }
        }

        // if find any recipes
        if (!recipeMap.isEmpty()) {
            Random random = new Random(day);
            // 5-10 rolls
            int rollAmount = (int) GenshinHeler.getRandomNormalDistrib(random, 5, 10);

            // executing current amount of rolls
            for (int i = 0; i < rollAmount; i++) {
                ParametricTransformerRecipe recipe = GenshinHeler.selectRandomly(recipeMap, random);
                result.addAll(recipe.getRandomItems(random));
            }

            // calculates damage. The most suitable recipe affects more to damage
            damage = recipeMap.entrySet().stream().mapToInt(x -> x.getValue() * x.getKey().getDamage()).sum() / recipeMap.values().stream().mapToInt(x -> x).sum();
        }

        return new Tuple<>(result, damage);
    }

    /**
     * Current energy
     */
    public int getEnergy() {
        return data.get(0);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean flag) {

        if (start < end && end <= firstPlayerSlot) {
            int finalEnergy = getEnergy() + ParametricTransformerRecipe.getEnergy(stack);
            // overflowing
            if (finalEnergy > ParametricTransformerRecipe.RECIPE_ENERGY) {
                // actual overflow
                int overflow = finalEnergy - ParametricTransformerRecipe.RECIPE_ENERGY;
                // energy per item
                int perItem = ParametricTransformerRecipe.getEnergy(stack) / stack.getCount();
                // if overflow is same or bigger than energy item
                // means we'll actually put useless item(s)
                if (overflow >= perItem) {
                    // how much item count I should reduce
                    int reduce = (int) Math.floor(overflow / (double) perItem);
                    if (reduce > 0) {

                        // actually can't put any item
                        if (reduce >= stack.getCount()) {
                            return false;
                        }

                        // final count for stack we'll transfer
                        int finalCount = stack.getCount() - reduce;

                        // creating stack copy
                        ItemStack copy = stack.copy();
                        // change it's count to min possible to rich max energy
                        copy.setCount(finalCount);
                        // perform moving
                        super.moveItemStackTo(copy, start, end, flag);

                        // moved all items
                        if (copy.isEmpty()) {
                            // reuse variable, no it means final slot count

                            // final count for return stack will be initial size without transfered stack size
                            finalCount = stack.getCount() - finalCount;
                        } else {
                            finalCount = stack.getCount() - finalCount + copy.getCount();
                        }

                        // if actually should change stack count
                        if (finalCount != stack.getCount()) {
                            stack.setCount(finalCount);
                        }

                        return false;
                    }
                }
            }
        }

        return super.moveItemStackTo(stack, start, end, flag);
    }

    public void setEnergy(int val) {
        data.set(0, val);
    }
}
