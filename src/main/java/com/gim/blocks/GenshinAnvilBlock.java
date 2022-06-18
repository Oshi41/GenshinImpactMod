package com.gim.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;

public class GenshinAnvilBlock extends GenshinCraftingTableBlock {
    public GenshinAnvilBlock(Properties p_49795_, TriFunction<Integer, Inventory, ContainerLevelAccess, AbstractContainerMenu> createMenu) {
        super(p_49795_, createMenu);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    public VoxelShape getShape(BlockState p_48816_, BlockGetter p_48817_, BlockPos p_48818_, CollisionContext p_48819_) {
        return Blocks.ANVIL.getShape(p_48816_, p_48817_, p_48818_, p_48819_);
    }

    @Override
    public BlockState rotate(BlockState p_48811_, Rotation p_48812_) {
        return p_48811_.setValue(HorizontalDirectionalBlock.FACING, p_48812_.rotate(p_48811_.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48814_) {
        p_48814_.add(HorizontalDirectionalBlock.FACING);
    }
}
