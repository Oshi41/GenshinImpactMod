package com.gim.blocks;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

public class GenshinCraftingTableBlock extends Block {

    private TriFunction<Integer, Inventory, ContainerLevelAccess, AbstractContainerMenu> createMenu;

    public GenshinCraftingTableBlock(Properties p_49795_, TriFunction<Integer, Inventory, ContainerLevelAccess, AbstractContainerMenu> createMenu) {
        super(p_49795_);
        this.createMenu = createMenu;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        // should have any characters
        if (genshinInfo == null || genshinInfo.getAllPersonages().isEmpty())
            return InteractionResult.FAIL;

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }


        TranslatableComponent name = new TranslatableComponent(getRegistryName().getNamespace() + ".crafting." + getRegistryName().getPath());
        player.openMenu(new SimpleMenuProvider((p_39954_, p_39955_, p_39956_) -> this.createMenu(p_39954_, p_39955_, p_39956_, blockState, level, hitResult, blockPos), name));
        return InteractionResult.CONSUME;
    }

    @Nullable
    protected AbstractContainerMenu createMenu(int containerID, Inventory playerInv, Player player, BlockState blockState, Level level, BlockHitResult hitResult, BlockPos blockPos) {
        return createMenu.apply(containerID, playerInv, ContainerLevelAccess.create(player.getLevel(), blockPos));
    }
}
