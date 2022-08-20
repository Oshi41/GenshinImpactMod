package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.items.ConstellationItem;
import com.gim.menu.ConstellationMenu;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
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
        this.titleLabelY = 9;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - (18 * 5) - 2;
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
        getMinecraft().gameMode.handleInventoryButtonClick(getMenu().containerId, forward ? 1 : 0);
    }

    private List<StarInfo> create(GenshinEntityData data) {
        IGenshinPlayer assotiatedPlayer = data.getAssotiatedPlayer();

        List<Vec2> poses = assotiatedPlayer.starPoses();
        double value = data.getAttributes().getInstance(Attributes.constellations).getValue();

        ArrayList<StarInfo> result = new ArrayList<>();

        for (int i = 0; i < poses.size(); i++) {
            StarInfo starInfo = new StarInfo(i,
                    value > i,
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

        int openSize = 15;
        int closedSIze = 7;

        StarInfo firstClosed = current.stream().filter(s -> !s.isOpen).findFirst().orElse(null);
        int index = firstClosed != null
                ? firstClosed.index
                : -1;

        for (StarInfo starInfo : current) {
            int size = starInfo.isOpen ? openSize : closedSIze;

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
                    p_93751_ -> {
                        if (index == starInfo.index) {
                            onStarClick(p_93751_, starInfo);
                        }
                    },
                    (Button button, PoseStack stack, int xPos, int yPos) -> renderStackTooltip(stack, starInfo.text, xPos, yPos),
                    TextComponent.EMPTY) {

                @Override
                public void renderButton(PoseStack p_94282_, int p_94283_, int p_94284_, float p_94285_) {
                    super.renderButton(p_94282_, p_94283_, p_94284_, p_94285_);

                    if (index == starInfo.index && isHovered) {
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, NETHER_STAR);

                        int diff = (openSize / 2) - (closedSIze / 2);
                        blit(p_94282_, this.x - diff, this.y - diff, 0, 0, openSize, openSize, openSize, openSize);
                    }
                }
            }));
        }

        Item item = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item1 -> item1 instanceof ConstellationItem)
                .filter(item1 -> Objects.equals(((ConstellationItem) item1).assignedTo.get(), genshinEntityData.getAssotiatedPlayer()))
                .findFirst()
                .orElse(Items.AIR);

        stack = item.getDefaultInstance();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int mouseX, int mouseY) {
        GenshinEntityData genshinEntityData = getMenu().current();
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        ResourceLocation location = genshinEntityData.getAssotiatedPlayer().getRegistryName();
        // fixed constellation image position!
        location = new ResourceLocation(location.getNamespace(), String.format("textures/players/%s/constellation.png", location.getPath()));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, location);

        // start of image position
        i += 34;
        j += 34;

        // refreshing stars by actual texture position (0:0 means image start)
        tryRefresh(genshinEntityData, i, j);

        int size = 128;
        this.blit(poseStack, i, j, 0, 0, size, size, size, size);


        // returning back
        i -= 34;
        j -= 34;
        Slot firstSlot = getMenu().getSlot(0);
        itemRenderer.renderGuiItem(stack, i + firstSlot.x, j + firstSlot.y - 18);

        // should render at the last order to hover everything
        if (i + firstSlot.x <= mouseX && mouseX <= i + firstSlot.x + 16
                &&
                j + firstSlot.y - 18 <= mouseY && mouseY <= j + firstSlot.y - 18 + 16) {
            // render tooltip for star
            renderTooltip(poseStack, stack, mouseX, mouseY);
        }
    }

    private void renderStackTooltip(PoseStack stack, FormattedText text, int xPos, int yPos) {
        renderComponentTooltip(stack, List.of(text), xPos, yPos, Items.AIR.getDefaultInstance());
    }

    void onStarClick(Button btn, StarInfo starInfo) {
        if (!starInfo.isOpen) {
            // need to skip first bit
            int bitMask = (starInfo.index + 1) >> 1;
            this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, bitMask);
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {

        BaseComponent text = getMenu().current().getAssotiatedPlayer().getName().plainCopy();
        text.setStyle(text.getStyle()
                .withColor(getMenu().current().getAssotiatedPlayer().getElemental().getChatColor())
                .withUnderlined(true)
        );


        // Menu title (above)
        drawCenteredString(p_97808_, this.font, this.title, imageWidth / 2, this.titleLabelY, -1);
        // Character text (in a middle)
        drawCenteredString(p_97808_, this.font, text, imageWidth / 2, 34 - 2 - font.lineHeight, -1);
        // inventory text below
        this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
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
            return index == starInfo.index && isOpen == starInfo.isOpen && text.getString().equals(starInfo.text.getString()) && pos.equals(starInfo.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, isOpen, text, pos);
        }
    }
}
