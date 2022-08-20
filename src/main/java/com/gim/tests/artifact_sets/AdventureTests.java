package com.gim.tests.artifact_sets;

import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import com.gim.registry.Items;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.gametest.GameTestHolder;

import java.text.DecimalFormat;
import java.util.List;

@GameTestHolder
public class AdventureTests {
    private static final DecimalFormat format = new DecimalFormat("###.##");
    private static final BlockPos orePos = new BlockPos(1, 1, 1);
    private static final List<Block> blocks = Lists.newArrayList(Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS);

    @CustomGameTest
    public void adventureSets_increaseHealth_healPlayerByBreakingBlocks(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        IGenshinInfo cap = TestHelper.getCap(helper, serverPlayer, Capabilities.GENSHIN_INFO);
        Container artifactsContainer = cap.getPersonInfo(cap.current()).getArtifactsContainer();

        float health = 5f;
        serverPlayer.setGameMode(GameType.SURVIVAL);
        serverPlayer.setHealth(health);

        // 0 - no set info
        // 1 - two pieces
        // 2 - full set

        for (int i = 0; i < 3; i++) {

            switch (i) {
                case 1:
                    artifactsContainer.setItem(ArtifactSlotType.FLOWER.ordinal(), Items.adventure_flower.getDefaultInstance());
                    artifactsContainer.setItem(ArtifactSlotType.FEATHER.ordinal(), Items.adventure_feather.getDefaultInstance());
                    break;

                case 2:
                    artifactsContainer.setItem(ArtifactSlotType.CROWN.ordinal(), Items.adventure_crown.getDefaultInstance());
                    artifactsContainer.setItem(ArtifactSlotType.CUP.ordinal(), Items.adventure_cup.getDefaultInstance());
                    break;
            }

            float maxHealth = serverPlayer.getMaxHealth();
            double expectedHealth = Attributes.MAX_HEALTH.getDefaultValue();
            if (i > 0) {
                expectedHealth += 70;
            }

            if (maxHealth < expectedHealth) {
                helper.fail(String.format("Max health is [%s] but should be [%s]. Wearing [%s] pieces of Adventure set",
                        format.format(maxHealth),
                        format.format(expectedHealth),
                        i * 2
                ));

            }

            for (Block block : blocks) {
                // setting block that should heal player
                helper.setBlock(orePos, block.defaultBlockState());
                // set player health
                serverPlayer.setHealth(5);

                // imitate block breaking
                ForgeHooks.onBlockBreakEvent(helper.getLevel(), serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, orePos);

                if (i == 2) {
                    if (serverPlayer.getHealth() <= 5) {
                        helper.fail(String.format("Health of %s is %s but should be more than 5!",
                                serverPlayer.getName().getString(),
                                format.format(serverPlayer.getHealth())
                        ));
                    }
                } else {
                    if (5 != serverPlayer.getHealth()) {
                        helper.fail(String.format("Health of %s was changed but should not!", serverPlayer.getName().getString()));
                    }
                }
            }
        }
    }
}
