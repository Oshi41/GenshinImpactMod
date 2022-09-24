package com.gim.blocks;

import com.gim.blocks.state.Fluids;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public interface SimpleLiquidBlockContainer extends BucketPickup, LiquidBlockContainer, IForgeBlock {
    private Block self() {
        return (Block) this;
    }

    /**
     * Property for liquid filling
     */
    EnumProperty<Fluids> LIQUIDLOGGED = EnumProperty.create("liquid_logged", com.gim.blocks.state.Fluids.class);

    /**
     * Loading all possible liquids and extending fluid enum if needed
     */
    static Set<Fluids> loadAll() {
        HashSet<Fluids> result = Sets.newHashSet(Fluids.values());

        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (!fluid.isSource(fluid.defaultFluidState()))
                continue;

            Fluids fluidEnum = Arrays.stream(Fluids.values()).filter(x -> x.getFluid().equals(fluid))
                    .findFirst()
                    .orElseGet(() -> Fluids.createEnum(fluid));

            result.add(fluidEnum);
        }

        return result;
    }


    @Override
    default boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return !state.getValue(LIQUIDLOGGED).same(fluid)
                && Fluids.from(fluid) != null;
    }

    @Override
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!canPlaceLiquid(level, pos, state, fluidState.getType()))
            return false;

        Fluids fluidEnum = Fluids.from(fluidState.getType());
        if (fluidEnum == null)
            return false;

        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(LIQUIDLOGGED, fluidEnum), 3);
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }

        return true;
    }

    @Override
    default ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        Fluids fluidEnum = state.getValue(LIQUIDLOGGED);

        if (fluidEnum.isEmpty())
            return ItemStack.EMPTY;

        ItemStack result = fluidEnum.getFluid().getBucket().getDefaultInstance();
        level.setBlock(pos, state.setValue(LIQUIDLOGGED, Fluids.EMPTY), 3);

        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }

        return result;
    }

    @Override
    default Optional<SoundEvent> getPickupSound(BlockState state) {
        Fluids value = state.getValue(LIQUIDLOGGED);
        return value.getFluid().getPickupSound();
    }

    @Override
    default Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    /**
     * Overriding class method
     */
    FluidState getFluidState(BlockState state);

    /**
     * Overriding class method
     */
    BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor level, BlockPos pos1, BlockPos pos2);

    @Nullable
    BlockState getStateForPlacement(BlockPlaceContext context);
}
