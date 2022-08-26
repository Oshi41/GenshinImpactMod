package com.gim.client.screen;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.ParametricTransformerMenu;
import com.gim.recipe.ParametricTransformerRecipe;
import com.gim.registry.Recipes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ParametricTransformerScreen extends GenshinScreenBase<ParametricTransformerMenu> {
    private String recipeName;
    private List<ItemStack> allItems = null;
    private int index = -1;

    private final ArrayList<ItemStack> toShow = new ArrayList<>(9);

    public ParametricTransformerScreen(ParametricTransformerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/parametric_transformer/parametric_transformer.png"));

        this.titleLabelX = 8;
        this.titleLabelY = 8;

        this.imageWidth = 173;
        this.imageHeight = 163;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new Button(this.leftPos + 115, this.topPos + 66, 53, 16,
                new TranslatableComponent("gim.start"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0)
        ) {

            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                active = getMenu().getEnergy() >= 150;
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
            }
        });
    }

    private void updateItems() {
        ParametricTransformerRecipe currentRecipe = getMenu().getCurrentRecipe();
        String currentRecipeName = currentRecipe == null
                ? null
                : currentRecipe.getId().toString();

        // need to fill all possible items
        if (allItems == null || allItems.isEmpty() || !Objects.equals(currentRecipeName, recipeName)) {
            // save new recipe name
            recipeName = currentRecipeName;

            // filling all possible items
            if (currentRecipe != null) {
                allItems = currentRecipe.getIngredients().stream().flatMap(x -> Arrays.stream(x.getItems())).collect(Collectors.toList());
            } else {
                allItems = GenshinHeler.getRecipeManager(getMinecraft().player)
                        .getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)
                        .stream()
                        .flatMap(x -> x.getIngredients().stream())
                        .flatMap(x -> Arrays.stream(x.getItems()))
                        .collect(Collectors.toList());
            }
        }

        // showing next 9 items
        if (allItems != null && allItems.isEmpty() && !getMinecraft().isPaused() && (getMinecraft().player.tickCount & 20) == 0) {
            int correctedIndex = this.index;

            for (int i = 0; i < 9; i++) {
                correctedIndex++;
                correctedIndex = Math.max(0, correctedIndex);
                if (correctedIndex >= allItems.size()) {
                    correctedIndex = 0;
                }

                toShow.set(i, allItems.get(correctedIndex));
            }

            this.index = correctedIndex;
        }
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(p_97787_, p_97788_, p_97789_, p_97790_);
        updateItems();

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        TextComponent component = new TextComponent(String.format("%s / %s", getMenu().getEnergy(), 150));

        // draw name
        drawCenteredString(p_97787_, getMinecraft().font, component, i + 140, j + 55, -1);

        for (int k = 0; k < toShow.size(); k++) {
            int y = 17;
            int x = 8 + k * 18;

            ItemStack itemStack = toShow.get(k);

            // draw items and it's count
            this.itemRenderer.renderAndDecorateItem(itemStack, x, y);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, x, y);
        }
    }
}
