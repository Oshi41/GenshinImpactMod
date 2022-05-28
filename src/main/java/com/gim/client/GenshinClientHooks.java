package com.gim.client;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.players.anemo_traveler.AnemoTravelerRender;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.GenshinCharacters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GenshinClientHooks {
    private static EntityRendererProvider.Context createContext() {
        return new EntityRendererProvider.Context(
                Minecraft.getInstance().getEntityRenderDispatcher(),
                Minecraft.getInstance().getItemRenderer(),
                Minecraft.getInstance().getResourceManager(),
                Minecraft.getInstance().getEntityModels(),
                Minecraft.getInstance().font
        );
    }

    /**
     * Map for render instances
     */
    private static final Map<IGenshinPlayer, Lazy<EntityRenderer>> characterRenders = new HashMap<>() {{
        put(GenshinCharacters.ANEMO_TRAVELER, Lazy.of(() -> new AnemoTravelerRender(createContext())));
    }};

    /**
     * Redirecting from EntityRenderDispatcher.getRenderer
     * Coremod transformation
     *
     * @param entity   - current entity
     * @param original - source render
     * @return - actual render for entity
     */
    public static EntityRenderer getRenderer(EntityRenderer original, Entity entity) {
        IGenshinInfo info = entity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (info != null && info.current() != null) {
            Lazy<EntityRenderer> lazy = characterRenders.get(info.current());
            if (lazy != null && lazy.get() != null) {
                return lazy.get();
            }
        }

        return original;
    }

//    public static final CustomLazy<Collection<EntityRenderer<? extends Player>>> getPlayerRenders = new CustomLazy<>(() -> Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values());
//
//    /**
//     * Redirecting from AbstractClientPlayer.getSkinTextureLocation.
//     * Coremod transformation
//     *
//     * @return - skin location
//     */
//    public static ResourceLocation getSkinTextureLocation(AbstractClientPlayer player) {
//        IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
//        // can show character skin
//        if (genshinInfo != null && genshinInfo.current() != null && genshinInfo.current().getSkin() != null) {
//            return genshinInfo.current().getSkin();
//        }
//
//        // immitating vanilla code
//        ClientPacketListener connection = Minecraft.getInstance().getConnection();
//        if (connection != null) {
//            PlayerInfo info = connection.getPlayerInfo(player.getUUID());
//            if (info != null) {
//                return info.getSkinLocation();
//            }
//        }
//
//        return DefaultPlayerSkin.getDefaultSkin(player.getUUID());
//    }
//
//
//    /**
//     * Redirecting from LivingEntityRenderer.getModel
//     * Coremod transformation
//     *
//     * @param model  - entity model
//     * @param source - renderer
//     * @return
//     */
//    public static EntityModel getModel(EntityModel model, LivingEntityRenderer source) {
//
//        // it's a player actually
//        if (getPlayerRenders.get().contains(source)) {
//            LocalPlayer player = Minecraft.getInstance().player;
//
//            IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
//            // Can show model
//            if (genshinInfo != null && genshinInfo.current() != null && genshinInfo.current().getModel() != null) {
//                return (EntityModel) genshinInfo.current().getModel();
//            }
//        }
//
//        return model;
//    }
//
//    /**
//     * Converts atlas texture location to full path to .png file
//     *
//     * @param location - atlas texture location
//     * @return
//     */
//    public static ResourceLocation getResourceLocation(ResourceLocation location) {
//        return new ResourceLocation(location.getNamespace(), String.format("textures/%s%s", location.getPath(), ".png"));
//    }
}
