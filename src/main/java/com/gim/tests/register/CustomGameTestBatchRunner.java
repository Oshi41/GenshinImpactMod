package com.gim.tests.register;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Special class for running tests without needed template
 */
public class CustomGameTestBatchRunner extends GameTestBatchRunner {
    private final Map<GameTestBatch, List<CustomGameTestInfo>> batches = new LinkedHashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerLevel serverLevel;
    private final GameTestTicker ticker;

    /**
     * Called from Core modification!
     */
    public CustomGameTestBatchRunner(Collection<GameTestBatch> batches, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker ticker, int testsPerRow) {
        super(batches.stream().filter(x -> x.getTestFunctions().stream().noneMatch(y -> y.getStructureName().isBlank())).toList(), blockPos, rotation, serverLevel, ticker, testsPerRow);
        this.serverLevel = serverLevel;
        this.ticker = ticker;

        batches = batches.stream().filter(x -> x.getTestFunctions().stream().anyMatch(y -> y.getStructureName().isBlank())).toList();

        for (GameTestBatch batch : batches) {
            List<CustomGameTestInfo> vals = batch.getTestFunctions().stream().map(x -> new CustomGameTestInfo(x, rotation, serverLevel)).toList();
            this.batches.put(batch, vals);
        }
    }

    @Override
    public List<GameTestInfo> getTestInfos() {
        Stream<GameTestInfo> stream = this.batches.values().stream().flatMap(x -> x.stream().map(y -> (GameTestInfo) y));
        Stream<GameTestInfo> resultStream = Streams.concat(super.getTestInfos().stream(), stream);
        return resultStream.toList();
    }

    @Override
    public void start() {
        runBatch(0);

        super.start();
    }

    void runBatch(final int index) {
        if (index >= this.batches.size())
            return;

        Map.Entry<GameTestBatch, List<CustomGameTestInfo>> entry = Iterators.get(this.batches.entrySet().iterator(), index);
        GameTestBatch testBatch = entry.getKey();
        List<CustomGameTestInfo> testInfoList = entry.getValue();

        LOGGER.info("Running custom test batch '{}' ({} tests)...", testBatch.getName(), testInfoList.size());
        testBatch.runBeforeBatchFunction(this.serverLevel);

        final MultipleTestTracker multipletesttracker = new MultipleTestTracker();
        testInfoList.forEach(multipletesttracker::addTestToTrack);

        multipletesttracker.addListener(new GameTestListener() {
            private void afterAll() {
                if (multipletesttracker.isDone()) {
                    testBatch.runAfterBatchFunction(CustomGameTestBatchRunner.this.serverLevel);

                    // run next batch
                    CustomGameTestBatchRunner.this.runBatch(index + 1);
                }
            }

            @Override
            public void testStructureLoaded(GameTestInfo p_127651_) {
                // ignored
            }

            @Override
            public void testPassed(GameTestInfo p_177494_) {
                afterAll();
            }

            @Override
            public void testFailed(GameTestInfo p_127652_) {
                afterAll();
            }
        });

        for (CustomGameTestInfo testInfo : testInfoList) {
            BlockPos pos = BlockPos.ZERO;
            GameTestRunner.runTest(testInfo, pos, this.ticker);
        }
    }
}
