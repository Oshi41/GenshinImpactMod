package com.gim.registry;

import com.gim.GenshinImpactMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Attributes {

    public static final Attribute heal_bonus = null;
    public static final Attribute defence = null;
    public static final Attribute elemental_majesty = null;
    public static final Attribute attack_bonus = null;
    public static final Attribute crit_rate = null;
    public static final Attribute crit_bonus = null;
    public static final Attribute skill_cooldown = null;
    public static final Attribute burst_cooldown = null;
    public static final Attribute burst_cost = null;
    public static final Attribute recharge_bonus = null;

    public static final Attribute pyro_bonus = null;
    public static final Attribute anemo_bonus = null;
    public static final Attribute hydro_bonus = null;
    public static final Attribute geo_bonus = null;
    public static final Attribute dendro_bonus = null;
    public static final Attribute cryo_bonus = null;
    public static final Attribute electro_bonus = null;

    public static final Attribute pyro_resistance = null;
    public static final Attribute anemo_resistance = null;
    public static final Attribute hydro_resistance = null;
    public static final Attribute geo_resistance = null;
    public static final Attribute dendro_resistance = null;
    public static final Attribute cryo_resistance = null;
    public static final Attribute electro_resistance = null;

    public static final Attribute level = null;
    public static final Attribute shield_strength = null;

    public static final Attribute skill_level = null;


    @SubscribeEvent
    public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
        IForgeRegistry<Attribute> registry = event.getRegistry();

        registry.registerAll(
                new RangedAttribute("genshin.heal_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "heal_bonus"),
                new RangedAttribute("genshin.defence", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "defence"),
                new RangedAttribute("genshin.elemental_majesty", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "elemental_majesty"),
                new RangedAttribute("genshin.attack_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "attack_bonus"),
                new RangedAttribute("genshin.crit_rate", 0.05, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "crit_rate"),
                new RangedAttribute("genshin.crit_bonus", 1, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "crit_bonus"),
                new RangedAttribute("genshin.skill_cooldown", Double.MAX_VALUE, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "skill_cooldown"),
                new RangedAttribute("genshin.burst_cooldown", Double.MAX_VALUE, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "burst_cooldown"),
                new RangedAttribute("genshin.recharge_bonus", 1, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "recharge_bonus"),
                new RangedAttribute("genshin.level", 1, 1, 8)
                        .setRegistryName(GenshinImpactMod.ModID, "level"),


                new RangedAttribute("genshin.pyro_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "pyro_bonus"),
                new RangedAttribute("genshin.anemo_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "anemo_bonus"),
                new RangedAttribute("genshin.hydro_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "hydro_bonus"),
                new RangedAttribute("genshin.geo_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "geo_bonus"),
                new RangedAttribute("genshin.dendro_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "dendro_bonus"),
                new RangedAttribute("genshin.cryo_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "cryo_bonus"),
                new RangedAttribute("genshin.electro_bonus", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "electro_bonus"),

                new RangedAttribute("genshin.pyro_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "pyro_resistance"),
                new RangedAttribute("genshin.anemo_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "anemo_resistance"),
                new RangedAttribute("genshin.hydro_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "hydro_resistance"),
                new RangedAttribute("genshin.geo_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "geo_resistance"),
                new RangedAttribute("genshin.dendro_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "dendro_resistance"),
                new RangedAttribute("genshin.cryo_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "cryo_resistance"),
                new RangedAttribute("genshin.electro_resistance", 0, Integer.MIN_VALUE, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "electro_resistance"),
                new RangedAttribute("genshin.shield_strength", 1, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "shield_strength"),
                new RangedAttribute("genshin.burst_cost", 90, 0, Double.MAX_VALUE)
                        .setRegistryName(GenshinImpactMod.ModID, "burst_cost"),
                new RangedAttribute("genshin.skill_level", 0, 0, 15)
                        .setRegistryName(GenshinImpactMod.ModID, "skill_level")
        );
    }
}
