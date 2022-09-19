package com.gim.client.entity.hilichurlian.hilichurl;

import com.gim.GenshinImpactMod;
import com.gim.entity.hilichurlian.Hilichurl;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class HilichurlRenderer extends MobRenderer<Hilichurl, PlayerModel<Hilichurl>> {
    private final ResourceLocation LOC = new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/hilichurl.png");

    public HilichurlRenderer(EntityRendererProvider.Context context) {
        super(context, new HilichurlModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this, getModel(), getModel()));
    }

    @Override
    public ResourceLocation getTextureLocation(Hilichurl p_114482_) {
        return LOC;
    }
}
