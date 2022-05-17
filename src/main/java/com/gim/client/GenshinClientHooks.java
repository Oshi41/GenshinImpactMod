package com.gim.client;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.other.CustomLazy;
import com.gim.registry.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class GenshinClientHooks {

    public static final CustomLazy<Collection<EntityRenderer<? extends Player>>> getPlayerRenders = new CustomLazy<>(() -> Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values());

    /**
     * Redirecting from AbstractClientPlayer.getSkinTextureLocation.
     * Coremod transformation
     *
     * @return - skin location
     */
    public static ResourceLocation getSkinTextureLocation(AbstractClientPlayer player) {
        IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        // can show character skin
        if (genshinInfo != null && genshinInfo.current() != null && genshinInfo.current().getSkin() != null) {
            return genshinInfo.current().getSkin();
        }

        // immitating vanilla code
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PlayerInfo info = connection.getPlayerInfo(player.getUUID());
            if (info != null) {
                return info.getSkinLocation();
            }
        }

        return DefaultPlayerSkin.getDefaultSkin(player.getUUID());
    }


    /**
     * Redirecting from LivingEntityRenderer.getModel
     * Coremod transformation
     *
     * @param model  - entity model
     * @param source - renderer
     * @return
     */
    public static EntityModel getModel(EntityModel model, LivingEntityRenderer source) {

        // it's a player actually
        if (getPlayerRenders.get().contains(source)) {
            LocalPlayer player = Minecraft.getInstance().player;

            IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            // Can show model
            if (genshinInfo != null && genshinInfo.current() != null && genshinInfo.current().getModel() != null) {
                return (EntityModel) genshinInfo.current().getModel();
            }
        }

        return model;
    }
}
