package com.gim.player;

import net.minecraft.server.level.ServerPlayer;

public class GenshinPlayer extends ServerPlayer {
    public GenshinPlayer(ServerPlayer player) {
        super(player.server, player.getLevel(), player.getGameProfile());
    }
}
