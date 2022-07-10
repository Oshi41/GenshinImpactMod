package com.gim.menu;

import com.gim.GenshinHeler;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.menu.base.GenshinContainer;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.menu.base.SlotListener;
import com.gim.players.base.IGenshinPlayer;
import com.gim.players.base.TalentAscendInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.registry.Menus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import java.util.UUID;
import java.util.function.Supplier;

public class SkillStationMenu extends GenshinIterableMenuBase implements SlotListener {
    private static final UUID ID = UUID.fromString("7391322a-ee84-4287-a03b-8d92150eaea4");
    private TalentAscendInfo talentInfo;
    private final GenshinContainer own = new GenshinContainer(4);

    public SkillStationMenu(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerID, playerInv, ContainerLevelAccess.NULL);

    }

    public SkillStationMenu(int containerID, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.skill_station, containerID, playerInv, access, 2);

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
                    // no talent info
                    if (info() == null) {
                        return false;
                    }

                    NonNullList<ItemStack> itemStacks = info().materials();
                    // this slot should be empty
                    if (slotIndex >= itemStacks.size()) {
                        return false;
                    }

                    ItemStack stack = itemStacks.get(slotIndex);
                    // empty or different item
                    if (stack.isEmpty() || !ItemStack.isSameItemSameTags(stack, toPlace)) {
                        return false;
                    }

                    return super.mayPlace(toPlace);
                }
            });
        }

        // draw player slots
        drawPlayersSlots(8, 175);

        refreshByIndex();
    }

    @Override
    public void refreshByIndex() {
        this.talentInfo = null;

        GenshinEntityData entityData = current();
        if (entityData != null) {
            int skill = (int) GenshinHeler.safeGetAttribute(entityData.getAttributes(), Attributes.skill_level);
            if (skill >= 0) {
                IGenshinPlayer genshinPlayer = entityData.getAssotiatedPlayer();
                this.talentInfo = genshinPlayer.talentInfo(skill + 1, entityData);
            }
        }
    }

    @Override
    protected boolean checkBlock(BlockState state) {
        return state.getBlock().equals(Blocks.skill_station);
    }

    @Override
    public void onChange(int slotId, ItemStack prev, ItemStack current) {
        if (slotId < own.getContainerSize()) {
            boolean canApply = true;
            TalentAscendInfo info = info();

            if (info != null) {
                NonNullList<ItemStack> materials = info.materials();

                for (int i = 0; i < materials.size(); i++) {
                    ItemStack fromContainer = own.getItem(i);
                    ItemStack material = materials.get(i);

                    // check item and count
                    if (!ItemStack.isSameItemSameTags(fromContainer, material) || fromContainer.getCount() < material.getCount()) {
                        canApply = false;
                        break;
                    }
                }

                // checking if not in the creative
                if (canApply && !playerInv.player.isCreative()) {

                    // exp levels is not enough
                    if (playerInv.player.experienceLevel < info.expLevel()) {
                        canApply = false;
                    }

                    // character level is not enough
                    if (canApply && info.minCharacterLevel() > GenshinHeler.safeGetAttribute(playerInv.player, Attributes.level)) {
                        canApply = false;
                    }
                }
            }

            setData(1, canApply ? 1 : 0);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int btnIndex) {
        if (super.clickMenuButton(player, btnIndex)) {
            return true;
        }

        switch (btnIndex) {
            // Apply
            case 2:
                // if can apply
                if (containerData.get(1) > 0) {
                    TalentAscendInfo talentAscendInfo = info();
                    if (talentAscendInfo != null) {
                        GenshinEntityData genshinEntityData = current();
                        if (genshinEntityData != null) {
                            AttributeInstance attributeInstance = genshinEntityData.getAttributes().getInstance(Attributes.skill_level);
                            if (attributeInstance != null) {

                                // only for non creative
                                if (!player.isCreative()) {
                                    NonNullList<ItemStack> materials = talentAscendInfo.materials();
                                    // shrink materials
                                    for (int i = 0; i < materials.size(); i++) {
                                        ItemStack material = materials.get(i);
                                        getSlot(i).remove(material.getCount());
                                    }

                                    // subtract levels
                                    player.giveExperienceLevels(-talentAscendInfo.expLevel());
                                }

                                // finding modifier by ID
                                AttributeModifier addingModifier = attributeInstance.getModifier(ID);
                                // get it's value
                                double amount = addingModifier == null
                                        ? 0
                                        : addingModifier.getAmount();

                                // creating new modifier with increased value
                                addingModifier = new AttributeModifier(ID, "skill.ascending", amount + 1, AttributeModifier.Operation.ADDITION);
                                // removing old modifier
                                attributeInstance.removeModifier(addingModifier);
                                // adding new modifier
                                attributeInstance.addPermanentModifier(addingModifier);
                            }
                        }
                    }
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * Can apply talent scaling
     */
    @OnlyIn(Dist.CLIENT)
    public boolean canApply() {
        return containerData.get(1) > 0;
    }

    @Nullable
    public TalentAscendInfo info() {
        return talentInfo;
    }
}
