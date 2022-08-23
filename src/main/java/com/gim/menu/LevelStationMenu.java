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
        super(Menus.level_station, containerID, playerInv, access, 3);

        // need to subscribe only on server level
        // only server side logic
        if (!playerInv.player.getLevel().isClientSide()) {
            own.addListener(this);
        }


        for (int i = 0; i < own.getContainerSize(); i++) {
            int x = 8 + i * 18;
            int y = 152;

            // current slot index
            final int slotIndex = i;

            addSlot(new Slot(own, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack toPlace) {
                    if (getForCurrent() == null)
                        return false;

                    // too big
                    if (getForCurrent().getMaterials().size() <= slotIndex) {
                        return false;
                    }

                    // obtaining current info
                    ItemStack itemStack = getForCurrent().getMaterials().get(slotIndex);

                    // empty item, can't place here
                    if (itemStack.isEmpty()) {
                        return false;
                    }

                    // Checking same item
                    if (!ItemStack.isSameItemSameTags(itemStack, toPlace)) {
                        return false;
                    }

                    return super.mayPlace(toPlace);
                }
            });
        }

        // draw player slots
        drawPlayersSlots(8, 175);

        // forcing refresh
        refreshByIndex();
    }

    @Override
    public void refreshByIndex() {
        refreshByIndex(false);
    }

    public void refreshByIndex(boolean saveInventory) {
        GenshinEntityData entityData = current();
        int current = (int) entityData.getAttributes().getValue(Attributes.level);
        forCurrent = entityData.getAssotiatedPlayer().ascendingInfo(current, entityData);

        if (!saveInventory) {
            // removing all items
            this.access.execute((p_39371_, p_39372_) -> this.clearContainer(playerInv.player, own));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int btnIndex) {
        if (super.clickMenuButton(player, btnIndex))
            return true;

        switch (btnIndex) {
            case 3:
                if (containerData.get(1) > 0 && getForCurrent() != null) {

                    // only for non creative player
                    if (!player.isCreative()) {
                        // subtract exp
                        player.giveExperienceLevels(-getForCurrent().getPlayerLevels());

                        // removing all items
                        for (int i = 0; i < getForCurrent().getMaterials().size(); i++) {
                            ItemStack stack = getForCurrent().getMaterials().get(i);
                            getSlot(i).remove(stack.getCount());
                        }
                    }

                    GenshinEntityData genshinEntityData = current();
                    AttributeMap map = genshinEntityData.getAttributes();

                    // scaling level
                    if (LevelScaling.scaleLevel(map::getInstance, (float) (map.getValue(Attributes.level) + 1))) {
                        genshinEntityData.setHealth(player, (float) map.getValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH));
                    }

                    refreshByIndex(true);
                    onChange(0, ItemStack.EMPTY, ItemStack.EMPTY);
                    return true;
                }

                return false;

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

    @OnlyIn(Dist.CLIENT)
    public int ticksToNextLevel() {
        return containerData.get(2);
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return state.getBlock().equals(Blocks.level_station);
    }

    @Override
    public void onChange(int slotId, ItemStack prev, ItemStack current) {
        // checking current
        if (slotId < own.getContainerSize()) {
            boolean checked = getForCurrent() != null && getForCurrent().getMaterials().size() > 0;

            if (checked) {
                for (int i = 0; i < getForCurrent().getMaterials().size(); i++) {
                    ItemStack original = getForCurrent().getMaterials().get(i);
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
                    if (checked && getForCurrent().getPlayerLevels() > playerInv.player.experienceLevel) {
                        checked = false;
                    }

                    if (checked && playerInv.player instanceof ServerPlayer serverPlayer) {
                        Stat<ResourceLocation> playTimeStat = Stats.CUSTOM.get(Stats.PLAY_TIME);

                        if (getForCurrent().getTicksTillLevel() > serverPlayer.getStats().getValue(playTimeStat)) {
                            checked = false;
                        }
                    }
                }
            }

            setData(1, checked ? 1 : 0);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!playerInv.player.getLevel().isClientSide() && getForCurrent() != null) {
            ServerPlayer serverPlayer = (ServerPlayer) playerInv.player;
            Stat<ResourceLocation> playTimeStat = Stats.CUSTOM.get(Stats.PLAY_TIME);
            int livingTicks = serverPlayer.getStats().getValue(playTimeStat);

            long toWait = getForCurrent().getTicksTillLevel() - livingTicks;
            setData(2, (int) toWait);

            if (serverPlayer.getAttributeValue(Attributes.level) >= Attributes.level.getMaxValue()) {
                setData(2, -1);
            }
        }
    }

    public AscendInfo getForCurrent() {
        if (playerInv != null && playerInv.player.getLevel().isClientSide()) {
            refreshByIndex(true);
        }

        return forCurrent;
    }
}
