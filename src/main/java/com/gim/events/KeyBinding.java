package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.networking.SwitchToPlayerMsg;
import com.gim.registry.Capabilities;
import com.gim.registry.KeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
            handle(IGenshinInfo::canUseSkill, () -> null, null);
        }

        if (KeyMappings.BURST.isDown()) {
            handle(IGenshinInfo::canUseBurst, () -> null, null);
        }
    }

    private static void handleSwitch(int index) {
        handle((genshinInfo, player) -> genshinInfo.canSwitchToPlayer(index, player), () -> new SwitchToPlayerMsg(index), () -> {
            SwitchToPlayerMsg.updatePlayer(Minecraft.getInstance().player, index);
        });
    }

    private static void handle(BiPredicate<IGenshinInfo, AbstractClientPlayer> canExecute, Supplier<Object> getMsg, Runnable runnable) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            localPlayer.getCapability(Capabilities.GENSHIN_INFO).ifPresent(iGenshinInfo -> {
                if (canExecute.test(iGenshinInfo, localPlayer)) {
                    Object msg = getMsg.get();
                    if (msg != null) {
                        GenshinImpactMod.CHANNEL.sendToServer(msg);
                    }

                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }
}
