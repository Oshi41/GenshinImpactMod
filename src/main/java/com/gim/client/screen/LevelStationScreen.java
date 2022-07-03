package com.gim.client.screen;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.ConstellationMenu;
import com.gim.menu.LevelStationMenu;
import com.gim.players.base.AscendInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class LevelStationScreen extends GenshinScreenBase<LevelStationMenu> {
    private final static ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
    private Button applyBtn;
    private Button left;
    private Button right;

    public LevelStationScreen(LevelStationMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/level_station/level_station.png"));

        this.titleLabelX = 111;
        this.titleLabelY = 9;

        this.imageWidth = 176;
        this.imageHeight = 256;
    }

    @Override
    protected void init() {
        super.init();

        TranslatableComponent component = new TranslatableComponent(GenshinImpactMod.ModID + ".upgrade");
        int buttonWidth = 56;

        applyBtn = addRenderableWidget(new Button(this.leftPos + 98, this.topPos + 151, buttonWidth, 18, component,
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 3)
        ));

        left = addRenderableWidget(new Button(this.leftPos + 79, this.topPos + 151, 18, 18, new TextComponent("<"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0)));

        right = addRenderableWidget(new Button(this.leftPos + 155, this.topPos + 151, 18, 18, new TextComponent(">"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 1)));
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(p_97787_, p_97788_, p_97789_, p_97790_);

        // control activating button
        applyBtn.active = getMenu().canApply();

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        GenshinEntityData entityData = getMenu().current();

        // render model player
        IGenshinPlayer assotiatedPlayer = entityData.getAssotiatedPlayer();

        // render current model
        renderEntityInInventory(i + 51 - 20,
                j + 75 - 5,
                30,
                (float) (i + 51) - this.xMouse,
                (float) (j + 75 - 50 - 10) - this.yMouse,
                this.minecraft.player,
                assotiatedPlayer);

        AscendInfo forCurrent = getMenu().getForCurrent();
        if (forCurrent == null)
            return;

        // render ghost items above the item stack
        for (int k = 0; k < forCurrent.materials.size(); k++) {
            ItemStack itemStack = forCurrent.materials.get(k);
            Slot firstSlot = getMenu().getSlot(0);
            int xStart = i + firstSlot.x + 18 * k;
            int yStart = j + firstSlot.y - 18;

            // draw items and it's count
            this.itemRenderer.renderAndDecorateItem(itemStack, xStart, yStart);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, xStart, yStart);

            if (xStart <= xMouse && xMouse <= xStart + 16
                    &&
                    yStart <= yMouse && yMouse <= yStart + 16) {
                // tooltip for curernt stack
                renderTooltip(p_97787_, itemStack, (int) xMouse, (int) yMouse);
            }
        }

        // full information about current ascending
        ArrayList<Component> texts = Lists.newArrayList(forCurrent.info.toArray(Component[]::new));

        // need to show exp condition
        if (forCurrent.playerLevels > 0) {
            ChatFormatting formatting = getMinecraft().player.experienceLevel >= forCurrent.playerLevels || getMinecraft().player.isCreative()
                    ? ChatFormatting.GREEN
                    : ChatFormatting.RED;
            texts.add(TextComponent.EMPTY);
            texts.add(new TranslatableComponent("container.enchant.level.requirement", forCurrent.playerLevels).withStyle(formatting));
        }

        // Cooldown till player ascending
        if (getMenu().ticksToNextLevel() > 0) {
            Duration duration = Duration.ofSeconds(getMenu().ticksToNextLevel() / 20);
            // list of time units (days, hours, etc.)
            List<String> list = new ArrayList<>();

            // collect all possible date units
            List<Function<Duration, Number>> functions = List.of(
                    Duration::toDaysPart,
                    Duration::toHoursPart,
                    Duration::toMinutesPart,
                    Duration::toSecondsPart
            );

            DecimalFormat decimalFormat = new DecimalFormat("00");

            // adding to resulting list
            for (Function<Duration, Number> function : functions) {
                list.add(decimalFormat.format(function.apply(duration)));
            }

            // header with text
            MutableComponent component = new TranslatableComponent(GenshinImpactMod.ModID + ".level_ascending_cooldown")
                    .append(" ")
                    .append(new TextComponent(String.join(":", list)).withStyle(getMinecraft().player.isCreative() ? ChatFormatting.GREEN : ChatFormatting.RED));
            texts.add(component);
        }

        // draw name
        drawCenteredString(p_97787_, getMinecraft().font, assotiatedPlayer.getName().withStyle(ChatFormatting.UNDERLINE), i + 111, j + 27, -1);

        for (int k = 0; k < texts.size(); k++) {
            Component text = texts.get(k);
            int x = i + 58;
            int y = j + 27 + ((k + 2) * getMinecraft().font.lineHeight);

            if (j + 27 + (6 * getMinecraft().font.lineHeight) <= y && y < j + 27 + (11 * getMinecraft().font.lineHeight)) {
                x = i + 5;
            }

            getMinecraft().font.draw(p_97787_, text, x, y, -1);
        }


        RenderSystem.setShaderTexture(0, QUESTIONMARK_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(p_97787_, i + 155, j + 3, 0, 0, 16, 16, 32, 16);


        if (i + 155 <= xMouse && xMouse <= i + 155 + 16
                &&
                j + 3 <= yMouse && yMouse <= j + 3 + 16) {
            List<Component> components = from(entityData.getAttributes());
            renderTooltip(p_97787_, components, Optional.empty(), (int) xMouse, (int) yMouse);
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        drawCenteredString(p_97808_, this.font, this.title, this.titleLabelX, this.titleLabelY, -1);
        // this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    public static List<Component> from(AttributeMap map) {
        Stream<Attribute> attributeStream = ForgeRegistries.ATTRIBUTES.getValues().stream().filter(x ->
                // restricted attributes
                Attributes.MAX_HEALTH.equals(x)
                        || Attributes.ARMOR.equals(x)
                        || Attributes.ATTACK_DAMAGE.equals(x)
                        || Attributes.ATTACK_SPEED.equals(x)
                        || Attributes.JUMP_STRENGTH.equals(x)
                        // genshin attributes and NOT HIDDEN
                        || (x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID) && !x.getDescriptionId().contains("hidden")));

        HashMap<Attribute, String> writeMap = new HashMap<>();

        for (Attribute attribute : attributeStream.toList()) {
            if (map.hasAttribute(attribute)) {
                double value = map.getValue(attribute);
                writeMap.put(attribute, new DecimalFormat("####.##").format(value));
            }
        }

        int length = writeMap.values().stream().max(Comparator.comparing(String::length)).orElse("1234567").length();

        return writeMap.entrySet().stream()
                // need to replace minecraft as the top most string, so decided to replace it by 'a'
                .sorted(Comparator.<Map.Entry<Attribute, String>, String>comparing(e -> e.getKey().getRegistryName().toString().replace("minecraft", "a"))
                        // than comparing by it's localized name
                        .thenComparing(e -> new TranslatableComponent(e.getKey().getDescriptionId()).getString()))
                .map(e -> (Component)
                        new TextComponent("")
                                .append(new TextComponent(String.format("%1$" + length + "s ", e.getValue())).withStyle(ChatFormatting.GRAY))
                                .append(new TranslatableComponent(e.getKey().getDescriptionId())))
                .toList();
    }
}
