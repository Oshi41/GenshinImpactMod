package com.gim.items;

import com.gim.players.base.IGenshinPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;

public class ConstellationItem extends Item {
    public final Lazy<IGenshinPlayer> assignedTo;

    public ConstellationItem(Properties p_41383_, Lazy<IGenshinPlayer> assignedTo) {
        super(p_41383_);
        this.assignedTo = assignedTo;
    }
}
