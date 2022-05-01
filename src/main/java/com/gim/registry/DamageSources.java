package com.gim.registry;

import com.gim.GenshinImpactMod;
import net.minecraft.world.damagesource.DamageSource;

public class DamageSources {

    public static final DamageSource HydroSource = new DamageSource(String.format("%s.hydro", GenshinImpactMod.ModID));
    public static final DamageSource AnemoSource = new DamageSource(String.format("%s.anemo", GenshinImpactMod.ModID));
    public static final DamageSource DendroSource = new DamageSource(String.format("%s.dendro", GenshinImpactMod.ModID));
    public static final DamageSource GeoSource = new DamageSource(String.format("%s.geo", GenshinImpactMod.ModID));
}
