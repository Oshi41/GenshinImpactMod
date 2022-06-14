package com.gim.capability.genshin;

import com.google.common.base.Equivalence;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Comparator;
import java.util.Objects;

public class AttributeEquals extends Equivalence<AttributeInstance> {
    @Override
    protected boolean doEquivalent(AttributeInstance a, AttributeInstance b) {
        // same instance
        if (a == b)
            return true;

        if (!Objects.equals(a.getValue(), b.getValue())) {
            return false;
        }

        if (!Objects.equals(a.getAttribute(), b.getAttribute())) {
            return false;
        }

        if (!Objects.equals(
                a.getModifiers().stream().sorted(Comparator.comparing(AttributeModifier::getId)).toList(),
                b.getModifiers().stream().sorted(Comparator.comparing(AttributeModifier::getId)).toList())) {
            return false;
        }

        return true;
    }

    @Override
    protected int doHash(AttributeInstance attributeInstance) {
        return Objects.hash(attributeInstance.getAttribute().getDescriptionId(), attributeInstance.getValue());
    }
}
