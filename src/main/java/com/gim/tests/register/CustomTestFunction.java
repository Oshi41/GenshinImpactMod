package com.gim.tests.register;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import java.util.function.Consumer;

public class CustomTestFunction extends TestFunction {

    public CustomTestFunction(String batchName, String testName, String structureName, Rotation rotation, int maxTicks, long setupTicks, boolean required,
                              int requiredSuccesses, int maxAttempts, Consumer<GameTestHelper> function) {
        super(batchName, testName, structureName, rotation, maxTicks, setupTicks, required, requiredSuccesses, maxAttempts, function);
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }

    public int getCustomAttempts() {
        return super.getMaxAttempts();
    }
}
