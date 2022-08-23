package com.gim.menu.base;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.google.common.collect.Iterators;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GenshinIterableMenuBase extends GenshinMenuBase {
    protected final ContainerData containerData;


    protected GenshinIterableMenuBase(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access) {
        this(menuType, containerID, playerInv, access, 1);
    }

    protected GenshinIterableMenuBase(@Nullable MenuType<?> menuType, int containerID, Inventory playerInv, ContainerLevelAccess access, int dataSize) {
        super(menuType, containerID, playerInv, access);

        containerData = new SimpleContainerData(dataSize);
        addDataSlots(containerData);
    }

    @Override
    public boolean clickMenuButton(Player player, int btnIndex) {
        switch (btnIndex) {
            // navigate backwards
            case 0:
                if (getIndex() > 0) {
                    setData(0, getIndex() - 1);
                    refreshByIndex();
                    return true;
                }

                return false;

            // navigate forwards
            case 1:
                IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
                if (genshinInfo != null && genshinInfo.getAllPersonages().size() > getIndex() + 1) {
                    setData(0, getIndex() + 1);
                    refreshByIndex();
                    return true;
                }

                return false;

            default:
                return false;
        }
    }

    /**
     * Returns current character
     *
     * @return
     */
    @Nullable
    public GenshinEntityData current() {
        int index = this.containerData.get(0);

        if (playerInv != null && playerInv.player != null) {
            IGenshinInfo genshinInfo = playerInv.player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (genshinInfo != null) {
                Collection<IGenshinPlayer> playerCollection = genshinInfo.getAllPersonages();
                index = Mth.clamp(index, 0, playerCollection.size() - 1);
                IGenshinPlayer player = Iterators.get(playerCollection.iterator(), index);
                return genshinInfo.getPersonInfo(player);
            }
        }

        return null;
    }

    /**
     * Returns current index of player
     */
    public int getIndex() {
        return containerData.get(0);
    }

    public abstract void refreshByIndex();

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int slotId, int value) {

        if (slotId == 0) {
            refreshByIndex();
        }
    }

}
