package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.blocks.GenshinAnvilBlock;
import com.gim.blocks.GenshinCraftingTableBlock;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Blocks;
import com.gim.registry.Capabilities;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.common.collect.Iterators;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@GameTestHolder(GenshinImpactMod.ModID)
public class IterableMenuTest {
    private final List<Block> blocksWithIterableMenu = List.of(Blocks.artifacts_station, Blocks.star_worktable, Blocks.level_station, Blocks.skill_station);

    @CustomGameTest(setupTicks = 5)
    public void testWithBlocks(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createPlayer(helper, true);
        Collection<IGenshinPlayer> playerCollection = TestHelper.getCap(helper, serverPlayer, Capabilities.GENSHIN_INFO).getAllPersonages();

        for (Block block : blocksWithIterableMenu) {
            helper.setBlock(BlockPos.ZERO, block);
            GenshinIterableMenuBase menu = TestHelper.rightClick(helper, serverPlayer, BlockPos.ZERO);

            int i = 0;
            while (i < playerCollection.size()) {
                IGenshinPlayer current = Iterators.get(playerCollection.iterator(), i);
                if (!Objects.equals(current, menu.current().getAssotiatedPlayer())) {
                    helper.fail(String.format("From menu: %s, but should: %s", menu.current().getAssotiatedPlayer(), current));
                }

                // click next
                menu.clickMenuButton(serverPlayer, 1);
                i++;
            }

            i = playerCollection.size() - 1;

            while (i >= 0) {
                IGenshinPlayer current = Iterators.get(playerCollection.iterator(), i);
                if (!Objects.equals(current, menu.current().getAssotiatedPlayer())) {
                    helper.fail(String.format("From menu: %s, but should: %s", menu.current().getAssotiatedPlayer(), current));
                }

                // click next
                menu.clickMenuButton(serverPlayer, 0);
                i--;
            }
        }
    }

    @CustomGameTest(setupTicks = 5)
    public void testWithBlockRemoval(GameTestHelper helper) {
        List<Block> blocksWithMenus = ForgeRegistries.BLOCKS.getValues().stream().filter(x -> x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID))
                .filter(x -> x instanceof GenshinAnvilBlock || x instanceof GenshinCraftingTableBlock)
                .toList();

        for (int i = 0; i < blocksWithMenus.size(); i++) {
            Block block = blocksWithMenus.get(i);
            BlockPos pos = new BlockPos(i, 0, i);
            helper.setBlock(pos, block);

            final ServerPlayer serverPlayer = TestHelper.createPlayer(helper, false);
            final AbstractContainerMenu menu = TestHelper.rightClick(helper, serverPlayer, pos);
            if (menu == null) {
                helper.fail(String.format("%s block is not created any menus for player", block.getRegistryName()), pos);
            }

            helper.destroyBlock(pos);

            helper.runAfterDelay(5, () -> {
                if (menu == serverPlayer.containerMenu) {
                    helper.fail(String.format("Menu for block { %s } was not closed", block.getRegistryName()), pos);
                }
            });
        }
    }
}
