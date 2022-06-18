package com.gim.artifacts.base;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IArtifactSet extends IForgeRegistryEntry<IArtifactSet> {

    /**
     * Name of current set
     */
    BaseComponent name();

    /**
     * Descriptoion of current set
     *
     * @return
     */
    BaseComponent description();

    /**
     * Called when current set is enabled
     *
     * @param holder - for holder
     * @param info   - genchin capability
     * @param data   - character data (can be in team but not active)
     */
    void onWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data);

    /**
     * Called when current set is disabled
     *
     * @param holder - for holder
     * @param info   - genchin capability
     * @param data   - character data (can be in team but not active)
     */
    void onTakeOff(LivingEntity holder, IGenshinInfo info, GenshinEntityData data);

    /**
     * Detects if character wearing this set
     *
     * @param holder - for holder
     * @param info   - genchin capability
     * @param data   - character data (can be in team but not active)
     */
    boolean isWearing(LivingEntity holder, IGenshinInfo info, GenshinEntityData data);

    /**
     * Called from tooltip to detect if item is a part of artifact set
     *
     * @param item - current item
     */
    boolean partOf(Item item);
}
