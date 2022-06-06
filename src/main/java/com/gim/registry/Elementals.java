package com.gim.registry;

import com.gim.attack.GenshinDamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Elementals {
    PYRO("pyro", Effects.PYRO, Attributes.pyro_bonus, Attributes.pyro_resistance, DamageSource::setIsFire, null, ChatFormatting.RED),
    HYDRO("hydro", Effects.HYDRO, Attributes.hydro_bonus, Attributes.hydro_resistance, null, null, ChatFormatting.DARK_BLUE),
    CRYO("cryo", Effects.CRYO, Attributes.cryo_bonus, Attributes.cryo_resistance, DamageSource::bypassArmor, DamageSource.FREEZE::equals, ChatFormatting.AQUA),
    ELECTRO("electro", Effects.ELECTRO, Attributes.electro_bonus, Attributes.electro_resistance, null, DamageSource.LIGHTNING_BOLT::equals, ChatFormatting.LIGHT_PURPLE),
    DENDRO("dendro", Effects.DENDRO, Attributes.dendro_bonus, Attributes.dendro_resistance, null, null, ChatFormatting.DARK_GREEN),
    ANEMO("anemo", Effects.ANEMO, Attributes.anemo_bonus, Attributes.anemo_resistance, null, null, ChatFormatting.WHITE),
    GEO("geo", Effects.GEO, Attributes.geo_bonus, Attributes.geo_resistance, null, null, ChatFormatting.GOLD),
    SUPERCONDUCT("superconduct", Effects.DEFENCE_DEBUFF, Attributes.cryo_bonus, Attributes.cryo_resistance, DamageSource::bypassArmor, null, ChatFormatting.LIGHT_PURPLE),
    FROZEN("frozen", Effects.FROZEN, null, null, null, null, ChatFormatting.AQUA),
    ELECTROCHARGED("electrocharged", Effects.ELECTROCHARGED, null, null, null, null, ChatFormatting.LIGHT_PURPLE),
    BURNING("burning", Effects.BURNING, null, null, null, null, ChatFormatting.RED),
    ;

    private final String id;
    private final Attribute bonus;
    private final Attribute resistance;
    private final Function<DamageSource, DamageSource> transform;
    private final Predicate<DamageSource> additionalCheck;
    private final MobEffect effect;

    private final ChatFormatting chatColor;

    Elementals(String id, MobEffect effect, Attribute bonus, Attribute resistance, Function<DamageSource, DamageSource> possibleTransform, Predicate<DamageSource> additionalCheck, ChatFormatting chatColor) {
        this.chatColor = chatColor;
        Objects.requireNonNull(id, "ID reqiured");
        Objects.requireNonNull(effect, "Effect required");

        this.id = id;
        this.bonus = bonus;
        this.resistance = resistance;
        this.transform = possibleTransform == null ? x -> x : possibleTransform;
        this.effect = effect;
        this.additionalCheck = additionalCheck == null ? x -> false : additionalCheck;
    }

    public boolean is(DamageSource source) {
        return additionalCheck.test(source) || source instanceof GenshinDamageSource && this.equals(((GenshinDamageSource) source).getElement());
    }

    public boolean is(LivingEntity e) {
        return e != null && e.hasEffect(effect);
    }

    public GenshinDamageSource create(@Nullable Entity attacker) {
        DamageSource raw = transform.apply(new GenshinDamageSource(transform.apply(new DamageSource(id)), attacker));

        if (!(raw instanceof GenshinDamageSource)) {
            String msg = String.format("Transformer for %s enum must return same type of DamageSource!", getClass().getName());
            throw new ReportedException(CrashReport.forThrowable(new Exception(msg), msg));
        }

        GenshinDamageSource result = (GenshinDamageSource) raw;
        result.withElement(this);
        return result;
    }

    public MobEffect getEffect() {
        return effect;
    }

    @Nullable
    public Attribute getBonus() {
        return bonus;
    }

    public Attribute getResistance() {
        return resistance;
    }

    public ChatFormatting getChatColor() {
        return chatColor;
    }
}
