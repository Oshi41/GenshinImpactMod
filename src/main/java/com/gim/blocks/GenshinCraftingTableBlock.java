package com.gim.blocks;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.menu.ConstellationMenu;
import com.gim.registry.Capabilities;
import com.gim.registry.Menus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GenshinCraftingTableBlock extends Block {
    private final Lazy<MenuType<?>> menuType;

    public GenshinCraftingTableBlock(Properties p_49795_, Lazy<MenuType<?>> menuType) {
        super(p_49795_);
        this.menuType = menuType;
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
        if (Objects.equals(menuType.get(), Menus.constellation)) {
            return new ConstellationMenu(containerID, playerInv, player, blockState, level, hitResult, blockPos);
        }

        return null;
    }
}
