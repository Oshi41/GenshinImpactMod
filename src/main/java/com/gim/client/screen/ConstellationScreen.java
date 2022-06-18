package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.items.ConstellationItem;
import com.gim.menu.ConstellationMenu;
import com.gim.networking.StarClickedPackage;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ConstellationScreen extends AbstractContainerScreen<ConstellationMenu> {
    private static final ResourceLocation GUI = new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/star_worktable/star_worktable_gui.png");
    private static final ResourceLocation NETHER_STAR = new ResourceLocation("textures/item/nether_star.png");


    private Button left;
    private Button right;
    private final List<StarInfo> current = new ArrayList<>();
    private final List<ImageButton> starButtons = new ArrayList<>();

    private ItemStack stack = ItemStack.EMPTY;

    public ConstellationScreen(ConstellationMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);

        this.imageWidth = 176;
        this.imageHeight = 256;

        this.titleLabelX = 50;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - (18 * 5) - 4;
    }

    @Override
    protected void init() {
        super.init();

        left = addRenderableWidget(new Button(this.leftPos + 4, this.topPos + 4, 18, 18, new TextComponent("<"), p_93751_ -> navigate(false)));
        right = addRenderableWidget(new Button(this.leftPos + this.imageWidth - 4 - 18, this.topPos + 4, 18, 18, new TextComponent(">"), p_93751_ -> navigate(true)));

        if (getMenu().current() == null) {
            removed();
        }
    }

    private void navigate(boolean forward) {
        int add = forward ? 1 : -1;
        getMenu().changeIndex(getMenu().getIndex() + add);
    }

    private List<StarInfo> create(GenshinEntityData data) {
        IGenshinPlayer assotiatedPlayer = data.getAssotiatedPlayer();

        List<Vec2> poses = assotiatedPlayer.starPoses();
        double value = data.getAttributes().getInstance(Attributes.constellations).getValue();

        ArrayList<StarInfo> result = new ArrayList<>();

        for (int i = 0; i < poses.size(); i++) {
            StarInfo starInfo = new StarInfo(i,
                    value >= i,
                    FormattedText.of(new TranslatableComponent(String.format("%s.%s.star.%s", assotiatedPlayer.getRegistryName().getNamespace(), assotiatedPlayer.getRegistryName().getPath(), i + 1)).getString()),
                    poses.get(i));

            result.add(starInfo);
        }

        return result;
    }

    private void tryRefresh(GenshinEntityData genshinEntityData, int x, int y) {

        List<StarInfo> list = create(genshinEntityData);
        if (Objects.equals(list, current)) {
            return;
        }

        current.clear();
        current.addAll(list);

        for (ImageButton imageButton : starButtons) {
            removeWidget(imageButton);
        }
        starButtons.clear();

        for (StarInfo starInfo : current) {
            int size = starInfo.isOpen ? 15 : 7;

            int xStart = (int) (x + starInfo.pos.x) - size / 2;
            int yStart = (int) (y + starInfo.pos.y) - size / 2;

            starButtons.add(addRenderableWidget(new ImageButton(
                    xStart,
                    yStart,
                    size,
                    size,
                    0,
                    0,
                    0,
                    NETHER_STAR,
                    size,
                    size,
                    p_93751_ -> onStarClick(p_93751_, starInfo),
                    (Button button, PoseStack stack, int xPos, int yPos) -> renderStackTooltip(stack, starInfo.text, xPos, yPos),
                    TextComponent.EMPTY)));
        }

        Item item = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item1 -> item1 instanceof ConstellationItem && Objects.equals(((ConstellationItem) item1).assignedTo.get(), current))
                .findFirst()
                .orElse(Items.AIR);

        stack = item.getDefaultInstance();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        GenshinEntityData genshinEntityData = getMenu().current();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        ResourceLocation location = genshinEntityData.getAssotiatedPlayer().getRegistryName();
        location = new ResourceLocation(location.getNamespace(), String.format("textures/players/%s/constellation.png", location.getPath()));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, location);

        i += 34;
        j += 34;
        int size = 128;
        this.blit(poseStack, i, j, 0, 0, size, size, size, size);

        tryRefresh(genshinEntityData, i, j);
    }

    private void renderStackTooltip(PoseStack stack, FormattedText text, int xPos, int yPos) {
        renderComponentTooltip(stack, List.of(text), xPos, yPos, Items.AIR.getDefaultInstance());
    }

    void onStarClick(Button p_93751_, StarInfo starInfo) {
        // not opened star
        if (!starInfo.isOpen
                // first index
                && starInfo.index == 0
                // or prev star is open
                || current.get(starInfo.index - 1).isOpen) {
            Slot slot = this.menu.getSlot(0);
            if (slot.hasItem()) {
                // sending message to open star
                GenshinImpactMod.CHANNEL.sendToServer(new StarClickedPackage(getMenu().current().getAssotiatedPlayer()));
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        super.renderLabels(p_97808_, p_97809_, p_97810_);

        BaseComponent text = getMenu().current().getAssotiatedPlayer().getName().plainCopy();
        text.setStyle(text.getStyle()
                .withColor(getMenu().current().getAssotiatedPlayer().getElemental().getChatColor())
                .withUnderlined(true)
        );

        this.font.draw(p_97808_, text, (float) this.titleLabelX, (float) this.titleLabelY + 18, 4210752);
    }

    @Override
    protected void renderTooltip(PoseStack p_97791_, int x, int y) {
        super.renderTooltip(p_97791_, x, y);

        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index == 0 && !stack.isEmpty()) {
            renderTooltip(p_97791_, stack, x, y);
        }
    }

    @Override
    public void render(PoseStack p_97795_, int x, int y, float p_97798_) {
        super.render(p_97795_, x, y, p_97798_);
        renderTooltip(p_97795_, x, y);
    }

    public class StarInfo {
        public final int index;
        public final boolean isOpen;
        public final FormattedText text;
        public final Vec2 pos;

        public StarInfo(int index, boolean isOpen, FormattedText text, Vec2 pos) {
            this.index = index;
            this.isOpen = isOpen;
            this.text = text;
            this.pos = pos;
        }

        //////////////////////////////////
        // Needed for Maps.difference call
        //////////////////////////////////

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StarInfo starInfo = (StarInfo) o;
            return index == starInfo.index && isOpen == starInfo.isOpen && text.equals(starInfo.text) && pos.equals(starInfo.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, isOpen, text, pos);
        }
    }
}
