package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;


@OnlyIn(Dist.CLIENT)
public class KeyMappings {
    private static final String category = GenshinImpactMod.ModID + ".key.category";

    public static final KeyMapping PLAYER_1 = new KeyMapping(GenshinImpactMod.ModID + ".key.player.1", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_1, -1), category);
    public static final KeyMapping PLAYER_2 = new KeyMapping(GenshinImpactMod.ModID + ".key.player.2", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_2, -1), category);
    public static final KeyMapping PLAYER_3 = new KeyMapping(GenshinImpactMod.ModID + ".key.player.3", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_3, -1), category);
    public static final KeyMapping PLAYER_4 = new KeyMapping(GenshinImpactMod.ModID + ".key.player.4", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_4, -1), category);

    public static final KeyMapping SKILL = new KeyMapping(GenshinImpactMod.ModID + ".key.skill", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_E, -1), category);
    public static final KeyMapping BURST = new KeyMapping(GenshinImpactMod.ModID + ".key.burst", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(InputConstants.KEY_Q, -1), category);


    public static void registerKeys() {
        ClientRegistry.registerKeyBinding(PLAYER_1);
        ClientRegistry.registerKeyBinding(PLAYER_2);
        ClientRegistry.registerKeyBinding(PLAYER_3);
        ClientRegistry.registerKeyBinding(PLAYER_4);

        ClientRegistry.registerKeyBinding(SKILL);
        ClientRegistry.registerKeyBinding(BURST);
    }
}
