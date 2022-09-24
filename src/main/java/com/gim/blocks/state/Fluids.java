package com.gim.blocks.state;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.IExtensibleEnum;

import javax.annotation.Nullable;
import java.util.Arrays;

public enum Fluids implements IExtensibleEnum, StringRepresentable {
    EMPTY(net.minecraft.world.level.material.Fluids.EMPTY),
    WATER(net.minecraft.world.level.material.Fluids.WATER),
    LAVA(net.minecraft.world.level.material.Fluids.LAVA),
    ;

    private final Fluid fluid;

    Fluids(Fluid fluid) {

        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    /**
     * Special extending method
     * Will change with ASM
     *
     * @param name  - enum name
     * @param fluid - fluid type
     */
    public static Fluids create(String name, Fluid fluid) {
        throw new IllegalStateException("Enum not extended");
    }

    /**
     * Extending enum from fluid
     *
     * @param fluid - minercaft fluid
     */
    public static Fluids createEnum(Fluid fluid) {
        return create(fluid.getRegistryName().toDebugFileName(), fluid);
    }

    @Override
    public String getSerializedName() {
        return toString();
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    /**
     * Is no fluid present
     */
    public boolean isEmpty() {
        return getFluid() == net.minecraft.world.level.material.Fluids.EMPTY;
    }

    /**
     * Enum assigned to this fluid
     */
    public boolean same(Fluid fluid) {
        return getFluid() == fluid;
    }

    /**
     * Enum from Fluid class
     *
     * @param fluid - fluid
     */
    @Nullable
    public static Fluids from(Fluid fluid) {
        return Arrays.stream(values()).filter(x -> x.same(fluid)).findFirst().orElse(null);
    }
}
