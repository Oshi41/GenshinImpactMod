package com.gim.client.screen;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.client.GenshinClientHooks;
import com.gim.items.ConstellationItem;
import com.gim.menu.WishMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WishMenuScreen extends AbstractContainerScreen<WishMenu> {
    private final ResourceLocation WHITE = new ResourceLocation("forge", "textures/white.png");
    private final TextureAtlasSprite sprite;
    private int tickAmount;

    public WishMenuScreen(WishMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);

        ResourceLocation location = fromStack(menu.getStack());
        sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);

        imageWidth = 128;
        imageHeight = 128;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {

        if (tickAmount <= sprite.getFrameCount()) {
            RenderSystem.setShaderTexture(0, sprite.atlas().location());
            blit(poseStack, this.getGuiLeft(), this.getGuiTop(),
                    this.getBlitOffset(), 128, 128, sprite);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, WHITE);
            this.blit(poseStack, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);

            ItemStack itemStack = getMenu().getStack();
            int xPos = getGuiLeft() + this.imageWidth / 2;
            int yPos = getGuiTop() + this.imageHeight / 2;

            this.itemRenderer.renderAndDecorateItem(itemStack, xPos, yPos);
            this.itemRenderer.renderGuiItemDecorations(getMinecraft().font, itemStack, xPos, yPos);

            if (GenshinHeler.between(mouseX, xPos, xPos + 16) &&
                    GenshinHeler.between(mouseY, yPos, yPos + 16)) {
                // tooltip for curernt stack
                renderTooltip(poseStack, itemStack, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        // ignored
    }

    @Override
    protected void containerTick() {
        tickAmount++;
    }

    /**
     * Returns animation location from item stack
     *
     * @param stack - reward
     */
    private ResourceLocation fromStack(ItemStack stack) {

        int index = 3;

        if (stack.getItem() instanceof ConstellationItem item) {
            int starsCount = item.assignedTo.get().starPoses().size();
            if (GenshinHeler.between(starsCount, 4, 5)) {
                index = starsCount;
            }
        }

        return new ResourceLocation(
                GenshinImpactMod.ModID,
                String.format("textures/wishes/%s.png", index)
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tickAmount > sprite.getFrameCount() + 20) {
            // request close
            getMinecraft().gameMode.handleInventoryButtonClick(getMenu().containerId, 1);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
