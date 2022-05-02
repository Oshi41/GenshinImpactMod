package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Effects {

    @ObjectHolder("elemental_cryo")
    public static final MobEffect CRYO = null;
    @ObjectHolder("elemental_pyro")
    public static final MobEffect PYRO = null;
    @ObjectHolder("elemental_dendro")
    public static final MobEffect DENDRO = null;
    @ObjectHolder("elemental_anemo")
    public static final MobEffect ANEMO = null;
    @ObjectHolder("elemental_geo")
    public static final MobEffect GEO = null;
    @ObjectHolder("elemental_electro")
    public static final MobEffect ELECTRO = null;
    @ObjectHolder("elemental_hydro")
    public static final MobEffect HYDRO = null;

    @ObjectHolder("defence_debuff")
    public static final MobEffect DEFENCE_DEBUFF = null;
    @ObjectHolder("frozen")
    public static final MobEffect FROZEN = null;

    @SubscribeEvent
    public static void onEffectRegistry(final RegistryEvent.Register<MobEffect> event) {
        IForgeRegistry<MobEffect> registry = event.getRegistry();

        registry.registerAll(
                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.WHITE.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_cryo"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.RED.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_pyro"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.GREEN.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_dendro"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.CYAN.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_anemo"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.YELLOW.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_geo"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.YELLOW.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_electro"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.YELLOW.getRGB())
                        .setPureElemental(true)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_hydro"),


                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.WHITE.getRGB())
                        .addAttributeModifier(Attributes.defence, "08ba996a-1ea9-40ab-a4ea-7171d5afdf64", -0.1d, AttributeModifier.Operation.ADDITION)
                        .setRegistryName(GenshinImpactMod.ModID, "defence_debuff"),

                new GenshinMobEffect(MobEffectCategory.HARMFUL, Color.WHITE.getRGB())
                        .setRegistryName(GenshinImpactMod.ModID, "frozen")
        );
    }
}
