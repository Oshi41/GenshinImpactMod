package com.gim.capability.genshin;

import com.gim.attack.GenshinDamageSource;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.GenshinCharacters;
import com.gim.registry.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GenshinInfo implements IGenshinInfo {
    private final List<CombatEntry> history = new ArrayList<>();
    private final List<IGenshinPlayer> allPlayers = new ArrayList<>();
    private final Map<IGenshinPlayer, GenshinEntityData> team = new LinkedHashMap<>();
    private int index;
    private long nextSwitch;

    public GenshinInfo(@Nullable LivingEntity entity) {
        allPlayers.add(GenshinCharacters.ANEMO_TRAVELER);
        GenshinEntityData entityData = new GenshinEntityData(
                new AttributeMap(new AttributeSupplier.Builder(GenshinCharacters.ANEMO_TRAVELER.getAttributes()).build()),
                entity.getActiveEffects(),
                entity.getHealth(),
                0,
                GenshinCharacters.ANEMO_TRAVELER
        );
        team.put(GenshinCharacters.ANEMO_TRAVELER, entityData);
    }

    @Override
    public void tick(LivingEntity entity) {
        if (!entity.getCombatTracker().isInCombat() && history.size() > 0) {
            // removing all attacks except for burst and skill
            history.removeIf(x -> {
                if (x.getSource() instanceof GenshinDamageSource) {
                    if (((GenshinDamageSource) x.getSource()).isBurst() || ((GenshinDamageSource) x.getSource()).isSkill()) {
                        // no need to delete this
                        return false;
                    }
                }

                return true;
            });
        }

        team.forEach((iGenshinPlayer, data) -> iGenshinPlayer.onTick(entity, this, history));
    }

    @Override
    public void recordAttack(CombatEntry entry) {
        history.add(entry);
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

        old.onSwitch(holder, this, history, false);
        current.onSwitch(holder, this, history, true);
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
        return id.ticksTillSkill(holder, this.team.get(id), this.history);
    }

    @Override
    public int ticksTillBurst(LivingEntity holder, IGenshinPlayer id) {
        return id.ticksTillBurst(holder, this.team.get(id), this.history);
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
        }

        tag.putInt("HistoryCount", history.size());
        for (int i = 0; i < history.size(); i++) {
            CombatEntry entry = history.get(i);
            tag.put("History_" + i, CombatEntrySerializable.serialize(entry));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        throw new NotImplementedException("Use deserializeNBT(CompoundTag,Level) instead");
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, LivingEntity holder) {
        index = nbt.getInt("Index");
        nextSwitch = nbt.getInt("NextSwitch");

        this.allPlayers.clear();
        int end = nbt.getInt("AllPlayersCount");
        for (int i = 0; i < end; i++) {
            ResourceLocation location = new ResourceLocation(nbt.getString("Player_" + i));
            this.allPlayers.add(Registries.CHARACTERS.get().getValue(location));
        }

        team.clear();
        end = nbt.getInt("TeamCount");
        for (int i = 0; i < end; i++) {
            ResourceLocation location = new ResourceLocation(nbt.getString("TeamId_" + i));
            IGenshinPlayer player = Registries.CHARACTERS.get().getValue(location);
            GenshinEntityData data = new GenshinEntityData();
            data.deserializeNBT(nbt.getCompound("TeamTag_" + i));
            team.put(player, data);
        }

        history.clear();
        end = nbt.getInt("HistoryCount");
        for (int i = 0; i < end; i++) {
            CompoundTag tag = nbt.getCompound("History_" + i);
            CombatEntry entry = CombatEntrySerializable.deserializeCombatEntry(holder.getLevel(), nbt);
            history.add(entry);
        }

        getPersonInfo(current()).applyToEntity(holder);

        // applying attributes
        onSwitchToIndex(holder, index);
    }

    @Override
    public void onSkill(LivingEntity holder) {
        current().performSkill(holder, this, history);
    }

    @Override
    public void onBurst(LivingEntity holder) {
        current().performBurst(holder, this, history);
    }
}
