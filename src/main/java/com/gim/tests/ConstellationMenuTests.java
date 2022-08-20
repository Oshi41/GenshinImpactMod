package com.gim.tests;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.items.ConstellationItem;
import com.gim.menu.base.GenshinIterableMenuBase;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.registry.Capabilities;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

@GameTestHolder
public class ConstellationMenuTests {
    private final BlockPos pos = new BlockPos(0, 0, 0);

    private ItemStack findForCharacter(GameTestHelper helper, IGenshinPlayer character) {
        List<Item> possibleItems = ForgeRegistries.ITEMS.getValues().stream()
                .filter(x -> x instanceof ConstellationItem && Objects.equals(((ConstellationItem) x).assignedTo.get(), character))
                .toList();

        if (possibleItems.isEmpty()) {
            helper.fail(String.format("Cannot find constellation item for %s", character.getName().getString()));
        }

        return possibleItems.get(0).getDefaultInstance();
    }

    /**
     * Clicking on all characters with all stars
     * Do not have constellation item
     */
    @CustomGameTest(setupTicks = 5)
    public void constellationMenu_noConstellationItem_clickAll_shouldNotChange(GameTestHelper helper) {
        ServerPlayer mockPlayer = TestHelper.createFakePlayer(helper, true);
        helper.setBlock(pos, Blocks.star_worktable);

        GenshinIterableMenuBase menu = TestHelper.rightClick(helper, mockPlayer, pos);
        int end = TestHelper.getCap(helper, mockPlayer, Capabilities.GENSHIN_INFO).getAllPersonages().size();

        for (int i = 0; i < end; i++) {
            for (int j = 0; j < Attributes.constellations.getMaxValue(); j++) {

                if (menu.clickMenuButton(mockPlayer, (j + 1) << 1)) {
                    helper.fail("Menu click must be failed", pos);
                }

                double fromData = menu.current().getAttributes().getValue(Attributes.constellations);

                if (fromData >= 1) {
                    helper.fail(String.format("Something wrong, initial constellations for %s should be 0 but have %s instead",
                            menu.current().getAssotiatedPlayer().getName().getString(),
                            fromData));
                }
            }

            // next
            menu.clickMenuButton(mockPlayer, 1);
        }
    }

    /**
     * Clicking all constellations in both survival/creative game modes
     */
    @CustomGameTest(setupTicks = 5)
    public void constellationMenu_survivalAndCreative_clickAllConstellations_shouldUpgrade(GameTestHelper helper) {
        for (GameType gameType : List.of(GameType.CREATIVE, GameType.SURVIVAL)) {
            // need to create new player every time
            ServerPlayer mockPlayer = TestHelper.createFakePlayer(helper, true);
            mockPlayer.setGameMode(gameType);

            helper.setBlock(pos, Blocks.star_worktable);

            GenshinIterableMenuBase menu = TestHelper.rightClick(helper, mockPlayer, pos);
            int end = TestHelper.getCap(helper, mockPlayer, Capabilities.GENSHIN_INFO).getAllPersonages().size();

            for (int i = 0; i < end; i++) {
                GenshinEntityData entityData = menu.current();

                ItemStack itemStack = findForCharacter(helper, entityData.getAssotiatedPlayer());

                for (int j = 0; j < Attributes.constellations.getMaxValue(); j++) {
                    Slot menuSlot = menu.getSlot(0);
                    menuSlot.safeInsert(itemStack.copy());

                    if (!menuSlot.hasItem()) {
                        helper.fail(String.format("Can't place constellation item for %", entityData.getAssotiatedPlayer().getName().getString()));
                    }

                    if (!menu.clickMenuButton(mockPlayer, (j + 1) << 1)) {
                        helper.fail(String.format("Unknown button or can't upgrade constellation"), pos);
                    }

                    double fromData = entityData.getAttributes().getValue(Attributes.constellations);

                    if (fromData < j + 1) {
                        helper.fail(String.format("Something wrong, should be %s stars for %s instead of %s",
                                j + 1,
                                entityData.getAssotiatedPlayer().getName().getString(),
                                fromData));
                    }

                    if (gameType != GameType.CREATIVE && menuSlot.hasItem()) {
                        helper.fail(String.format("Something wrong, constellation item for %s was not consumed",
                                entityData.getAssotiatedPlayer().getName().getString()));
                    }
                }

                // next
                menu.clickMenuButton(mockPlayer, 1);
            }
        }
    }

    /**
     * Clicking on all unavailable stars
     * Should not change constellation for character
     */
    @CustomGameTest(setupTicks = 5)
    public void constellationMenu_missClickedStars_shouldNotUpgrade(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        serverPlayer.setGameMode(GameType.SURVIVAL);
        helper.setBlock(pos, Blocks.star_worktable);

        GenshinIterableMenuBase menu = TestHelper.rightClick(helper, serverPlayer, pos);

        do {
            ItemStack stack = findForCharacter(helper, menu.current().getAssotiatedPlayer());

            if (!menu.getSlot(0).safeInsert(stack).isEmpty()) {
                helper.fail(String.format("Cannot insert constellation item { %s } for player %s", stack.getItem().getRegistryName(),
                        menu.current().getAssotiatedPlayer().getName().getString()));
            }

            for (int i = 0; i < Attributes.constellations.getMaxValue(); i++) {
                int nextIndex = (i + 2) << 1;
                if (menu.clickMenuButton(serverPlayer, nextIndex)) {
                    helper.fail(String.format("Open star {%s} without open previous {%s}", i + 1, i), pos);
                }

                double stars = menu.current().getAttributes().getValue(Attributes.constellations);

                if (stars > 0) {
                    helper.fail(String.format("Constellation count should not change, %s have %s stars",
                            menu.current().getAssotiatedPlayer().getName().getString(),
                            stars));
                }
            }

            menu.getSlot(0).remove(1);

            if (menu.getSlot(0).hasItem()) {
                helper.fail(String.format("Cannot remove constellation item { %s } for player %s",
                        stack.getItem().getRegistryName(),
                        menu.current().getAssotiatedPlayer().getName().getString()));
            }
        } while (menu.clickMenuButton(serverPlayer, 1));
    }
}
