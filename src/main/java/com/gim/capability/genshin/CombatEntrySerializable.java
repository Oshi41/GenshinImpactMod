package com.gim.capability.genshin;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import org.antlr.v4.runtime.misc.EqualityComparator;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class CombatEntrySerializable {
    private static final Lazy<List<DamageSource>> loadDefaults = Lazy.of(() -> {
        return Arrays.stream(DamageSource.class.getDeclaredFields()).filter(x -> Modifier.isStatic(x.getModifiers()) && Modifier.isFinal(x.getModifiers()))
                .map(x -> {
                    try {
                        return x.get(null);
                    } catch (IllegalAccessException e) {
                        GenshinImpactMod.LOGGER.debug(e);
                        return null;
                    }
                }).filter(x -> x instanceof DamageSource)
                .map(x -> ((DamageSource) x))
                .toList();
    });

    private static boolean equals(DamageSource x, DamageSource y) {
        if (x == null && y == null)
            return true;

        if (x == null || y == null)
            return false;

        return x.getMsgId().equals(y.getMsgId())
                && Objects.equals(x.isFire(), y.isFire())
                && Objects.equals(x.isMagic(), y.isMagic())
                && Objects.equals(x.isBypassArmor(), y.isBypassArmor())
                && Objects.equals(x.isBypassInvul(), y.isBypassInvul())
                && Objects.equals(x.isBypassMagic(), y.isBypassMagic())
                && Objects.equals(x.isExplosion(), y.isExplosion())
                && Objects.equals(x.isFall(), y.isFall())
                && Objects.equals(x.isNoAggro(), y.isNoAggro())
                && Objects.equals(x.isProjectile(), y.isProjectile())
                && Objects.equals(x.isDamageHelmet(), y.isDamageHelmet());
    }

    public static CompoundTag serialize(DamageSource source) {
        CompoundTag damageSourceTag = new CompoundTag();

        damageSourceTag.putString("DamageSourceClass", source.getClass().getName());
        damageSourceTag.putString("DamageSource", source.getMsgId());

        if (source.getEntity() != null) {
            damageSourceTag.putInt("Entity", source.getEntity().getId());
        }

        if (source.getDirectEntity() != null) {
            damageSourceTag.putInt("DirectEntity", source.getDirectEntity().getId());
        }

        damageSourceTag.putBoolean("Fire", source.isFire());
        damageSourceTag.putBoolean("Magic", source.isMagic());
        damageSourceTag.putBoolean("Helmet", source.isDamageHelmet());
        damageSourceTag.putBoolean("Bypass", source.isBypassArmor());
        damageSourceTag.putBoolean("BypassInvul", source.isBypassInvul());
        damageSourceTag.putBoolean("Explosion", source.isExplosion());
        damageSourceTag.putBoolean("Fall", source.isFall());
        damageSourceTag.putBoolean("NoAggro", source.isNoAggro());
        damageSourceTag.putBoolean("Projectile", source.isProjectile());
        damageSourceTag.putBoolean("Scale", source.scalesWithDifficulty());

        if (source instanceof GenshinDamageSource) {
            damageSourceTag.putBoolean("Skill", ((GenshinDamageSource) source).isSkill());
            damageSourceTag.putBoolean("Burst", ((GenshinDamageSource) source).isBurst());
            damageSourceTag.putBoolean("IgnoreBonus", ((GenshinDamageSource) source).shouldIgnoreBonus());
            damageSourceTag.putBoolean("IgnoreResist", ((GenshinDamageSource) source).shouldIgnoreResistance());

            damageSourceTag.put("InnerSource", serialize(((GenshinDamageSource) source).getInnerSource()));
        }

        if (source instanceof EntityDamageSource) {
            damageSourceTag.putBoolean("Thorns", ((EntityDamageSource) source).isThorns());
        }

        return damageSourceTag;
    }

    public static DamageSource deserializeDamageSource(Level level, CompoundTag nbt) {
        Class<? extends DamageSource> damageSourceClass = DamageSource.class;
        String msg = nbt.getString("DamageSource");
        int entity = nbt.getInt("Entity");
        int directEntity = nbt.getInt("DirectEntity");

        DamageSource source = new DamageSource(msg);

        try {
            damageSourceClass = (Class<? extends DamageSource>) Class.forName(nbt.getString("DamageSourceClass"));
        } catch (ClassNotFoundException e) {
            GenshinImpactMod.LOGGER.debug(e);
        }

        if (damageSourceClass.equals(EntityDamageSource.class)) {
            EntityDamageSource entityDamageSource = new EntityDamageSource(msg, level.getEntity(entity));
            if (nbt.getBoolean("Thorns")) {
                entityDamageSource.setThorns();
            }
            source = entityDamageSource;
        }

        if (damageSourceClass.equals(IndirectEntityDamageSource.class)) {
            source = new IndirectEntityDamageSource(msg, level.getEntity(directEntity), level.getEntity(entity));
        }

        if (damageSourceClass.equals(GenshinDamageSource.class)) {
            GenshinDamageSource genshinDamageSource = new GenshinDamageSource(deserializeDamageSource(level, nbt.getCompound("InnerSource")), level.getEntity(entity));

            if (nbt.getBoolean("Skill")) {
                genshinDamageSource.bySkill();
            }

            if (nbt.getBoolean("Burst")) {
                genshinDamageSource.byBurst();
            }

            if (nbt.getBoolean("IgnoreBonus")) {
                genshinDamageSource.ignoreElementalBonus();
            }

            if (nbt.getBoolean("IgnoreResist")) {
                genshinDamageSource.ignoreResistance();
            }

            source = genshinDamageSource;
        }

        if (nbt.getBoolean("Fire")) {
            source.setIsFire();
        }

        if (nbt.getBoolean("Magic")) {
            source.setMagic();
        }

        if (nbt.getBoolean("Helmet")) {
            source.damageHelmet();
        }

        if (nbt.getBoolean("Bypass")) {
            source.bypassArmor();
        }

        if (nbt.getBoolean("BypassInvul")) {
            source.bypassInvul();
        }

        if (nbt.getBoolean("Explosion")) {
            source.setExplosion();
        }

        if (nbt.getBoolean("Fall")) {
            source.setIsFall();
        }

        if (nbt.getBoolean("NoAggro")) {
            source.setNoAggro();
        }

        if (nbt.getBoolean("Projectile")) {
            source.setProjectile();
        }

        if (nbt.getBoolean("Scale")) {
            source.setScalesWithDifficulty();
        }

        final DamageSource toCompare = source;

        return loadDefaults.get().stream().filter(x -> equals(x, toCompare)).findFirst().orElse(source);
    }


    public static CompoundTag serialize(CombatEntry entry) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("Time", entry.getTime());
        tag.putFloat("Damage", entry.getDamage());
        tag.putFloat("Health", entry.getHealthBeforeDamage());
        tag.putString("Location", entry.getLocation() == null ? "" : entry.getLocation());
        tag.putFloat("Fall", entry.getFallDistance());
        tag.put("DamageSource", serialize(entry.getSource()));

        return tag;
    }

    public static CombatEntry deserializeCombatEntry(Level level, CompoundTag nbt) {
        return new CombatEntry(
                deserializeDamageSource(level, nbt.getCompound("DamageSource")),
                nbt.getInt("Time"),
                nbt.getFloat("Damage"),
                nbt.getFloat("Health"),
                nbt.getString("Location"),
                nbt.getFloat("Fall")
        );
    }
}
