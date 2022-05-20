package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Elementals {
    PYRO("pyro", Effects.PYRO, Attributes.pyro_bonus, Attributes.pyro_resistance, DamageSource::isFire, DamageSource::setIsFire),
    HYDRO("hydro", Effects.HYDRO, Attributes.hydro_bonus, Attributes.hydro_resistance),
    CRYO("cryo", Effects.CRYO, Attributes.cryo_bonus, Attributes.cryo_resistance, DamageSource::bypassArmor, DamageSource.FREEZE),
    ELECTRO("electro", Effects.ELECTRO, Attributes.electro_bonus, Attributes.electro_resistance, DamageSource.LIGHTNING_BOLT),
    DENDRO("dendro", Effects.DENDRO, Attributes.dendro_bonus, Attributes.dendro_resistance),
    ANEMO("anemo", Effects.ANEMO, Attributes.anemo_bonus, Attributes.anemo_resistance),
    GEO("geo", Effects.GEO, Attributes.geo_bonus, Attributes.geo_resistance),
    SUPERCONDUCT("superconduct", Effects.DEFENCE_DEBUFF, Attributes.cryo_bonus, Attributes.cryo_resistance, DamageSource::bypassArmor),
    FROZEN("frozen", Effects.FROZEN),
    ;

    private String id;
    private Predicate<DamageSource> possibleCheck;
    private Function<DamageSource, DamageSource> transform;
    private MobEffect effect;

    private Attribute bonus;
    private Attribute resistance;

    public boolean is(DamageSource source) {
        return source != null && (possibleCheck.test(source) || source.getMsgId().equals(id));
    }

    public boolean is(LivingEntity e) {
        return e != null && e.hasEffect(this.getEffect());
    }

    /**
     * Created damage source for current elemental
     *
     * @param attacker - possible attacker
     * @return DamageSource/GenshinDamageSource if attacker is not null
     */
    public DamageSource create(@Nullable Entity attacker) {
        DamageSource source = new DamageSource(id);
        if (attacker != null) {
            source = new GenshinDamageSource(source, attacker);
        }

        if (transform != null) {
            source = transform.apply(source);
        }

        return source;
    }

    public DamageSource create() {
        return create(null);
    }

    public MobEffect getEffect() {
        return effect;
    }

    @Nullable
    public Attribute getBonus() {
        return bonus;
    }

    @Nullable
    public Attribute getResistance() {
        return resistance;
    }

    Elementals(String id, MobEffect effect) {
        this(id, effect, null, null);
    }

    Elementals(String id, MobEffect effect, Attribute bonus, Attribute resistance) {
        this(id, effect, bonus, resistance, x -> false, source -> source);
    }

    Elementals(String id, MobEffect effect, Attribute bonus, Attribute resistance, DamageSource... embedded) {
        this(
                id,
                effect,
                bonus,
                resistance,
                source -> source,
                embedded
        );
    }

    Elementals(String id, MobEffect effect, Function<DamageSource, DamageSource> transform) {
        this(id, effect, null, null, transform);
    }

    Elementals(String id, MobEffect effect, Attribute bonus, Attribute resistance, Function<DamageSource, DamageSource> transform, DamageSource... embedded) {
        this(
                id,
                effect,
                bonus,
                resistance,
                source -> embedded != null && embedded.length > 0 && Arrays.asList(embedded).contains(source),
                transform
        );
    }

    Elementals(String id, MobEffect effect, Attribute bonus, Attribute resistance, Predicate<DamageSource> possibleCheck, Function<DamageSource, DamageSource> transform) {
        this.id = GenshinImpactMod.ModID + "." + id;
        this.possibleCheck = possibleCheck;
        this.transform = transform;
        this.effect = effect;
        this.bonus = bonus;
        this.resistance = resistance;
    }
}
