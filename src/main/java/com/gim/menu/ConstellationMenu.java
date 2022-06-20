package com.gim.menu;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.items.ConstellationItem;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.registry.Capabilities;
import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ConstellationMenu extends GenshinIterableMenuBase {
    private final Container own = new SimpleContainer(1);

    public ConstellationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    public ConstellationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.constellation, containerID, playerInv, access);

        this.addSlot(new Slot(own, 0, 8, 139) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                // item should be ConstellationItem
                if (!(itemStack.getItem() instanceof ConstellationItem)) {
                    return false;
                }

                ConstellationItem constellationItem = (ConstellationItem) itemStack.getItem();

                GenshinEntityData genshinEntityData = current();
                // item star should be for character
                if (genshinEntityData != null && !Objects.equals(constellationItem.assignedTo.get(), genshinEntityData.getAssotiatedPlayer())) {
                    return false;
                }

                return true;
            }
        });

        drawPlayersSlots(8, 175);
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return state.getBlock().equals(Blocks.star_worktable);
    }

    @Override
    public boolean clickMenuButton(Player player, int starIndex) {
        GenshinEntityData entityData = current();
        if (entityData != null) {
            IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (genshinInfo != null) {
                AttributeInstance attributeInstance = entityData.getAttributes().getInstance(Attributes.constellations);
                // clicked on correct star
                if (attributeInstance != null && attributeInstance.getBaseValue() == starIndex) {
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 1);
                    entityData.getAssotiatedPlayer().onStarAdded(player, genshinInfo, ((int) attributeInstance.getValue()));
                    own.removeItem(0, 1);

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void refreshByIndex() {
        // ignored
    }
}
