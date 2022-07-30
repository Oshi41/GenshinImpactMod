package com.gim.tests.register;

import net.minecraft.gametest.framework.GameTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomGameTest {
    int timeoutTicks() default 100;

    String batch() default "defaultBatch";

    int rotationSteps() default 0;

    boolean required() default true;

    long setupTicks() default 0L;

    int attempts() default 1;

    int requiredSuccesses() default 1;
}
