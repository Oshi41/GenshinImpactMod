package com.gim.menu;

import com.gim.GenshinHeler;
import com.gim.entity.ParametricTransformer;
import com.gim.items.ParametricTransformerItem;
import com.gim.menu.base.GenshinContainer;
import com.gim.menu.base.GenshinMenuBase;
import com.gim.recipe.ParametricTransformerRecipe;
import com.gim.registry.Menus;
import com.gim.registry.Recipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ParametricTransformerMenu extends GenshinMenuBase {
    private final GenshinContainer container = new GenshinContainer(9);
    private final ContainerData data = new SimpleContainerData(1);
    private ParametricTransformerRecipe recipe;

    protected ParametricTransformerMenu(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(menuType, containerID, playerInv, access);

        addDataSlots(data);

        for (int i = 0; i < 9; i++) {
            int y = 35;
            int x = 8 + i * 18;

            addSlot(new Slot(container, i, x, y) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return recipe != null
                            ? recipe.possibleCatalyst(stack)
                            : from(stack, playerInv.player) != null;
                }

                @Override
                public void setChanged() {
                    super.setChanged();

                    // current recipe
                    if (this.container.isEmpty()) {
                        recipe = null;
                    } else if (recipe == null) {
                        // iterating through all items
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            ItemStack item = container.getItem(i);
                            // find first not null
                            if (!item.isEmpty()) {
                                // searching recipe
                                recipe = from(item, playerInv.player);
                                break;
                            }
                        }
                    }

                    int energy = 0;

                    // calculating energy
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty()) {
                            energy += stack.getCount() * (stack.getItem().getRarity(stack).ordinal() + 1);
                        }
                    }

                    setEnergy(energy);
                }
            });
        }

        drawPlayersSlots(8, 84);
    }

    public ParametricTransformerMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(Menus.parametric_transformer, containerID, playerInv, ContainerLevelAccess.create(playerInv.player.getLevel(), pos));
    }

    public ParametricTransformerMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(Menus.parametric_transformer, containerID, playerInv, ContainerLevelAccess.NULL);
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
        if (index == 0 && getEnergy() >= 150 && recipe != null) {
            access.execute((level, blockPos) -> {
                // remove parametric transformer
                player.getMainHandItem().shrink(1);

                // clear all catalysts
                container.clearContent();

                // placing entity in world
                level.addFreshEntity(new ParametricTransformer(player, recipe));
                removed(player);
            });

            return true;
        }

        return false;
    }

    /**
     * Current energy
     */
    public int getEnergy() {
        return data.get(0);
    }

    public void setEnergy(int val) {
        data.set(0, val);
    }

    @Nullable
    public ParametricTransformerRecipe getCurrentRecipe() {
        return recipe;
    }

    /**
     * Searches transformer recipe from stack
     *
     * @param stack  - catalyst
     * @param entity - holder entity
     */
    public static ParametricTransformerRecipe from(ItemStack stack, LivingEntity entity) {
        RecipeManager recipeManager = GenshinHeler.getRecipeManager(entity);
        if (recipeManager == null)
            return null;

        ParametricTransformerRecipe result = recipeManager.getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)
                .stream()
                .filter(x -> x.possibleCatalyst(stack))
                .findFirst()
                .orElse(null);

        return result;
    }
}
