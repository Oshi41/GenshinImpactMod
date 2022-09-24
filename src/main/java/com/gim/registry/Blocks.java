package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.blocks.ArhontStatueBlock;
import com.gim.blocks.GenshinAnvilBlock;
import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.menu.*;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Blocks {
    public static final Block star_worktable = null;
    public static final Block artifacts_station = null;
    public static final Block artifacts_forge = null;
    public static final Block level_station = null;
    public static final Block skill_station = null;
    public static final Block wind_astra = null;
    public static final Block anemo_arhont_statue = null;

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        registerBlock(event,
                new FlowerBlock(MobEffects.LEVITATION, 5, BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)),
                new Item.Properties().setNoRepair().tab(CreativeModeTab.TAB_DECORATIONS),
                "wind_astra",
                () -> Blocks::cutout);

        registerBlock(
                event,
                new GenshinCraftingTableBlock(
                        BlockBehaviour.Properties.of(Material.WOOD)
                                .strength(2.5f)
                                .sound(SoundType.WOOD)
                                .requiresCorrectToolForDrops(),
                        ConstellationMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "star_worktable");

        registerBlock(
                event,
                new GenshinCraftingTableBlock(
                        BlockBehaviour.Properties.of(Material.WOOD)
                                .strength(2.5f)
                                .sound(SoundType.WOOD)
                                .requiresCorrectToolForDrops(),
                        ArtifactsStationMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "artifacts_station");

        registerBlock(
                event,
                new GenshinAnvilBlock(
                        BlockBehaviour.Properties.of(Material.HEAVY_METAL)
                                .strength(5, 1000)
                                .sound(SoundType.WOOD)
                                .requiresCorrectToolForDrops(),
                        ArtifactsForgeMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "artifacts_forge");

        registerBlock(
                event,
                new GenshinCraftingTableBlock(
                        BlockBehaviour.Properties.of(Material.WOOD)
                                .strength(2.5f)
                                .sound(SoundType.WOOD)
                                .requiresCorrectToolForDrops(),
                        LevelStationMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "level_station");

        registerBlock(
                event,
                new GenshinCraftingTableBlock(
                        BlockBehaviour.Properties.of(Material.STONE)
                                .strength(2.5f)
                                .sound(SoundType.STONE)
                                .requiresCorrectToolForDrops(),
                        SkillStationMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "skill_station");

        registerBlock(
                event,
                new ArhontStatueBlock(
                        BlockBehaviour.Properties.of(Material.STONE)
                                .strength(-1.0F, 3600000.0F)
                                .noDrops()
                                .isValidSpawn((a, b, c, d) -> false)
                ),
                new Item.Properties().setNoRepair().tab(CreativeTabs.GENSHIN),
                "anemo_arhont_statue",
                () -> Blocks::cutout
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        toRegister.forEach((resourceLocation, properties) -> {
            Block value = ForgeRegistries.BLOCKS.getValue(resourceLocation);

            if (value != null) {
                event.getRegistry().register(new BlockItem(value, properties).setRegistryName(resourceLocation));
            } else {
                String msg = String.format("Cannot register %s item because block with same registry name is not existing", resourceLocation);
                throw new ReportedException(CrashReport.forThrowable(new Exception(msg), msg));
            }
        });

        toRegister.clear();
    }

    private static final Map<ResourceLocation, Item.Properties> toRegister = new HashMap<>();

    private static Block registerBlock(RegistryEvent.Register<Block> event,
                                       Block original,
                                       Item.Properties props,
                                       String name) {
        return registerBlock(event, original, props, name, null);
    }

    private static Block registerBlock(RegistryEvent.Register<Block> event,
                                       Block original,
                                       Item.Properties props,
                                       String name,
                                       @Nullable Supplier<Consumer<Block>> clientCallback) {
        ResourceLocation location = new ResourceLocation(GenshinImpactMod.ModID, name);
        event.getRegistry().register(original.setRegistryName(location));
        toRegister.put(location, props);

        if (clientCallback != null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> clientCallback.get().accept(original));
        }

        return original;
    }

    @OnlyIn(Dist.CLIENT)
    private static void cutout(Block block) {
        com.gim.registry.Renders.RENDER_MAP.put(net.minecraft.client.renderer.RenderType.cutout(), block);
    }

    @OnlyIn(Dist.CLIENT)
    private static void cutoutMipped(Block block) {
        com.gim.registry.Renders.RENDER_MAP.put(net.minecraft.client.renderer.RenderType.cutoutMipped(), block);
    }
}
