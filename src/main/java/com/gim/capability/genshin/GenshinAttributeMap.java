package com.gim.capability.genshin;

import com.google.common.collect.Sets;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GenshinAttributeMap extends AttributeMap {
    private static final Field attributesField = ObfuscationReflectionHelper.findField(AttributeMap.class, "f_22139_");

    /**
     * Obtainig private field from attributes map
     *
     * @param map     - attributes map to retrieve private field in
     * @param preload - need to load all containing attributes? By default there is NO LOADED instances inside
     * @return loaded attribute instances
     */
    public static Map<Attribute, AttributeInstance> from(AttributeMap map, boolean preload) {
        try {
            // retrieving field
            Map<Attribute, AttributeInstance> instanceMap = (Map<Attribute, AttributeInstance>) attributesField.get(map);

            if (preload) {
                // loading all attributes in map
                for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
                    // if has attribute
                    if (map.hasAttribute(attribute) && !instanceMap.containsKey(attribute)) {
                        // trying to load this
                        map.getInstance(attribute);
                    }
                }
            }


            return instanceMap;
        } catch (IllegalAccessException e) {
            throw new ReportedException(CrashReport.forThrowable(e, "Cannot obtain attributes field"));
        }
    }

    private final AttributeSupplier supplier;
    private final Map<Attribute, AttributeInstance> attributes;

    private final Set<AttributeInstance> dirtyNotSyncableAttributes = Sets.newHashSet();
    private final ObservableSet<AttributeInstance> observedDirtySet;
    private BiConsumer<Attribute, AttributeMap> onChange;

    private AttributeMap observable;

    private boolean handleChanges;


    /**
     * Called from core modification
     * Replacing AttributeMap for LivingEntity
     */
    public GenshinAttributeMap(AttributeSupplier supplier) {
        super(supplier);
        this.supplier = supplier;
        attributes = from(this, false);
        observedDirtySet = new ObservableSet<>(super.getDirtyAttributes(), this::onDirtySetChanged);
    }

    public GenshinAttributeMap(AttributeSupplier supplier, BiConsumer<Attribute, AttributeMap> onChange) {
        this(supplier);
        this.onChange = onChange;
    }

    public GenshinAttributeMap syncFor(AttributeMap map) {
        observable = map;
        return this;
    }

    private void onDirtySetChanged(String name) {
        if ("clear".equals(name)) {
            dirtyNotSyncableAttributes.clear();
        }
    }

    @Nullable
    @Override
    public AttributeInstance getInstance(Attribute p_22147_) {
        return this.attributes.computeIfAbsent(p_22147_, (p_22188_) -> {
            handleChanges = true;
            AttributeInstance fromSupplier = this.supplier.createInstance(this::onAttributeModified, p_22188_);
            handleChanges = false;
            return fromSupplier;
        });
    }

    @Override
    public Set<AttributeInstance> getDirtyAttributes() {
        return observedDirtySet;
    }

    private void onAttributeModified(AttributeInstance p_22158_) {
        if (p_22158_.getAttribute().isClientSyncable()) {
            getDirtyAttributes().add(p_22158_);
        } else {
            dirtyNotSyncableAttributes.add(p_22158_);
        }

        updateInner(p_22158_.getAttribute());
    }

    private void updateInner(@Nullable Attribute attr) {
        if (handleChanges)
            return;

        if (observable != null) {

            // preventing chaotic changes
            if (observable instanceof GenshinAttributeMap) {
                ((GenshinAttributeMap) observable).handleChanges = true;
            }

            if (attr == null) {
                observable.assignValues(this);
            } else {
                AttributeInstance original = getInstance(attr);

                if (original != null) {
                    AttributeInstance toCopy = observable.getInstance(attr);

                    if (toCopy != null) {
                        toCopy.replaceFrom(original);
                    }
                }
            }

            if (observable instanceof GenshinAttributeMap) {
                ((GenshinAttributeMap) observable).handleChanges = false;
                BiConsumer<Attribute, AttributeMap> onChange = ((GenshinAttributeMap) observable).onChange;
                if (onChange != null) {
                    onChange.accept(attr, this);
                }
            }
        }

        if (onChange != null) {
            onChange.accept(attr, this);
        }
    }
}
