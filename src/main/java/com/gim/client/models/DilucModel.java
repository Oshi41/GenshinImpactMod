package com.gim.client.models;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class DilucModel<T extends LivingEntity> extends PlayerModel<T> {
    public DilucModel(ModelPart modelPart, boolean slim) {
        super(modelPart, slim);
    }

    @Override
    protected void setupAttackAnimation(T entity, float p_102859_) {
        super.setupAttackAnimation(entity, p_102859_);
    }
}
