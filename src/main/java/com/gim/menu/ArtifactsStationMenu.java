package com.gim.menu;

import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.items.ArtefactItem;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.menu.base.WrapperContainer;
import com.gim.registry.Blocks;
import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ArtifactsStationMenu extends GenshinIterableMenuBase {
    private final WrapperContainer wrapper = new WrapperContainer();

    public ArtifactsStationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.artifacts_station, containerID, playerInv, access);

        for (int i = 0; i < ArtifactSlotType.values().length; i++) {
            ArtifactSlotType slotType = ArtifactSlotType.values()[i];
            addSlot(new Slot(wrapper, i, 29 + i * 24, 111) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return super.mayPlace(stack)
                            && stack.getItem() instanceof ArtefactItem
                            && Objects.equals(((ArtefactItem) stack.getItem()).getType(), slotType);
                }
            });
        }

        drawPlayersSlots(8, 134);

        refreshByIndex();
    }

    public ArtifactsStationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    @Override
    protected boolean checkBlock(BlockState block) {
        return block.getBlock().equals(Blocks.artifacts_station);
    }

    @Override
    public void refreshByIndex() {
        Container container = null;
        GenshinEntityData genshinEntityData = current();

        if (genshinEntityData != null) {
            container = genshinEntityData.getArtifactsContainer();
        }

        wrapper.with(container);
    }
}
