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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ParametricTransformerScreen extends GenshinScreenBase<ParametricTransformerMenu> {
    private final int pageSize;
    private int offset = 0;
    private final List<ItemStack> toShow = new ArrayList<>();

    public ParametricTransformerScreen(ParametricTransformerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/parametric_transformer/parametric_transformer.png"));

        this.titleLabelX = 8;
        this.titleLabelY = 8;

        this.imageWidth = 173;
        this.imageHeight = 163;

        RecipeManager recipeManager = GenshinHeler.getRecipeManager(inventory.player);
        if (recipeManager != null) {

            // fill all possible items to show them inside GUI
            List<ItemStack> allItems = recipeManager.getAllRecipesFor(Recipes.Types.PARAMETRIC_TRANSFORMER)
                    .stream()
                    .flatMap(x -> x.getAllCatalysts().stream().map(ItemStack::getItem))
                    .map(Item::getDefaultInstance)
                    .toList();

            toShow.addAll(allItems);
        }

        // current page size is a size of container
        pageSize = getMenu().slots.get(0).container.getContainerSize();
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

    @Override
    protected void containerTick() {
        // every 3 seconds change items view
        if (!getMinecraft().isPaused() && getMinecraft().player.tickCount % 15 == 0) {

            int xStart = this.leftPos + 8;
            int xEnd = xStart + 16 + pageSize * 18;

            int yStart = ((this.height - this.imageHeight) / 2) + 17;
            int yEnd = yStart + 16;

            // skip offsetting as we hover on items
            if (xStart <= this.xMouse && this.xMouse <= xEnd
                    &&
                    yStart <= this.yMouse && this.yMouse <= yEnd) {
                return;
            }

            offset += 1;
            if (offset >= toShow.size()) {
                offset = 0;
            }
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int mouseX, int mouseY) {
        super.renderBg(poseStack, p_97788_, mouseX, mouseY);

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        TextComponent component = new TextComponent(String.format("%s / %s", getMenu().getEnergy(), 150));

        // draw name
        drawCenteredString(poseStack, getMinecraft().font, component, i + 140, j + 55, -1);

        int index = 0;

        for (ItemStack stack : getCurrentList()) {
            int x = i + 8 + index * 18;
            int y = j + 17;

            // draw items and it's count
            this.itemRenderer.renderAndDecorateItem(stack, x, y);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, stack, x, y);

            // should render at the last order to hover everything
            if (x <= mouseX && mouseX <= x + 16
                    &&
                    y <= mouseY && mouseY <= y + 16) {
                // render tooltip for star
                renderTooltip(poseStack, stack, mouseX, mouseY);
            }

            index++;
        }
    }

    private Iterable<ItemStack> getCurrentList() {

        Iterator<ItemStack> iterator = new Iterator<>() {
            int index = 0;

            /**
             * Return always valid index
             * @param i - possible index
             */
            private int fixIndex(int i) {
                int total = ParametricTransformerScreen.this.toShow.size();

                if (i >= total) {
                    return i % total;
                }

                return i;
            }

            @Override
            public boolean hasNext() {
                return this.index < ParametricTransformerScreen.this.pageSize
                        && !ParametricTransformerScreen.this.toShow.isEmpty();
            }

            @Override
            public ItemStack next() {
                int currentIndex = fixIndex(index + ParametricTransformerScreen.this.offset);
                index++;

                try {
                    return ParametricTransformerScreen.this.toShow.get(currentIndex);
                } catch (Exception e) {
                    GenshinImpactMod.LOGGER.warn(e);
                    return toShow.get(0);
                }

            }
        };


        return () -> iterator;
    }
}
