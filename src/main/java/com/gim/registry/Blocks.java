package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.blocks.GenshinAnvilBlock;
import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.menu.ArtifactsForgeMenu;
import com.gim.menu.ArtifactsStationMenu;
import com.gim.menu.ConstellationMenu;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Blocks {
    public static final Block star_worktable = null;
    public static final Block artifacts_station = null;
    public static final Block artifacts_forge = null;

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        registerBlock(
                event,
                new GenshinCraftingTableBlock(
                        BlockBehaviour.Properties.of(Material.WOOD)
                                .strength(2.5f)
                                .sound(SoundType.WOOD)
                                .requiresCorrectToolForDrops(),
                        ConstellationMenu::new
                ),
                new Item.Properties().setNoRepair().tab(CreativeModeTab.TAB_MISC),
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
                new Item.Properties().setNoRepair().tab(CreativeModeTab.TAB_MISC),
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
                new Item.Properties().setNoRepair().tab(CreativeModeTab.TAB_MISC),
                "artifacts_forge");

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

    private static Block registerBlock(RegistryEvent.Register<Block> event, Block original, Item.Properties props, String name) {
        ResourceLocation location = new ResourceLocation(GenshinImpactMod.ModID, name);
        event.getRegistry().register(original.setRegistryName(location));
        toRegister.put(location, props);
        return original;
    }
}
