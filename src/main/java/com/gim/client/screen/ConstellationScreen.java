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
    private final Map<IGenshinPlayer, List<StarInfo>> info = new LinkedHashMap<>();
    private final Map<IGenshinPlayer, ItemStack> items = new LinkedHashMap<>();
    private IGenshinPlayer current;

    private static final ResourceLocation GUI = new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/star_worktable/star_worktable_gui.png");
    private static final ResourceLocation NETHER_STAR = new ResourceLocation("textures/item/nether_star.png");
    private final Inventory inventory;
    private Button left;
    private Button right;
    private int index = 0;

    private final List<ImageButton> starsButtons = new ArrayList<>();

    public ConstellationScreen(ConstellationMenu p_97741_, Inventory inventory, Component p_97743_) {
        super(p_97741_, inventory, p_97743_);
        this.inventory = inventory;

        this.imageWidth = 176;
        this.imageHeight = 256;

        this.titleLabelX = 50;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - (18 * 5) - 4;

        info.putAll(initStars());
    }

    private Map<IGenshinPlayer, List<StarInfo>> initStars() {
        IGenshinInfo genshinInfo = inventory.player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);

        if (genshinInfo == null || genshinInfo.getAllPersonages().isEmpty()) {
            this.removed();
            return new HashMap<>();
        }

        HashMap<IGenshinPlayer, List<StarInfo>> result = new HashMap<>();

        for (IGenshinPlayer player : genshinInfo.getAllPersonages()) {
            GenshinEntityData personInfo = genshinInfo.getPersonInfo(player);
            List<StarInfo> stars = new ArrayList<>();
            result.put(player, stars);

            // get all star poses
            List<Vec2> starPoses = player.starPoses();

            // get current star count
            int count = (int) personInfo.getAttributes().getInstance(Attributes.constellations).getValue();

            // rendering based on total star count
            for (int i = 0; i < starPoses.size(); i++) {
                stars.add(new StarInfo(i,
                        count > i,
                        FormattedText.of(new TranslatableComponent(String.format("%s.%s.star.%s", player.getRegistryName().getNamespace(), player.getRegistryName().getPath(), i + 1)).getString()),
                        starPoses.get(i)));
            }
        }

        return result;
    }

    @Override
    protected void init() {
        super.init();

        left = addRenderableWidget(new Button(this.leftPos + 4, this.topPos + 4, 18, 18, new TextComponent("<"), p_93751_ -> navigate(index - 1)));
        right = addRenderableWidget(new Button(this.leftPos + this.imageWidth - 4 - 18, this.topPos + 4, 18, 18, new TextComponent(">"), p_93751_ -> navigate(index + 1)));

        navigate(0);
    }

    private boolean navigate(int nextIndex) {
        index = Mth.clamp(nextIndex, 0, info.size());

        left.active = index > 0;
        right.active = index < info.size() - 1;
        current = Iterators.get(info.keySet().iterator(), index);

        if (!items.containsKey(current)) {
            Item item = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(x -> x instanceof ConstellationItem && Objects.equals(((ConstellationItem) x).assignedTo.get(), current))
                    .findFirst()
                    .orElse(Items.AIR);

            items.put(current, item.getDefaultInstance());
        }

        // removing prev buttons
        for (ImageButton button : starsButtons) {
            removeWidget(button);
        }

        starsButtons.clear();
        return true;
    }

    public void refresh(boolean safe) {
        Map<IGenshinPlayer, List<StarInfo>> newOnes = initStars();
        if (!safe || !Maps.difference(newOnes, this.info).areEqual()) {
            info.clear();
            info.putAll(newOnes);
            navigate(index);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int p_94716_, int p_94717_) {
        return switch (keyCode) {
            case InputConstants.KEY_LEFT -> navigate(index - 1);
            case InputConstants.KEY_RIGHT -> navigate(index + 1);
            default -> super.keyReleased(keyCode, p_94716_, p_94717_);
        };
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        if (inventory.player.tickCount % 20 == 0) {
            refresh(true);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        ResourceLocation location = current.getRegistryName();
        location = new ResourceLocation(location.getNamespace(), String.format("textures/players/%s/constellation.png", location.getPath()));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, location);

        i += 34;
        j += 34;
        int size = 128;
        this.blit(poseStack, i, j, 0, 0, size, size, size, size);

        // need to create new buttons
        if (starsButtons.isEmpty() && current != null && info.get(current) != null) {
            for (StarInfo starInfo : info.get(current)) {
                size = starInfo.isOpen ? 15 : 7;

                int x = (int) (i + starInfo.pos.x) - size / 2;
                int y = (int) (j + starInfo.pos.y) - size / 2;

                starsButtons.add(addRenderableWidget(new ImageButton(
                        x,
                        y,
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
        }

        for (int i1 = 0; i1 < starsButtons.size() - 1; i1++) {
            ImageButton first = starsButtons.get(i1);
            ImageButton second = starsButtons.get(i1 + 1);

            hLine(poseStack, first.x, first.y, second.x, second.y);
        }
    }

    private void renderStackTooltip(PoseStack stack, FormattedText text, int xPos, int yPos) {
        renderComponentTooltip(stack, List.of(text), xPos, yPos, Items.AIR.getDefaultInstance());
    }

    void onStarClick(Button p_93751_, StarInfo starInfo) {
        if (!starInfo.isOpen) {
            // must click next star
            if (starInfo.index > 0 && !this.info.get(current).get(starInfo.index - 1).isOpen) {
                return;
            }

            // find input slot
            Slot slot = this.menu.getSlot(0);
            if (slot.hasItem()) {
                // sending to server
                GenshinImpactMod.CHANNEL.sendToServer(new StarClickedPackage(current));
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        renderGhostSlot(p_97808_);

        super.renderLabels(p_97808_, p_97809_, p_97810_);

        BaseComponent text = current.getName().plainCopy();
        text.setStyle(text.getStyle()
                .withColor(current.getElemental().getChatColor())
                .withUnderlined(true)
        );

        this.font.draw(p_97808_, text, (float) this.titleLabelX, (float) this.titleLabelY + 18, 4210752);
    }

    @Override
    public void render(PoseStack p_97795_, int x, int y, float p_97798_) {
        super.render(p_97795_, x, y, p_97798_);
        renderTooltip(p_97795_, x, y);
    }

    @Override
    protected void renderTooltip(PoseStack p_97791_, int x, int y) {
        super.renderTooltip(p_97791_, x, y);

        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index == 0) {
            ItemStack itemStack = items.get(current);
            renderTooltip(p_97791_, itemStack, x, y);
        }
    }


    private void renderGhostSlot(PoseStack poseStack) {

        Slot inputSlot = this.menu.slots.get(0);
        if (inputSlot.isActive() && !inputSlot.hasItem() && Objects.equals(hoveredSlot, inputSlot) && !this.isDragging()) {

            ItemStack itemStack = items.get(current);
            if (!itemStack.isEmpty()) {
                BakedModel model = this.itemRenderer.getModel(itemStack, null, null, 0);
                if (model != null) {
                    setBlitOffset(100);
                    this.itemRenderer.blitOffset = 100;

                    RenderSystem.setShaderTexture(0, model.getParticleIcon().atlas().location());
                    RenderSystem.setShaderColor(1, 1, 1, 0.2f);
                    innerBlit(poseStack, inputSlot.x, inputSlot.y, this.getBlitOffset(), 16, 16, model.getParticleIcon(), 1, 1, 1, 0.2f);

                    setBlitOffset(0);
                    this.itemRenderer.blitOffset = 0;
                }
            }
        }
    }

    private void innerBlit(PoseStack p_93201_, int p_93202_, int p_93203_, int p_93204_, int p_93205_, int p_93206_, TextureAtlasSprite p_93207_,
                           float red, float green, float blue, float alpha) {
        this.innerBlit(p_93201_.last().pose(), p_93202_, p_93202_ + p_93205_, p_93203_, p_93203_ + p_93206_, p_93204_, p_93207_.getU0(), p_93207_.getU1(), p_93207_.getV0(), p_93207_.getV1(),
                red, green, blue, alpha);
    }

    private void innerBlit(Matrix4f p_93113_, int p_93114_, int p_93115_, int p_93116_, int p_93117_, int p_93118_, float p_93119_, float p_93120_, float p_93121_, float p_93122_,
                           float red, float green, float blue, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(p_93113_, (float) p_93114_, (float) p_93117_, (float) p_93118_).color(red, green, blue, alpha).uv(p_93119_, p_93122_).endVertex();
        bufferbuilder.vertex(p_93113_, (float) p_93115_, (float) p_93117_, (float) p_93118_).color(red, green, blue, alpha).uv(p_93120_, p_93122_).endVertex();
        bufferbuilder.vertex(p_93113_, (float) p_93115_, (float) p_93116_, (float) p_93118_).color(red, green, blue, alpha).uv(p_93120_, p_93121_).endVertex();
        bufferbuilder.vertex(p_93113_, (float) p_93114_, (float) p_93116_, (float) p_93118_).color(red, green, blue, alpha).uv(p_93119_, p_93121_).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
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
