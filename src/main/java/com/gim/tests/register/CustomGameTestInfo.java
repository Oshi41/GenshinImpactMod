package com.gim.tests.register;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class CustomGameTestInfo extends GameTestInfo {
    private final boolean haveStructure;

    public CustomGameTestInfo(TestFunction function, Rotation p_127614_, ServerLevel serverLevel) {
        super(function, p_127614_, serverLevel);

        String structureName = function.getStructureName();
        haveStructure = structureName != null && !structureName.isBlank();
    }

    @Nullable
    @Override
    public AABB getStructureBounds() {
        return haveStructure ? super.getStructureBounds() : new AABB(BlockPos.ZERO);
    }

    @Override
    public BlockPos getStructureBlockPos() {
        return haveStructure ? super.getStructureBlockPos() : BlockPos.ZERO;
    }

    @Override
    public String getStructureName() {
        return super.getStructureName();
    }

    @Nullable
    @Override
    public Vec3i getStructureSize() {
        return haveStructure ? super.getStructureSize() : Vec3i.ZERO;
    }

    @Override
    public void clearStructure() {
        if (haveStructure)
            super.clearStructure();
    }

    @Override
    public void spawnStructure(BlockPos p_127620_, int p_127621_) {
        if (haveStructure)
            super.spawnStructure(p_127620_, p_127621_);
    }
}
