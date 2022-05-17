package com.gim.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;

public class GenshinServerPlayer extends ServerPlayer {
    private final AttributeMap inner;

    public GenshinServerPlayer(ServerPlayer player, AttributeSupplier.Builder builder) {
        super(player.server, player.getLevel(), player.getGameProfile());
        builder.combine(Player.createAttributes());
        inner = new AttributeMap(builder.build());
    }

    @Override
    public AttributeMap getAttributes() {
        return inner;
    }
}
