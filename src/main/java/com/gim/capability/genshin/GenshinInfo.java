package com.gim.capability.genshin;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinCombatTracker;
import com.gim.networking.CapabilityUpdatePackage;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

public class GenshinInfo implements IGenshinInfo {
    private final Map<IGenshinPlayer, GenshinEntityData> allPlayers = new HashMap<>();
    private final ArrayList<IGenshinPlayer> stackOrder = new ArrayList<>();
    private final Map<IGenshinPlayer, GenshinEntityData> team = new MapView<>(allPlayers, stackOrder);
    private final GenshinCombatTracker tracker;
    private int index;
    private long nextSwitch;


    public GenshinInfo(LivingEntity entity) {
        // injecting own implementation for all entities
        tracker = ((GenshinCombatTracker) entity.getCombatTracker());

        // debug
        addNewCharacter(GenshinCharacters.ANEMO_TRAVELER, entity);
    }

    @Override
    public void tick(LivingEntity entity) {
        if (team.isEmpty())
            return;

        team.forEach((iGenshinPlayer, data) -> iGenshinPlayer.onTick(entity, this, tracker));

        GenshinEntityData entityData = getPersonInfo(current());
        if (entityData != null) {
            entityData.setHealth(entity, entity.getHealth());
        }
    }

    @Override
    public int currentIndex() {
        return index;
    }

    @Override
    public void onSwitchToIndex(LivingEntity holder, int newIndex) {
        IGenshinPlayer old = current();

        this.index = newIndex;
        this.nextSwitch = holder.tickCount + (20);

        IGenshinPlayer current = current();

        old.onSwitch(holder, this, this.tracker, false);
        current.onSwitch(holder, this, this.tracker, true);

        stackOrder.remove(current);
        stackOrder.add(0, current);

        CombatTracker combatTracker = holder.getCombatTracker();
        if (combatTracker instanceof GenshinCombatTracker) {
            // remove all prev player attack
            ((GenshinCombatTracker) combatTracker).removeAttacks(source -> source instanceof EntityDamageSource && "player".equals(source.getMsgId()));
        }

        getPersonInfo(current).applyToEntity(holder);
    }

    @Override
    public int ticksTillSwitch(LivingEntity holder) {

        if (holder instanceof Player && ((Player) holder).isCreative()) {
            return 0;
        }

        long diff = nextSwitch - holder.tickCount;
        return (int) Math.max(0, diff);
    }

    @Override
    public int ticksTillSkill(LivingEntity holder, IGenshinPlayer id) {
        return id.ticksTillSkill(holder, this.team.get(id), this.tracker);
    }

    @Override
    public int ticksTillBurst(LivingEntity holder, IGenshinPlayer id) {
        return id.ticksTillBurst(holder, this.team.get(id), this.tracker);
    }

    @Override
    public GenshinEntityData getPersonInfo(IGenshinPlayer id) {
        return allPlayers.get(id);
    }

    @Override
    public Collection<IGenshinPlayer> getAllPersonages() {
        return allPlayers.keySet();
    }

    @Override
    public Collection<IGenshinPlayer> currentStack() {
        return stackOrder;
    }

    @Override
    public void addNewCharacter(IGenshinPlayer character, LivingEntity holder) {
        GenshinEntityData entityData = new GenshinEntityData(holder, character, 0);
        allPlayers.put(character, entityData);

        if (stackOrder.size() < 4) {
            stackOrder.add(character);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Index", index);
        tag.putLong("NextSwitch", nextSwitch);

        tag.putInt("AllCount", allPlayers.size());
        tag.putInt("TeamCount", stackOrder.size());

        int i = 0;
        for (Map.Entry<IGenshinPlayer, GenshinEntityData> entry : allPlayers.entrySet()) {
            tag.putString(String.format("Player_%s", i), entry.getKey().getRegistryName().toString());
            tag.put(String.format("PlayerData_%s", i), entry.getValue().serializeNBT());
            i++;
        }

        for (i = 0; i < stackOrder.size(); i++) {
            tag.putString(String.format("Stack_%s", i), stackOrder.get(i).getRegistryName().toString());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        throw new NotImplementedException("Use deserializeNBT(CompoundTag,Level) instead");
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, LivingEntity holder) {
        int oldIndex = index;

        index = nbt.getInt("Index");
        nextSwitch = nbt.getInt("NextSwitch");

        allPlayers.clear();
        stackOrder.clear();

        int end = nbt.getInt("AllCount");
        for (int i = 0; i < end; i++) {
            IGenshinPlayer player = Registries.characters().getValue(new ResourceLocation(nbt.getString(String.format("Player_%s", i))));
            GenshinEntityData data = new GenshinEntityData(holder, player, 0);
            data.deserializeNBT(nbt.getCompound(String.format("PlayerData_%s", i)));

            allPlayers.put(player, data);
        }

        end = nbt.getInt("TeamCount");
        for (int i = 0; i < end; i++) {
            stackOrder.add(Registries.characters().getValue(new ResourceLocation(nbt.getString(String.format("Stack_%s", i)))));
        }


        // applying all genshin info to holder entity
        getPersonInfo(current()).applyToEntity(holder);
    }

    @Override
    public void onSkill(LivingEntity holder) {
        current().performSkill(holder, this, this.tracker);
    }

    @Override
    public void onBurst(LivingEntity holder) {
        current().performBurst(holder, this, this.tracker);
    }

    @Override
    public double consumeEnergy(LivingEntity holder, double energy, Elementals elemental) {
        if (energy > 0) {
            for (int i = 0; i < stackOrder.size(); i++) {
                IGenshinPlayer player = stackOrder.get(i);
                GenshinEntityData data = getPersonInfo(player);
                double multiplier = Math.pow(0.84, i);

                if (elemental == null) {
                    // 2 for non elementals
                    multiplier *= 2;
                } else {
                    multiplier *= Objects.equals(player.getElemental(), elemental)
                            // 3 for same element
                            ? 3
                            // only one for different
                            : 1;
                }

                // recharge bonus
                double bonus = Math.max(0, GenshinHeler.safeGetAttribute(data.getAttributes(), Attributes.recharge_bonus));
                data.burstInfo().receiveEnergy((int) (Math.ceil(energy * multiplier * bonus)), false);
            }

            sendUpdate(holder);
        }

        return energy;
    }

    /**
     * Sending update for current entity
     *
     * @param holder
     */
    private void sendUpdate(LivingEntity holder) {
        if (holder instanceof ServerPlayer) {
            PacketDistributor.PacketTarget distrib = PacketDistributor.PLAYER.with(() -> ((ServerPlayer) holder));
            CapabilityUpdatePackage updatePackage = new CapabilityUpdatePackage(Capabilities.GENSHIN_INFO, this);
            GenshinImpactMod.CHANNEL.send(distrib, updatePackage);
        }
    }
}
