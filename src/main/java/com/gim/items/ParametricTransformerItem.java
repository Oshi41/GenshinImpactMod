package com.gim.items;

import com.gim.GenshinImpactMod;
import com.gim.menu.ParametricTransformerMenu;
import com.gim.registry.CreativeTabs;
import com.gim.registry.Menus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class ParametricTransformerItem extends Item implements Vanishable {
    private static final String LastUseTimeName = "LastUsedTime";

    public ParametricTransformerItem() {
        super(new Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1)
                .tab(CreativeTabs.MATERIALS)
                .setNoRepair());
    }

    /**
     * Ticks till next usage
     *
     * @param entity - holder
     * @param stack  - transformer item
     */
    public int tickToNext(LivingEntity entity, ItemStack stack) {
        // obviousely it works only on server
        if (entity != null && stack != null && stack.getItem() instanceof ParametricTransformerItem && entity.getLevel().getServer() != null) {
            // getting persistant entity data
            CompoundTag tag = entity.getPersistentData();

            // first ever
            if (!tag.contains(LastUseTimeName)){
                return 0;
            }

            // checking last use data
            int lastTickCount = tag.getInt(LastUseTimeName);
            // config based value
            int delayTicks = GenshinImpactMod.CONFIG.getKey().parametricTranformerDelayMin.get() * 60 * 20;
            // calculating next possible time
            int nextPossibleTime = lastTickCount + delayTicks;
            // retrieving current time
            int currentTime = entity.getLevel().getServer().getTickCount();
            // returning ticks to wait or 0 if we can use
            return Math.max(0, nextPossibleTime - currentTime);
        }

        // error value less 0
        return -1;
    }

    /**
     * Saving current time as last parametric usage
     *
     * @param entity - server side entity
     * @return - success of operation
     */
    public boolean saveUsage(LivingEntity entity) {
        if (entity != null && entity.getLevel().getServer() != null) {
            CompoundTag tag = entity.getPersistentData();
            tag.putInt(LastUseTimeName, entity.getLevel().getServer().getTickCount());
            return true;
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack heldItem = context.getItemInHand();
        Player player = context.getPlayer();
        if (heldItem.getItem() instanceof ParametricTransformerItem && !context.isInside() && player != null) {
            // can place entity and successfully spawned parametric transformer
            if (tickToNext(player, heldItem) == 0 && openGUI(player, context.getClickedPos())) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }

    protected boolean openGUI(Player owner, BlockPos pos) {
        if (owner != null && pos != null) {
            owner.openMenu(new SimpleMenuProvider(
                    (int containerID, Inventory playerInv, Player player) -> new ParametricTransformerMenu(containerID, playerInv, pos),
                    new TranslatableComponent("gim.gui.parametric_transformer")));
            return true;
        }

        return false;
    }
}
