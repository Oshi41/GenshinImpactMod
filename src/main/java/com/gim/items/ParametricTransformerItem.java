package com.gim.items;

import com.gim.GenshinImpactMod;
import com.gim.menu.ParametricTransformerMenu;
import com.gim.registry.CreativeTabs;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class ParametricTransformerItem extends Item implements Vanishable {
    private static final String LastUseTimeName = "LastUsedTime";
    private static final String CreativeName = "Creative";

    public ParametricTransformerItem() {
        super(new Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1)
                .tab(CreativeTabs.GENSHIN)
                .setNoRepair());
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        if (!allowdedIn(tab))
            return;

        // regular
        ItemStack source = getDefaultInstance();
        stacks.add(source.copy());

        // creative
        source.getOrCreateTag().putBoolean(CreativeName, true);
        stacks.add(source.copy());
    }

    /**
     * Calculating if we can use parametric transformer
     *
     * @param entity - player
     */
    public static boolean canUse(LivingEntity entity) {
        return canUse(entity, false, false);
    }

    /**
     * Calculating if we can use parametric transformer
     *
     * @param entity       - player
     * @param creativeItem - is parametric transformer creative
     * @param silent       - send msg to player?
     */
    private static boolean canUse(LivingEntity entity, boolean creativeItem, boolean silent) {
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
        int current = entity.getLevel().getServer().getTickCount();

        // can use
        if (nextUse <= current)
            return true;
        else if (creativeItem) {
            // changing last use item
            tag.putInt(LastUseTimeName, current - delayTicks);
            // creative always available
            return true;
        }

        if (!silent) {
            // calculating next duration
            String date = DurationFormatUtils.formatDuration((nextUse - current) * 50L, "d:HH:mm:ss");
            TranslatableComponent component = new TranslatableComponent("gim.chat.parametric_transformer.already_used", date);
            entity.sendMessage(component, Util.NIL_UUID);
        }

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag p_41424_) {
        super.appendHoverText(stack, level, components, p_41424_);

        if (!stack.hasTag())
            return;

        if (stack.getTag().getBoolean(CreativeName)) {
            components.add(new TranslatableComponent("gim.chat.creative"));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.getOrCreateTag().getBoolean(CreativeName);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack heldItem = context.getItemInHand();
        Player player = context.getPlayer();
        if (heldItem.getItem() instanceof ParametricTransformerItem && !context.isInside() && player != null) {
            BlockPos position = context.getClickedPos().relative(context.getClickedFace());
            boolean isCreative = heldItem.getOrCreateTag().getBoolean(CreativeName);

            // If we can't use item
            if (!canUse(context.getPlayer(), isCreative, false)) {
                return InteractionResult.CONSUME;
            }

            // if GUI was opened
            if (openGUI(context.getPlayer(), position)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }

    protected boolean openGUI(Player owner, BlockPos pos) {
        return owner.openMenu(new SimpleMenuProvider(
                        (int containerID, Inventory playerInv, Player player) -> new ParametricTransformerMenu(containerID, playerInv, pos),
                        new TranslatableComponent("gim.gui.parametric_transformer")))
                .isPresent();
    }
}
