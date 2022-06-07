package com.gim.capability.genshin;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import java.lang.reflect.Modifier;
import java.util.*;

@Deprecated
public class CombatEntrySerializable {
//    private static final Lazy<List<DamageSource>> loadDefaults = Lazy.of(() -> Arrays.stream(DamageSource.class.getDeclaredFields()).filter(x -> Modifier.isStatic(x.getModifiers()) && Modifier.isFinal(x.getModifiers()))
//            .map(x -> {
//                try {
//                    return x.get(null);
//                } catch (IllegalAccessException e) {
//                    GenshinImpactMod.LOGGER.debug(e);
//                    return null;
//                }
//            }).filter(x -> x instanceof DamageSource)
//            .map(x -> ((DamageSource) x))
//            .toList());
//
//    private static boolean equals(DamageSource x, DamageSource y) {
//        if (x == null && y == null)
//            return true;
//
//        if (x == null || y == null)
//            return false;
//
//        return x.getMsgId().equals(y.getMsgId())
//                && Objects.equals(x.isFire(), y.isFire())
//                && Objects.equals(x.isMagic(), y.isMagic())
//                && Objects.equals(x.isBypassArmor(), y.isBypassArmor())
//                && Objects.equals(x.isBypassInvul(), y.isBypassInvul())
//                && Objects.equals(x.isBypassMagic(), y.isBypassMagic())
//                && Objects.equals(x.isExplosion(), y.isExplosion())
//                && Objects.equals(x.isFall(), y.isFall())
//                && Objects.equals(x.isNoAggro(), y.isNoAggro())
//                && Objects.equals(x.isProjectile(), y.isProjectile())
//                && Objects.equals(x.isDamageHelmet(), y.isDamageHelmet());
//    }
//
//    public static CompoundTag serialize(DamageSource source) {
//        CompoundTag damageSourceTag = new CompoundTag();
//
//        damageSourceTag.putString("DamageSourceClass", source.getClass().getName());
//        damageSourceTag.putString("DamageSource", source.getMsgId());
//
//        if (source.getEntity() != null) {
//            damageSourceTag.putInt("Entity", source.getEntity().getId());
//        }
//
//        if (source.getDirectEntity() != null) {
//            damageSourceTag.putInt("DirectEntity", source.getDirectEntity().getId());
//        }
//
//        if (source.isFire()) {
//            damageSourceTag.putBoolean("Fire", true);
//        }
//
//        if (source.isMagic()) {
//            damageSourceTag.putBoolean("Magic", true);
//        }
//
//        if (source.isDamageHelmet()) {
//            damageSourceTag.putBoolean("Helmet", true);
//        }
//
//        if (source.isBypassArmor()) {
//            damageSourceTag.putBoolean("Bypass", true);
//        }
//
//        if (source.isBypassMagic()) {
//            damageSourceTag.putBoolean("BypassMagic", true);
//        }
//
//        if (source.isBypassInvul()) {
//            damageSourceTag.putBoolean("BypassInvul", true);
//        }
//
//        if (source.isExplosion()) {
//            damageSourceTag.putBoolean("Explosion", true);
//        }
//
//        if (source.isFall()) {
//            damageSourceTag.putBoolean("Fall", true);
//        }
//
//        if (source.isNoAggro()) {
//            damageSourceTag.putBoolean("NoAggro", true);
//        }
//
//        if (source.isProjectile()) {
//            damageSourceTag.putBoolean("Projectile", true);
//        }
//
//        if (source.scalesWithDifficulty()) {
//            damageSourceTag.putBoolean("Scale", true);
//        }
//
//        if (source instanceof GenshinDamageSource) {
//            GenshinDamageSource genshinDamageSource = (GenshinDamageSource) source;
//
//            if (genshinDamageSource.skillOf() != null)
//                damageSourceTag.putString("Skill", genshinDamageSource.skillOf().getRegistryName().toString());
//
//            if (genshinDamageSource.burstOf() != null)
//                damageSourceTag.putString("Burst", genshinDamageSource.burstOf().getRegistryName().toString());
//
//            if (genshinDamageSource.shouldIgnoreBonus())
//                damageSourceTag.putBoolean("IgnoreBonus", true);
//
//            if (genshinDamageSource.shouldIgnoreResistance())
//                damageSourceTag.putBoolean("IgnoreResist", true);
//
//            damageSourceTag.put("InnerSource", serialize(genshinDamageSource.getInnerSource()));
//        }
//
//        if (source instanceof EntityDamageSource) {
//            if (((EntityDamageSource) source).isThorns())
//                damageSourceTag.putBoolean("Thorns", true);
//        }
//
//        return damageSourceTag;
//    }
//
//    public static DamageSource deserializeDamageSource(Level level, CompoundTag nbt) {
//        Class<? extends DamageSource> damageSourceClass = DamageSource.class;
//        String msg = nbt.getString("DamageSource");
//        Entity entity = null;
//        Entity directEntity = null;
//
//        if (nbt.contains("Entity")) {
//            entity = level.getEntity(nbt.getInt("Entity"));
//        }
//
//        if (nbt.contains("DirectEntity")) {
//            directEntity = level.getEntity(nbt.getInt("DirectEntity"));
//        }
//
//        DamageSource source = new DamageSource(msg);
//
//        try {
//            damageSourceClass = (Class<? extends DamageSource>) Class.forName(nbt.getString("DamageSourceClass"));
//        } catch (ClassNotFoundException e) {
//            GenshinImpactMod.LOGGER.debug(e);
//        }
//
//        if (damageSourceClass.equals(EntityDamageSource.class)) {
//            EntityDamageSource entityDamageSource = new EntityDamageSource(msg, entity);
//            if (nbt.getBoolean("Thorns")) {
//                entityDamageSource.setThorns();
//            }
//            source = entityDamageSource;
//        }
//
//        if (damageSourceClass.equals(IndirectEntityDamageSource.class)) {
//            source = new IndirectEntityDamageSource(msg, directEntity, entity);
//        }
//
//        if (damageSourceClass.equals(GenshinDamageSource.class)) {
//            GenshinDamageSource genshinDamageSource = new GenshinDamageSource(deserializeDamageSource(level, nbt.getCompound("InnerSource")), entity);
//
////            if (nbt.getBoolean("Skill")) {
////                genshinDamageSource.bySkill();
////            }
////
////            if (nbt.getBoolean("Burst")) {
////                genshinDamageSource.byBurst();
////            }
//
//            if (nbt.getBoolean("IgnoreBonus")) {
//                genshinDamageSource.ignoreElementalBonus();
//            }
//
//            if (nbt.getBoolean("IgnoreResist")) {
//                genshinDamageSource.ignoreResistance();
//            }
//
//            source = genshinDamageSource;
//        }
//
//        if (nbt.getBoolean("Fire")) {
//            source.setIsFire();
//        }
//
//        if (nbt.getBoolean("Magic")) {
//            source.setMagic();
//        }
//
//        if (nbt.getBoolean("Helmet")) {
//            source.damageHelmet();
//        }
//
//        if (nbt.getBoolean("Bypass")) {
//            source.bypassArmor();
//        }
//
//        if (nbt.getBoolean("BypassMagic")) {
//            source.bypassMagic();
//        }
//
//        if (nbt.getBoolean("BypassInvul")) {
//            source.bypassInvul();
//        }
//
//        if (nbt.getBoolean("Explosion")) {
//            source.setExplosion();
//        }
//
//        if (nbt.getBoolean("Fall")) {
//            source.setIsFall();
//        }
//
//        if (nbt.getBoolean("NoAggro")) {
//            source.setNoAggro();
//        }
//
//        if (nbt.getBoolean("Projectile")) {
//            source.setProjectile();
//        }
//
//        if (nbt.getBoolean("Scale")) {
//            source.setScalesWithDifficulty();
//        }
//
//        // checking for same static instance. Usually DamageSource is using by compare it by references
//        if (damageSourceClass.equals(DamageSource.class)) {
//            final DamageSource toCompare = source;
//
//            return loadDefaults.get().stream().filter(x -> equals(x, toCompare)).findFirst().orElse(source);
//        }
//
//        return source;
//    }
//
//
//    public static CompoundTag serialize(CombatEntry entry) {
//        CompoundTag tag = new CompoundTag();
//
//        tag.putInt("Time", entry.getTime());
//        tag.putFloat("Damage", entry.getDamage());
//        tag.putFloat("Health", entry.getHealthBeforeDamage());
//        tag.putString("Location", entry.getLocation() == null ? "" : entry.getLocation());
//        tag.putFloat("Fall", entry.getFallDistance());
//        tag.put("DamageSource", serialize(entry.getSource()));
//
//        return tag;
//    }
//
//    public static CombatEntry deserializeCombatEntry(Level level, CompoundTag nbt) {
//        return new CombatEntry(
//                deserializeDamageSource(level, nbt.getCompound("DamageSource")),
//                nbt.getInt("Time"),
//                nbt.getFloat("Damage"),
//                nbt.getFloat("Health"),
//                nbt.getString("Location"),
//                nbt.getFloat("Fall")
//        );
//    }
}
