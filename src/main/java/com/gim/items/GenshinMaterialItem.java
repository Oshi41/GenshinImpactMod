package com.gim.items;

import com.gim.registry.CreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GenshinMaterialItem extends Item {
    private boolean isFoil;

    public GenshinMaterialItem(Properties properties) {
        super(properties.tab(CreativeTabs.MATERIALS));

        if (properties instanceof GenshinProperties) {
            GenshinProperties genshinProperties = (GenshinProperties) properties;

            isFoil = genshinProperties.isFoil;
        }
    }

    @Override
    public boolean isFoil(ItemStack p_41453_) {
        return isFoil || super.isFoil(p_41453_);
    }

    public static class GenshinProperties extends Properties {
        private boolean isFoil;

        public GenshinProperties foil() {
            isFoil = true;
            return this;
        }
    }
}
