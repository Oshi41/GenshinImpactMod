package com.gim.events;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import com.gim.entity.Energy;
import com.gim.entity.TextParticle;
import com.gim.registry.ElementalReactions;
import com.gim.registry.Elementals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShowDamage {
    private static List<Player> cryticalHits = new ArrayList<>();


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyCritHit(CriticalHitEvent e) {
        if (e.getDamageModifier() > 1) {
            cryticalHits.add(e.getPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityHurt(LivingDamageEvent event) {
        if (event.getAmount() > Double.MIN_NORMAL) {
            LivingEntity entityLiving = event.getEntityLiving();
            DamageSource damageSource = event.getSource();

            if (entityLiving != null && damageSource != null) {

                if (!entityLiving.getLevel().isClientSide()) {
                    ChatFormatting format = ChatFormatting.GRAY;

                    if (event.getSource() instanceof GenshinDamageSource) {
                        GenshinDamageSource genshinDamageSource = (GenshinDamageSource) event.getSource();

                        if (genshinDamageSource.getElement() != null) {
                            format = genshinDamageSource.getElement().getChatColor();
                        }

                        if (genshinDamageSource.possibleReaction() != null && GenshinImpactMod.CONFIG.getKey().indicateReactions.get()) {
                            GenshinHeler.showInfo(entityLiving, genshinDamageSource.possibleReaction().text);
                        }
                    }

                    TextComponent component = new TextComponent("" + Math.abs(Math.round(event.getAmount())));

                    Style style = component.getStyle().applyFormat(format);
                    if (cryticalHits.remove(event.getSource().getEntity())) {
                        style = style.withUnderlined(true);

                        // 10% of chance to make energy orb
                        if (entityLiving.getRandom().nextFloat() < 0.5) {
                            // adding element orb here

                            Elementals e = null;
                            if (event.getSource() instanceof GenshinDamageSource) {
                                e = ((GenshinDamageSource) event.getSource()).getElement();
                            }

                            Energy energy = new Energy(event.getSource().getEntity(), entityLiving, 1, e);
                            entityLiving.getLevel().addFreshEntity(energy);
                        }
                    }

                    component.setStyle(style);

                    if (GenshinImpactMod.CONFIG.getKey().indicateDamage.get()) {
                        GenshinHeler.showInfo(entityLiving, component);
                    }
                }
            }
        }
    }
}
