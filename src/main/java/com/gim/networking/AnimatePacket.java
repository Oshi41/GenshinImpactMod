package com.gim.networking;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AnimatePacket {
    private int attackIndex;
    private int id;

    public AnimatePacket(LivingEntity entity, int attackIndex) {
        this(entity.getId(), attackIndex);
    }

    private AnimatePacket(int id, int attackIndex) {
        this.id = id;
        this.attackIndex = attackIndex;
    }


    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(id);
        byteBuf.writeInt(attackIndex);
    }

    public static AnimatePacket decode(FriendlyByteBuf byteBuf) {
        return new AnimatePacket(byteBuf.readInt(), byteBuf.readInt());
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        return context.getDirection().getReceptionSide() == LogicalSide.CLIENT
                ? consumeClient()
                : consumeServer(context.getSender());
    }

    @OnlyIn(Dist.CLIENT)
    private boolean consumeClient() {
        Entity entity = net.minecraft.client.Minecraft.getInstance().level.getEntity(id);
        if (entity != null) {
            IGenshinInfo info = entity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (info != null) {
                IGenshinPlayer character = info.current();
                if (character != null) {
                    GenshinEntityData data = info.getPersonInfo(character);
                    if (data != null) {
                        data.getAdditional().putInt(GenshinPlayerBase.ANIMATE_STAGE_ID, attackIndex);

                        // TODO think about attack speed or smth
                    }
                }
            }
        }


        return false;
    }

    private boolean consumeServer(ServerPlayer player) {
        return false;
    }
}
