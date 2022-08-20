package com.gim.tests;

import com.gim.menu.SkillStationMenu;
import com.gim.players.base.TalentAscendInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GameTestHolder
public class SkillStationMenuTests {

    @CustomGameTest(setupTicks = 5)
    public void skillStation_testForSurvival(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        serverPlayer.setGameMode(GameType.SURVIVAL);
        helper.setBlock(BlockPos.ZERO, Blocks.skill_station);
        SkillStationMenu menu = TestHelper.rightClick(helper, serverPlayer, BlockPos.ZERO);

        do {
            List<TalentAscendInfo> testData = getTestInfo(menu.info(false));
            TalentAscendInfo correct = testData.get(testData.size() - 1);

            for (TalentAscendInfo info : testData) {
                // clear all
                menu.getSlot(0).container.clearContent();

                // need to set it at first
                serverPlayer.experienceLevel = info.expLevel();
                menu.current().getAttributes().getInstance(Attributes.level).setBaseValue(info.minCharacterLevel());

                // inserting materials
                for (int j = 0; j < info.materials().size(); j++) {
                    menu.getSlot(j).safeInsert(info.materials().get(j));
                }

                boolean isCorrect = info == correct;
                boolean clickResult = menu.clickMenuButton(serverPlayer, 2);

                if (clickResult != isCorrect) {
                    helper.fail(String.format("Menu click result is %s, while should be %s for player %s", clickResult, isCorrect, menu.current().getAssotiatedPlayer().getName().getString()));
                }
            }
        } while (menu.clickMenuButton(serverPlayer, 1));
    }

    /**
     * Test talent ascending data
     * Last one is correct
     * Test data contains with one or more wrong fields
     */
    private List<TalentAscendInfo> getTestInfo(TalentAscendInfo talentAscendInfo) {
        ArrayList<TalentAscendInfo> result = new ArrayList<>();

        double end = 1 << 3;

        for (int i = 1; i < end; i++) {
            List<ItemStack> itemsCopy = talentAscendInfo.materials().stream().map(ItemStack::copy).collect(Collectors.toList());
            int expLevel = talentAscendInfo.expLevel();
            int minCharacterLevel = talentAscendInfo.minCharacterLevel();

            if (TestHelper.isBitPresented(i, 0)) {
                ItemStack itemStack = itemsCopy.stream().filter(x -> x.getCount() > 1).findFirst().orElse(null);
                if (itemStack != null) {
                    itemStack.shrink(1);
                } else {
                    if (itemsCopy.size() >= 4) {
                        itemsCopy.remove(0);
                    } else {
                        itemsCopy.add(Items.REDSTONE.getDefaultInstance());
                    }
                }
            }

            if (TestHelper.isBitPresented(i, 1)) {
                expLevel -= 1;
            }

            if (TestHelper.isBitPresented(i, 2)) {
                minCharacterLevel -= 1;
            }

            result.add(new TalentAscendInfo(NonNullList.of(ItemStack.EMPTY, itemsCopy.toArray(ItemStack[]::new)), talentAscendInfo.info(), expLevel, minCharacterLevel, talentAscendInfo.skillsInfo()));
        }

        result.add(new TalentAscendInfo(NonNullList.of(ItemStack.EMPTY, talentAscendInfo.materials().stream().map(ItemStack::copy).toArray(ItemStack[]::new)),
                talentAscendInfo.info(),
                talentAscendInfo.expLevel(),
                talentAscendInfo.minCharacterLevel(),
                talentAscendInfo.skillsInfo()));
        return result;
    }
}
