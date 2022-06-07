package com.gim.capability.genshin;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinCombatTracker;
import com.gim.networking.CapabilityUpdatePackage;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.gim.registry.GenshinCharacters;
import com.gim.registry.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

public class GenshinInfo implements IGenshinInfo {
    private final List<IGenshinPlayer> allPlayers = new ArrayList<>();
    private final Map<IGenshinPlayer, GenshinEntityData> team = new LinkedHashMap<>();
    private final GenshinCombatTracker tracker;
    private int index;
    private long nextSwitch;

    private final ArrayList<IGenshinPlayer> stackOrder = new ArrayList<>();

    public GenshinInfo(LivingEntity entity) {
        // injecting own implementation for all entities
        tracker = ((GenshinCombatTracker) entity.getCombatTracker());

        //////////////////////////
        // FOR DEBUG PURPOSES!
        /////////////////////////
        allPlayers.add(GenshinCharacters.ANEMO_TRAVELER);
        GenshinEntityData entityData = new GenshinEntityData(
                new AttributeMap(new AttributeSupplier.Builder(GenshinCharacters.ANEMO_TRAVELER.getAttributes()).build()),
                entity.getActiveEffects(),
                entity.getHealth(),
                90,
                GenshinCharacters.ANEMO_TRAVELER
        );
        team.put(GenshinCharacters.ANEMO_TRAVELER, entityData);
    }

    @Override
    public void tick(LivingEntity entity) {
        team.forEach((iGenshinPlayer, data) -> iGenshinPlayer.onTick(entity, this, tracker));
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
        return team.getOrDefault(id, new GenshinEntityData());
    }

    @Override
    public List<IGenshinPlayer> getAllPersonages() {
        return allPlayers;
    }

    @Override
    public Collection<IGenshinPlayer> currentStack() {
        return team.keySet();
    }

    @Override
    public void onAddPersonage(IGenshinPlayer newPlayer) {
        getAllPersonages().add(newPlayer);
    }

    @Override
    public void updateByHolder(LivingEntity entity) {
        team.compute(current(), (iGenshinPlayer, genshinEntityInfo) -> genshinEntityInfo != null
                ? new GenshinEntityData(entity, genshinEntityInfo)
                : null);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("AllPlayersCount", getAllPersonages().size());
        for (int i = 0; i < getAllPersonages().size(); i++) {
            tag.putString("Player_" + i, getAllPersonages().get(i).getRegistryName().toString());
        }

        tag.putInt("Index", index);
        tag.putLong("NextSwitch", nextSwitch);
        tag.putInt("TeamCount", team.size());
        int current = -1;
        for (Map.Entry<IGenshinPlayer, GenshinEntityData> entry : team.entrySet()) {
            current++;

            tag.putString("TeamId_" + current, entry.getKey().getRegistryName().toString());
            tag.put("TeamTag_" + current, entry.getValue().serializeNBT());
            tag.putInt("TeamOrder_" + current, stackOrder.indexOf(entry.getKey()));
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

        this.allPlayers.clear();
        int end = nbt.getInt("AllPlayersCount");
        for (int i = 0; i < end; i++) {
            ResourceLocation location = new ResourceLocation(nbt.getString("Player_" + i));
            this.allPlayers.add(Registries.characters().getValue(location));
        }

        team.clear();
        stackOrder.clear();

        // storing here players by their indexes
        Map<Integer, IGenshinPlayer> stack = new HashMap<>();

        end = nbt.getInt("TeamCount");
        for (int i = 0; i < end; i++) {
            // ID of registered genshin character
            ResourceLocation location = new ResourceLocation(nbt.getString("TeamId_" + i));
            // find actual value
            IGenshinPlayer player = Registries.characters().getValue(location);
            // deserializing it's data
            GenshinEntityData data = new GenshinEntityData();
            data.deserializeNBT(nbt.getCompound("TeamTag_" + i));
            team.put(player, data);

            // find current character stack order
            int order = nbt.getInt("TeamOrder_" + i);
            if (order < 0) {
                // unique index based on current
                order = end + i;
            }

            // putting it to map
            stack.put(order, player);
        }

        stack.keySet().stream()
                // sorting backwards
                .sorted((o1, o2) -> Integer.compare(o2, o1))
                // pushing to stack from old ones
                .forEach(x -> {
                    stackOrder.add(stack.get(x));
                });


        // applying all genshin info to holder entity
        getPersonInfo(current()).applyToEntity(holder);

        // applying attributes
        if (oldIndex != index) {
            onSwitchToIndex(holder, index);
        }
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

                data.burstInfo().receiveEnergy((int) (Math.ceil(energy * multiplier)), false);
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
