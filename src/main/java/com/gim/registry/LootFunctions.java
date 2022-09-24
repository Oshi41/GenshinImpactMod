package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.loot.LevelScaleCountFunction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LootFunctions {
    public static final LootItemFunctionType LEVEL_SCALE_CONDITION = new LootItemFunctionType(new LevelScaleCountFunction.Serializer());


    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<GlobalLootModifierSerializer<?>> e) {
        Registry.register(Registry.LOOT_FUNCTION_TYPE,
                new ResourceLocation(GenshinImpactMod.ModID, "level_scale_condition"),
                LEVEL_SCALE_CONDITION);

    }
}
