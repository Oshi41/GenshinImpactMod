package com.gim.tests;

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
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.Arrays;
import java.util.List;

@GameTestHolder
public class LevelStationTests {

    @CustomGameTest(setupTicks = 1)
    public void increaseLevelSurvival(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createPlayer(helper, true);
        serverPlayer.setGameMode(GameType.SURVIVAL);
        helper.setBlock(BlockPos.ZERO, Blocks.level_station);
        LevelStationMenu menu = TestHelper.rightClick(helper, serverPlayer, BlockPos.ZERO);

        do {
            GenshinEntityData entityData = menu.current();
            while (entityData.getAttributes().getValue(Attributes.level) < Attributes.level.getMaxValue()) {
                AscendInfo correctInfo = menu.getForCurrent();

                // creating incorrect vartiants we use to check all conditions
                List<AscendInfo> infos = Arrays.asList(
                        new CustomAscendingInfo(correctInfo, -1, 0, null),
                        new CustomAscendingInfo(correctInfo, 0, -1, null),
                        new CustomAscendingInfo(correctInfo, 0, 0, false),
                        // only the last one is correct
                        new CustomAscendingInfo(correctInfo, 0, 0, null)
                );

                correctInfo = infos.get(infos.size() - 1);

                // iterating on all infos
                for (AscendInfo ascendInfo : infos) {
                    // clear all slots
                    menu.getSlot(0).container.clearContent();

                    // time condition
                    serverPlayer.getStats().setValue(serverPlayer, Stats.CUSTOM.get(Stats.PLAY_TIME), (int) ascendInfo.getTicksTillLevel());
                    // exp condition
                    serverPlayer.experienceLevel = ascendInfo.getPlayerLevels();
                    // material conditions
                    for (int i = 0; i < correctInfo.getMaterials().size(); i++) {
                        if (!menu.getSlot(i).safeInsert(correctInfo.getMaterials().get(i)).isEmpty()) {
                            helper.fail("Error while inserting in level station container", BlockPos.ZERO);
                        }
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

    class CustomAscendingInfo extends AscendInfo {
        private final AscendInfo source;
        private int playerLevelDelta;
        private int ticksDelta;
        private Boolean addOrRemoveItem;

        public CustomAscendingInfo(AscendInfo source, int playerLevelDelta, int ticksDelta, Boolean addOrRemoveItem) {
            super(null, 100, 0, null, null);
            this.source = source;
            this.playerLevelDelta = playerLevelDelta;
            this.ticksDelta = ticksDelta;
            this.addOrRemoveItem = addOrRemoveItem;
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
            NonNullList<ItemStack> materials = NonNullList.of(ItemStack.EMPTY, source.getMaterials().toArray(ItemStack[]::new));

            if (addOrRemoveItem != null) {
                if (addOrRemoveItem) {
                    materials.add(0, new ItemStack(net.minecraft.world.level.block.Blocks.ANVIL));
                } else {
                    materials.remove(0);
                }
            }

            return materials;
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
