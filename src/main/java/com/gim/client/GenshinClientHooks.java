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

    /**
     * Helping method for creating context
     *
     * @return - context for model creators
     */
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
}
