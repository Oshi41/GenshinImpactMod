package com.gim.networking;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.menu.ConstellationMenu;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StarClickedPackage {

    private IGenshinPlayer player;

    public StarClickedPackage(IGenshinPlayer player) {
        this.player = player;
    }

    public StarClickedPackage(FriendlyByteBuf friendlyByteBuf) {
        this(Registries.characters().getValue(new ResourceLocation(friendlyByteBuf.readUtf())));
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(player.getRegistryName().toString());
    }

    public boolean consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        return context.getDirection().getReceptionSide().isClient()
                ? consumeClient(context)
                : consumeServer(context);
    }

    private boolean consumeServer(NetworkEvent.Context context) {
        if (updateByMenu(context.getSender())) {
            context.getSender().containerMenu.broadcastChanges();
            GenshinImpactMod.CHANNEL.reply(this, context);
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean consumeClient(NetworkEvent.Context context) {
        net.minecraft.client.gui.screens.Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
        if (screen instanceof com.gim.client.screen.ConstellationScreen) {
            ((com.gim.client.screen.ConstellationScreen) screen).refresh(true);
        }
        return true;
    }

    private boolean updateByMenu(Player player) {
        if (player != null && player.containerMenu instanceof ConstellationMenu) {
            ConstellationMenu constellationMenu = (ConstellationMenu) player.containerMenu;

            Slot inputSlot = constellationMenu.getSlot(0);
            if (inputSlot.hasItem()) {
                IGenshinInfo genshinInfo = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
                if (genshinInfo != null) {
                    GenshinEntityData genshinEntityData = genshinInfo.getPersonInfo(this.player);
                    if (genshinEntityData != null) {
                        AttributeInstance instance = genshinEntityData.getAttributes().getInstance(Attributes.constellations);
                        if (instance != null) {
                            instance.setBaseValue(instance.getBaseValue() + 1);
                            this.player.onStarAdded(player, genshinInfo, (int) instance.getValue());

                            inputSlot.remove(1);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
