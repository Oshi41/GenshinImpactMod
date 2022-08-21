package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.menu.LevelStationMenu;
import com.gim.players.base.AscendInfo;
import com.gim.registry.Attributes;
import com.gim.registry.Blocks;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GameTestHolder(GenshinImpactMod.ModID)
public class LevelStationTests {

    @CustomGameTest(setupTicks = 1)
    public void levelStation_increaseLevelSurvival(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        serverPlayer.setGameMode(GameType.SURVIVAL);
        helper.setBlock(BlockPos.ZERO, Blocks.level_station);
        LevelStationMenu menu = TestHelper.rightClick(helper, serverPlayer, BlockPos.ZERO);

        do {
            GenshinEntityData entityData = menu.current();
            while (entityData.getAttributes().getValue(Attributes.level) < Attributes.level.getMaxValue()) {
                AscendInfo correctInfo = menu.getForCurrent();
                // creating incorrect vartiants we use to check all conditions
                List<AscendInfo> infos = getTestData(correctInfo);

                // iterating on all infos
                for (AscendInfo ascendInfo : infos) {
                    // clear all slots
                    menu.getSlot(0).container.clearContent();

                    // time condition
                    TestHelper.setGameTime(serverPlayer, (int) ascendInfo.getTicksTillLevel());
                    // exp condition
                    serverPlayer.experienceLevel = ascendInfo.getPlayerLevels();
                    // material conditions
                    for (int i = 0; i < ascendInfo.getMaterials().size(); i++) {
                        menu.getSlot(i).safeInsert(ascendInfo.getMaterials().get(i));
                    }

                    boolean shouldWork = ascendInfo == correctInfo;
                    boolean wasClicked = menu.clickMenuButton(serverPlayer, 3);

                    if ((wasClicked && !shouldWork) || (!wasClicked && shouldWork)) {

                        String text = shouldWork
                                ? "Didn't work but should"
                                : "Worked but shouldn't";
                        text += "\n\nCorrect:\n" + correctInfo.toString();

                        if (ascendInfo != correctInfo) {
                            text += "\n\nCurrent:\n" + ascendInfo.toString();
                        }

                        helper.fail(text);
                    }
                }
            }

        } while (menu.clickMenuButton(serverPlayer, 1));
    }

    private List<AscendInfo> getTestData(AscendInfo info) {
        List<AscendInfo> result = new ArrayList<>();

        double end = 1 << 3;

        for (int i = 1; i < end; i++) {
            boolean changeMaterials = false;
            int playerLevelsDelta = 0;
            int ticksTillLevelDelta = 0;

            if (TestHelper.isBitPresented(i, 0)) {
                changeMaterials = true;
            }

            if (TestHelper.isBitPresented(i, 1)) {
                playerLevelsDelta = -2;
            }

            if (TestHelper.isBitPresented(i, 2)) {
                ticksTillLevelDelta = -2;
            }

            result.add(new CustomAscendingInfo(info, playerLevelsDelta, ticksTillLevelDelta, changeMaterials));
        }

        result.add(info);
        return result;
    }

    class CustomAscendingInfo extends AscendInfo {
        private final AscendInfo source;
        private int playerLevelDelta;
        private int ticksDelta;
        private boolean changeMaterials;

        public CustomAscendingInfo(AscendInfo source, int playerLevelDelta, int ticksDelta, boolean changeMaterials) {
            super(null, 100, 0, null, null);
            this.source = source;
            this.playerLevelDelta = playerLevelDelta;
            this.ticksDelta = ticksDelta;
            this.changeMaterials = changeMaterials;
        }

        @Override
        public int getPlayerLevels() {
            return source.getPlayerLevels() + playerLevelDelta;
        }

        @Override
        public List<Component> getInfo() {
            return source.getInfo();
        }

        @Override
        public long getTicksTillLevel() {
            return source.getTicksTillLevel() + ticksDelta;
        }

        @Override
        public NonNullList<ItemStack> getMaterials() {

            List<ItemStack> stacks = source.getMaterials().stream().map(ItemStack::copy).collect(Collectors.toList());

            if (changeMaterials) {
                ItemStack stack = stacks.stream().filter(x -> x.getCount() > 1).findFirst().orElse(null);

                if (stack != null) {
                    stack.shrink(1);
                } else {
                    if (stacks.size() >= 4) {
                        stacks.remove(0);
                    } else {
                        stacks.add(Items.REDSTONE.getDefaultInstance());
                    }
                }
            }

            return NonNullList.of(ItemStack.EMPTY, stacks.toArray(ItemStack[]::new));
        }

        @Override
        public String toString() {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("playerLevels", getPlayerLevels());
            jsonObject.addProperty("ticks", getTicksTillLevel());
            JsonArray jsonArray = new JsonArray();
            jsonObject.add("materials", jsonArray);
            for (ItemStack material : getMaterials()) {
                JsonObject item = new JsonObject();
                jsonArray.add(item);
                item.addProperty("name", material.getDisplayName().getString());
                item.addProperty("count", material.getCount());
            }

            return jsonObject.toString();
        }
    }
}
