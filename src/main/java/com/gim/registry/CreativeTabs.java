package com.gim.registry;

import com.gim.GenshinImpactMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

public class CreativeTabs {
    public static final CreativeModeTab ARTIFACTS = new CreativeModeTab(String.format("%s.artifacts", GenshinImpactMod.ModID)) {
        private final Lazy<ItemStack> icon = Lazy.of(() -> Items.adventure_feather.getDefaultInstance());

        @Override
        public ItemStack makeIcon() {
            return icon.get();
        }
    };

    public static final CreativeModeTab MATERIALS = new CreativeModeTab(String.format("%s.materials", GenshinImpactMod.ModID)) {
        private final Lazy<ItemStack> icon = Lazy.of(() -> Items.resistance_scroll.getDefaultInstance());

        @Override
        public ItemStack makeIcon() {
            return icon.get();
        }
    };
}
