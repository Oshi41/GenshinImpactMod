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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParametricTransformerMenu extends GenshinMenuBase {
    private final GenshinContainer container = new GenshinContainer(9);
    private final ContainerData data = new SimpleContainerData(1);
    private final Map<ParametricTransformerRecipe, Integer> recipes = new HashMap<>();

    protected ParametricTransformerMenu(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(menuType, containerID, playerInv, access);

        addDataSlots(data);

        for (int i = 0; i < 9; i++) {
            int y = 35;
            int x = 8 + i * 18;

            addSlot(new Slot(container, i, x, y) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (recipes.isEmpty()) {
                        // acceptable by any transformer recipe
                        return GenshinHeler.getRecipeManager(playerInv.player).getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)
                                .stream()
                                .anyMatch(x -> x.isAcceptableAsCatalyst(stack));
                    } else {
                        //  can current recipes accept this item
                        return recipes.keySet().stream().anyMatch(x -> x.isAcceptableAsCatalyst(stack));
                    }
                }

                @Override
                public void setChanged() {
                    super.setChanged();

                    // clear saved recipes
                    recipes.clear();

                    // no items inside
                    if (container.isEmpty()) {
                        setEnergy(0);
                        return;
                    }

                    // iterating through all transformer recipes
                    for (ParametricTransformerRecipe recipe : GenshinHeler.getRecipeManager(playerInv.player).getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)) {
                        int energy = recipe.getEnergy(container, playerInv.player.getLevel());

                        // if contains any energy we should store it
                        if (energy > 0) {
                            recipes.put(recipe, energy);
                        }
                    }

                    // find max energy for some recipe
                    int energy = recipes.values().stream().max(Integer::compareTo).orElse(0);
                    setEnergy(energy);

                    // need to left only one recipe cause we can apply transformation
                    if (energy >= 150) {
                        // find all recipes with max energies
                        List<ParametricTransformerRecipe> accepted = recipes.entrySet().stream().filter(x -> x.getValue() == energy).map(Map.Entry::getKey).toList();
                        // founded recipe by random index
                        int index = playerInv.player.getLevel().getRandom().nextInt(accepted.size());
                        ParametricTransformerRecipe recipe = accepted.get(index);

                        // left only one recipe
                        recipes.clear();
                        recipes.put(recipe, energy);
                    }
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
        if (index == 0 && getEnergy() >= 150 && recipes.size() == 1) {
            access.execute((level, blockPos) -> {
                // remove parametric transformer
                player.getMainHandItem().shrink(1);

                // clear all catalysts
                container.clearContent();

                // placing entity in world
                level.addFreshEntity(new ParametricTransformer(player, recipes.keySet().iterator().next()));
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

    /**
     * Possible catalysts
     *
     * @param offset - how many items we should skip
     * @param take   - how many items we should take
     * @return - items to render
     */
    public List<ItemStack> getPossibleCatalysts(int offset, int take) {

        Stream<ParametricTransformerRecipe> stream = recipes.isEmpty()
                // choose all possible recipes
                ? GenshinHeler.getRecipeManager(playerInv.player).getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER).stream()
                // choosing only possible recipes
                : recipes.keySet().stream();

        // retrieving possible catalysts
        return stream.flatMap(x -> x.getIngredients().stream())
                .flatMap(x -> Arrays.stream(x.getItems()))
                .skip(offset)
                .limit(take)
                .collect(Collectors.toList());
    }

    /**
     * Searches transformer recipe from stack
     *
     * @param stack  - catalyst
     * @param entity - holder entity
     */
    @NotNull
    public static List<ParametricTransformerRecipe> from(ItemStack stack, LivingEntity entity) {
        RecipeManager recipeManager = GenshinHeler.getRecipeManager(entity);
        if (recipeManager == null)
            return List.of();

        List<ParametricTransformerRecipe> recipes = recipeManager.getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)
                .stream()
                .filter(x -> x.isAcceptableAsCatalyst(stack))
                .toList();

        return recipes;
    }
}
