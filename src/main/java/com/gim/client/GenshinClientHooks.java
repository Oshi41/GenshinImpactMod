package com.gim.client;

import com.gim.GenshinHeler;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.entity.players.anemo_traveler.AnemoTravelerRender;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.GenshinCharacters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.Collection;
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

    public static EntityRenderer getRenderer(IGenshinPlayer player) {
        return characterRenders.get(player).get();
    }

    /**
     * Rotate part of entity
     * Takes swing time and uses linear approximation from start to end
     *
     * @param entity - rendered entity
     * @param part   - rendering part
     * @param xStart - X position start
     * @param yStart - Y position start
     * @param zStart - Z position start
     * @param xEnd   - X position end
     * @param yEnd   - Y position end
     * @param zEnd   - Z position end
     */
    public static void rotatePart(LivingEntity entity, ModelPart part,
                                  float xStart, float yStart, float zStart,
                                  float xEnd, float yEnd, float zEnd) {
        float swingTime = entity.swingTime;
        int maxSwingTime = GenshinHeler.getCurrentSwingDuration(entity) - 1;
        float percentage = swingTime / maxSwingTime;
        float x = (xEnd - xStart) * percentage + xStart;
        float y = (yEnd - yStart) * percentage + yStart;
        float z = (zEnd - zStart) * percentage + zStart;

        x = (float) Math.toRadians(x);
        y = (float) Math.toRadians(y);
        z = (float) Math.toRadians(z);

        part.setRotation(x, y, z);
    }


}
