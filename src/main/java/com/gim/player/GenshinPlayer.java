package com.gim.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class GenshinPlayer extends ServerPlayer {

    public GenshinPlayer(MinecraftServer minecraftServer, ServerLevel level, GameProfile profile) {
        super(minecraftServer, level, profile);
    }

    public GenshinPlayer(ServerPlayer player) {
        this(player.server, player.getLevel(), player.getGameProfile());
    }
}
