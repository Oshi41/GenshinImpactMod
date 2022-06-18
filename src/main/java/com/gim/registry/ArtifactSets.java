package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.AdventureSet;
import com.gim.artifacts.base.ArtifactSetBase;
import com.gim.artifacts.base.IArtifactSet;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class ArtifactSets {
    public static final IArtifactSet empty = null;
    public static final IArtifactSet adventure_2 = null;
    public static final IArtifactSet adventure_4 = null;

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<IArtifactSet> e) {
        e.getRegistry().registerAll(
                new ArtifactSetBase(new TextComponent(""), new TextComponent("")) {
                    @Override
                    public boolean isWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
                        return false;
                    }

                    @Override
                    public boolean partOf(Item item) {
                        return false;
                    }
                }.setRegistryName(GenshinImpactMod.ModID, "empty"),

                new AdventureSet(2).setRegistryName(GenshinImpactMod.ModID, "adventure_2"),
                new AdventureSet(4).setRegistryName(GenshinImpactMod.ModID, "adventure_4")
        );
    }
}
