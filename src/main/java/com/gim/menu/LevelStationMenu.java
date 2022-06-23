package com.gim.menu;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.events.LevelScaling;
import com.gim.menu.base.GenshinContainer;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.menu.base.SlotListener;
import com.gim.players.base.AscendInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.registry.Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelStationMenu extends GenshinIterableMenuBase implements SlotListener {
    private AscendInfo forCurrent;
    private final GenshinContainer own = new GenshinContainer(4);

    public LevelStationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);
    }

    public LevelStationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.level_station, containerID, playerInv, access, 2);
        own.addListener(this);

        for (int i = 0; i < own.getContainerSize(); i++) {
            int x = 8 + i * 18;
            int y = 111;

            // current slot index
            final int slotIndex = i;

            addSlot(new Slot(own, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack toPlace) {
                    if (getForCurrent() != null) {

                        // too big
                        if (getForCurrent().materials.size() <= slotIndex) {
                            return false;
                        }

                        // obtaining current info
                        ItemStack itemStack = getForCurrent().materials.get(slotIndex);

                        // empty item, can't place here
                        if (itemStack.isEmpty()) {
                            return false;
                        }

                        // Checking same item
                        if (!ItemStack.isSameItemSameTags(itemStack, toPlace)) {
                            return false;
                        }
                    }

                    return super.mayPlace(toPlace);
                }
            });
        }

        // draw player slots
        drawPlayersSlots(8, 134);

        // forcing refresh
        refreshByIndex();
    }

    @Override
    public void refreshByIndex() {
        GenshinEntityData entityData = current();
        int current = (int) entityData.getAttributes().getValue(Attributes.level);
        forCurrent = entityData.getAssotiatedPlayer().fromLevel(current);

        // removing all items
        this.access.execute((p_39371_, p_39372_) -> this.clearContainer(playerInv.player, own));
    }

    @Override
    public boolean clickMenuButton(Player player, int btnIndex) {
        if (super.clickMenuButton(player, btnIndex))
            return true;

        switch (btnIndex) {
            case 3:
                if (containerData.get(1) > 0 && getForCurrent() != null) {

                    if (!player.isCreative()) {
                        player.giveExperienceLevels(-getForCurrent().playerLevels);
                    }

                    for (int i = 0; i < getForCurrent().materials.size(); i++) {
                        ItemStack stack = getForCurrent().materials.get(i);
                        getSlot(i).remove(stack.getCount());
                    }

                    GenshinEntityData genshinEntityData = current();
                    AttributeMap map = genshinEntityData.getAttributes();

                    LevelScaling.scaleLevel(map::getInstance, (float) (map.getValue(Attributes.level) + 1));
                    refreshByIndex();
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public void removed(Player p_38940_) {
        super.removed(p_38940_);

        // removing all items
        this.access.execute((p_39371_, p_39372_) -> this.clearContainer(playerInv.player, own));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canApply() {
        return containerData.get(1) > 0;
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return state.getBlock().equals(Blocks.level_station);
    }

    @Override
    public void onChange(int slotId, ItemStack prev, ItemStack current) {
        // checking current
        if (slotId < own.getContainerSize()) {
            boolean checked = getForCurrent() != null && getForCurrent().materials.size() > 0;

            if (checked) {
                for (int i = 0; i < getForCurrent().materials.size(); i++) {
                    ItemStack original = getForCurrent().materials.get(i);
                    ItemStack ownStack = own.getItem(i);

                    if (!original.isEmpty()) {
                        if (!ItemStack.isSameItemSameTags(original, ownStack) || ownStack.getCount() < original.getCount()) {
                            checked = false;
                            break;
                        }
                    }
                }

                if (!playerInv.player.isCreative()) {

                    // checking player level
                    if (checked && getForCurrent().playerLevels > playerInv.player.experienceLevel) {
                        checked = false;
                    }

                    if (checked && playerInv.player instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer) playerInv.player;
                        Stat<ResourceLocation> playTimeStat = Stats.CUSTOM.get(Stats.PLAY_TIME);

                        if (getForCurrent().ticksTillLevel > serverPlayer.getStats().getValue(playTimeStat)) {
                            checked = false;
                        }
                    }
                }
            }

            setData(1, checked ? 1 : 0);
        }
    }

    public AscendInfo getForCurrent() {
        return forCurrent;
    }
}
