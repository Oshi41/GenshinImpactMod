package com.gim.tests.register;


import com.gim.GenshinImpactMod;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import net.minecraftforge.gametest.GameTestHolder;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRegistry {
    private static final Field TEST_FUNCTIONS_FIELD = ObfuscationReflectionHelper.findField(GameTestRegistry.class, "f_177495_");
    private static final Field TEST_CLASS_NAMES_FIELD = ObfuscationReflectionHelper.findField(GameTestRegistry.class, "f_177496_");
    private static final Type GAME_TEST_HOLDER = Type.getType(GameTestHolder.class);

    // instances for class calling
    private static final Map<Class, Object> instances = new HashMap<>();

    public static void register() {
        try {
            Collection<TestFunction> testFunctions = (Collection<TestFunction>) TEST_FUNCTIONS_FIELD.get(null);
            Set<String> testClasses = (Set<String>) TEST_CLASS_NAMES_FIELD.get(null);

            for (TestFunction testFunction : getAllTests()) {
                testFunctions.add(testFunction);
                testClasses.add(testFunction.getBatchName());
            }

        } catch (IllegalAccessException e) {
            throw new ReportedException(CrashReport.forThrowable(e, "Cannot get static fields"));
        }
    }

    private static Collection<TestFunction> getAllTests() {
        Set<String> enabledNamespaces = getEnabledNamespaces();
        if (!enabledNamespaces.isEmpty() && !enabledNamespaces.contains(GenshinImpactMod.ModID)) {
            return new HashSet<>();
        }

        List<TestFunction> functionList = ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .filter(a -> GAME_TEST_HOLDER.equals(a.annotationType()))
                .flatMap(TestRegistry::getMethods)
                .map(TestRegistry::convert)
                .filter(Objects::nonNull)
                .toList();

        return functionList;
    }

    private static Stream<Method> getMethods(ModFileScanData.AnnotationData data) {
        try {
            Class<?> clazz = Class.forName(data.clazz().getClassName(), true, ForgeGameTestHooks.class.getClassLoader());
            return Arrays.stream(clazz.getDeclaredMethods());
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.warn(e);
            return Stream.of();
        }
    }

    @Nullable
    private static TestFunction convert(Method method) {
        if (method == null)
            return null;

        String simpleName = method.getDeclaringClass().getSimpleName();
        CustomGameTest noTemplateGameTest = method.getAnnotation(CustomGameTest.class);
        if (noTemplateGameTest == null)
            return null;


        return new TestFunction(
                simpleName,
                method.getName().toLowerCase(),
                "",
                Rotation.NONE,
                noTemplateGameTest.timeoutTicks(),
                noTemplateGameTest.setupTicks(),
                noTemplateGameTest.required(),
                noTemplateGameTest.requiredSuccesses(),
                1,
                helper -> {
                    turnMethodIntoConsumer(method).accept(helper);
                    helper.runAfterDelay(noTemplateGameTest.timeoutTicks() - 1, helper::succeed);
                }
        );
    }

    private static <T> Consumer<T> turnMethodIntoConsumer(Method method) {
        return (p_177512_) -> {
            try {
                Object object = null;

                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    Class<?> declaringClass = method.getDeclaringClass();
                    if (!instances.containsKey(declaringClass)) {
                        instances.put(declaringClass, declaringClass.newInstance());
                    }

                    object = instances.get(declaringClass);
                }

                method.invoke(object, p_177512_);
            } catch (InvocationTargetException invocationtargetexception) {
                if (invocationtargetexception.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) invocationtargetexception.getCause();
                } else {
                    throw new RuntimeException(invocationtargetexception.getCause());
                }
            } catch (ReflectiveOperationException reflectiveoperationexception) {
                throw new RuntimeException(reflectiveoperationexception);
            }
        };
    }

    private static Set<String> getEnabledNamespaces() {
        String enabledNamespacesStr = System.getProperty("forge.enabledGameTestNamespaces");
        if (enabledNamespacesStr == null) {
            return Set.of();
        }

        return Arrays.stream(enabledNamespacesStr.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toUnmodifiableSet());
    }
}
