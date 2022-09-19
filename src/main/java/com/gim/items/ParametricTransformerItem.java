package com.gim.items;

import com.gim.GenshinImpactMod;
import com.gim.menu.ParametricTransformerMenu;
import com.gim.registry.CreativeTabs;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

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
     */
    public static boolean canUse(LivingEntity entity) {
        // obviousely it works only on server
        if (entity == null || entity.getLevel().getServer() == null)
            return false;

        // getting persistant entity data
        CompoundTag tag = entity.getPersistentData();
        // first use
        if (!tag.contains(LastUseTimeName))
            return true;

        int lastUse = tag.getInt(LastUseTimeName);
        // config based value
        int delayTicks = GenshinImpactMod.CONFIG.getKey().parametricTranformerDelayMin.get() * 60 * 20;
        // next use
        int nextUse = lastUse + delayTicks;
        // next possible use
        int current = delayTicks + entity.getLevel().getServer().getTickCount();

        // can use
        if (nextUse <= current)
            return true;

        // calculating next duration
        String date = DurationFormatUtils.formatDuration((nextUse - current) * 50L, "d:HH:ss");
        TranslatableComponent component = new TranslatableComponent("gim.chat.parametric_transformer.already_used", date);
        entity.sendMessage(component, Util.NIL_UUID);
        return false;
    }

    /**
     * Saving current time as last parametric usage
     *
     * @param entity - server side entity
     * @return - success of operation
     */
    public static boolean saveUsage(LivingEntity entity) {
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

            if (!canUse(player) || openGUI(player, context.getClickedPos().relative(context.getClickedFace()))) {
                return InteractionResult.CONSUME;
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
