package com.gim.artifacts.base;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ArtifactSetBase extends ForgeRegistryEntry<IArtifactSet> implements IArtifactSet {
    private final BaseComponent name;
    private final BaseComponent description;

    /**
     * @param name        - name of artifact set
     * @param description - desciption of current set
     */
    protected ArtifactSetBase(BaseComponent name, BaseComponent description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public BaseComponent name() {
        return name;
    }

    @Override
    public BaseComponent description() {
        return description;
    }

    @Override
    public void onTakeOff(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
        if (holder == null || info == null || data == null)
            return;

        data.getActiveSets().remove(this);
    }

    @Override
    public void onWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data) {
        if (holder == null || info == null || data == null)
            return;

        data.getActiveSets().add(this);
    }

    protected boolean isWearing(Container artifactContainer, Set<Item> possibleItems, int count) {
        if (artifactContainer == null || possibleItems == null || count < 1 || artifactContainer.isEmpty() || possibleItems.isEmpty()) {
            return false;
        }

        int find = 0;

        for (int i = 0; i < artifactContainer.getContainerSize(); i++) {
            Item fromContainer = artifactContainer.getItem(i).getItem();

            if (possibleItems.contains(fromContainer)) {
                find++;

                if (find >= count) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    protected GenshinEntityData getCurrent(LivingEntity entity) {
        if (entity != null) {
            IGenshinInfo genshinInfo = entity.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (genshinInfo != null) {
                GenshinEntityData info = genshinInfo.getPersonInfo(genshinInfo.current());
                if (info != null) {
                    return info;
                }
            }
        }

        return null;
    }

    protected <T extends Event> void subscribeIfActive(Class<T> clazz, Function<T, GenshinEntityData> fromEvent, BiConsumer<T, GenshinEntityData> handle) {
        if (clazz == null || fromEvent == null || handle == null)
            return;

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, clazz, e -> {
            GenshinEntityData entityData = fromEvent.apply(e);
            if (entityData != null && entityData.getActiveSets().contains(this)) {
                handle.accept(e, entityData);
            }
        });
    }
}
