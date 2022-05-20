package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.networking.GenshinAbilityMsg;
import com.gim.networking.SwitchToPlayerMsg;
import com.gim.registry.Capabilities;
import com.gim.registry.KeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.*;
import java.util.function.*;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBinding {

    @SubscribeEvent
    public static void reciveKey(InputEvent.KeyInputEvent event) {
        if (KeyMappings.PLAYER_1.isDown()) {
            handleSwitch(0);
        }

        if (KeyMappings.PLAYER_2.isDown()) {
            handleSwitch(1);
        }

        if (KeyMappings.PLAYER_3.isDown()) {
            handleSwitch(2);
        }

        if (KeyMappings.PLAYER_4.isDown()) {
            handleSwitch(3);
        }

        if (KeyMappings.SKILL.isDown()) {
            handleAbility(GenshinAbilityMsg.Abilities.SKILL);
        }

        if (KeyMappings.BURST.isDown()) {
            handleAbility(GenshinAbilityMsg.Abilities.BURST);
        }
    }

    /**
     * Handles characters switching
     *
     * @param index - index to switch
     */
    private static void handleSwitch(int index) {
        handle((genshinInfo, player) -> genshinInfo.canSwitchToPlayer(index, player), () -> new SwitchToPlayerMsg(index), (player) -> {
            // SwitchToPlayerMsg.updatePlayer(player, index);
        });
    }

    /**
     * Hadles special ability for character
     *
     * @param type - ability type
     */
    private static void handleAbility(GenshinAbilityMsg.Abilities type) {
        BiPredicate<IGenshinInfo, AbstractClientPlayer> canExecute = (iGenshinInfo, player) -> switch (type) {
            case BURST -> iGenshinInfo.canUseBurst(player);
            case SKILL -> iGenshinInfo.canUseSkill(player);
            default -> false;
        };
        handle(canExecute, () -> new GenshinAbilityMsg(type), (player) -> {
//            GenshinAbilityMsg.onUse(player, type);
        });
    }

    private static void handle(BiPredicate<IGenshinInfo, AbstractClientPlayer> canExecute, Supplier<Object> getMsg, Consumer<Player> runnable) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            localPlayer.getCapability(Capabilities.GENSHIN_INFO).ifPresent(iGenshinInfo -> {
                if (canExecute.test(iGenshinInfo, localPlayer)) {
                    Object msg = getMsg.get();
                    if (msg != null) {
                        GenshinImpactMod.CHANNEL.sendToServer(msg);
                    }

                    if (runnable != null) {
                        runnable.accept(localPlayer);
                    }
                }
            });
        }
    }
}
