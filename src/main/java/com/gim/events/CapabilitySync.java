package com.gim.events;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.networking.CapabilityUpdatePackage;
import com.gim.registry.Capabilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber()
public class CapabilitySync {
    private static final List<Capability> playerCaps = List.of(Capabilities.SHIELDS, Capabilities.GENSHIN_INFO);
    private static final List<Capability> livingEntityCaps = List.of(Capabilities.SHIELDS);

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent e) {
        if (!e.getWorld().isClientSide()) {
            if (e.getEntity() instanceof ServerPlayer livingEntity) {

                for (Capability cap : playerCaps) {
                    livingEntity.getCapability(cap).ifPresent(instance -> {
                        ICapabilityProvider provider = GenshinHeler.from(livingEntity, cap.getName());
                        if (provider instanceof INBTSerializable) {
                            Tag tag = ((INBTSerializable<?>) provider).serializeNBT();
                            if (tag instanceof CompoundTag) {
                                CapabilityUpdatePackage msg = new CapabilityUpdatePackage(cap.getName(), ((CompoundTag) tag));
                                GenshinImpactMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> livingEntity), msg);
                            }
                        }
                    });
                }
            } else if (e.getEntity() instanceof LivingEntity) {

            }
        }

        ////////////////////////////
        // DEBUG
        ///////////////////////////
        IGenshinInfo genshinInfo = e.getEntity().getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (genshinInfo != null) {
            GenshinEntityData genshinEntityData = genshinInfo.getPersonInfo(genshinInfo.current());
            if (genshinEntityData != null) {
                genshinEntityData.applyToEntity(((LivingEntity) e.getEntity()));
            }
        }
    }
}
