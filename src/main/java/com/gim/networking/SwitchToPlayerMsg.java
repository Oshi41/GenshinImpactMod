package com.gim.networking;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.google.common.collect.Iterators;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchToPlayerMsg {

    private final byte index;

    public SwitchToPlayerMsg(int index) {
        this.index = (byte) index;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeByte(index);
    }

    public static SwitchToPlayerMsg decode(FriendlyByteBuf buf) {
        return new SwitchToPlayerMsg(buf.readByte());
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        if (context.getDirection().getReceptionSide() == LogicalSide.SERVER) {
            return updatePlayer(context.getSender(), index);
        }

        return true;
    }

    /**
     * Player entity updating by choosing another one from Genshin team stack
     *
     * @param player - current player
     * @param index  - index of current stack player
     * @return
     */
    public static boolean updatePlayer(Player player, int index) {
        if (player != null && index >= 0) {
            IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (genshinInfo != null && genshinInfo.canSwitchToPlayer(index, player)) {
                IGenshinPlayer genshinPlayer = Iterators.get(genshinInfo.currentStack().iterator(), index);
                if (genshinPlayer != null) {
                    GenshinEntityData info = genshinInfo.getPersonInfo(genshinPlayer);
                    info.applyToEntity(player);
                    genshinInfo.onSwitchToIndex(player, index);
                    return true;
                }
            }
        }

        return false;
    }
}