package com.gim.registry;

import com.gim.GenshinImpactMod;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class MaxHealthPatch {

    public static void patchMaxHealth() {
        try {
            ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, ((RangedAttribute) Attributes.MAX_HEALTH),
                    Double.MAX_VALUE, "maxValue");

            GenshinImpactMod.LOGGER.info("MAX_HEALTH attribute was patched to Double.MAX_VALUE");
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.error(e);
            GenshinImpactMod.LOGGER.error("MAX_HEALTH attribute was not patched");
        }
    }
}
