package com.gim;

import com.gim.capability.shield.IShield;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class GenshinHeler {
    private static final Field playerByUuidField = ObfuscationReflectionHelper.findField(PlayerList.class, "f_11197_");

    /**
     * Returns elemental majesty bonus
     * https://genshin-impact.fandom.com/wiki/Damage#Amplifying_Reaction_Damage
     *
     * @param e - current entity
     * @return
     */
    public static float majestyBonus(Entity e) {

        double value = safeGetAttribute(e, Attributes.elemental_majesty);

        return value > 0
                ? (float) (16 * value / (value + 2000) / 100)
                : 0;
    }

    /**
     * Defence bonus
     *
     * @param e - current entity
     * @return - bonus of defence attribute
     */
    public static float defenceBonus(Entity e) {
        double defenceRaw = safeGetAttribute(e, Attributes.defence);
        int level = (int) safeGetAttribute(e, Attributes.level);

        return (float) (defenceRaw / (defenceRaw + 5f * (1f + level)));
    }

    /**
     * Calcuates main damage
     *
     * @param entity - victim
     * @param source - damage source
     * @param damage - damage amount
     * @return
     */
    public static float getActualDamage(LivingEntity entity, final DamageSource source, float damage) {
        // current attacker level
        int level = 1;
        // elemental majesty
        float majesty = 0;
        // bonus for current elemental attack. Can be zero if non elemental attack happens
        float elementalBonus = 0;
        // elemental resistance. Can be 0 if non elemental attack happens
        float elementalResistance = 0;
        // current defence bonus. Can be negative (kinda debuff, multiplying incoming damage)
        float defence = 0;

        // some null checking
        if (source.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getEntity();

            // calclulated majesty for attacker
            majesty = majestyBonus(attacker);
            level = (int) safeGetAttribute(entity, Attributes.level);

            // find elemental from attack
            Elementals elemental = Arrays.stream(Elementals.values()).filter(x -> x.is(source)).findFirst().orElse(null);
            // if elemental attack hapens
            if (elemental != null) {
                // checking possible bonus for current elemental
                if (elemental.getBonus() != null) {
                    elementalBonus = (float) safeGetAttribute(attacker, elemental.getBonus());
                }

                // checking possible resistance for current elemental
                if (elemental.getResistance() != null) {
                    elementalResistance = (float) safeGetAttribute(attacker, elemental.getResistance());
                }
            }

            defence = defenceBonus(attacker);
        }

        // calcuating raw damage (by level, majesty and elemental bonuses)
        float rawDamage = damage * level * (1 + majesty + elementalBonus);
        // calculating defence by raw damage
        float defenceValue = rawDamage * defence;
        // calculating resistance for raw damage
        float resist = rawDamage * (elementalResistance + majesty);

        // final result is damage without resist with defence
        AtomicDouble result = new AtomicDouble(rawDamage - defenceValue - resist);

        // applying shield values
        LazyOptional<IShield> optional = entity.getCapability(Capabilities.SHIELDS, null);
        optional.ifPresent(iShield -> {
            if (iShield.isAvailable()) {
                result.set(iShield.acceptDamage(entity, source, result.floatValue()));
            }
        });

        // returns damage result
        return result.floatValue();
    }

    /**
     * adds effect to entity and saves changes on client
     *
     * @param e      - current entity
     * @param effect - applicable effect
     * @return
     */
    public static boolean addEffect(Entity e, MobEffectInstance effect) {

        if (e instanceof LivingEntity
                && effect != null
                && !e.getLevel().isClientSide()
                && ((LivingEntity) e).addEffect(effect)) {
            ((ServerLevel) e.getLevel()).getServer().getPlayerList().broadcast(
                    null,
                    e.getX(),
                    e.getY(),
                    e.getZ(),
                    32,
                    e.getLevel().dimension(),
                    new ClientboundUpdateMobEffectPacket(e.getId(), effect)
            );

            return true;
        }

        return false;
    }

    /**
     * Removes effect from entity and saves changes to client
     *
     * @param e      - current entity
     * @param effect - removing effect
     * @return
     */
    public static boolean removeEffect(Entity e, MobEffect effect) {
        if (e instanceof LivingEntity
                && effect != null
                && !e.getLevel().isClientSide()
                && ((LivingEntity) e).removeEffect(effect)) {
            ((ServerLevel) e.getLevel()).getServer().getPlayerList().broadcast(
                    null,
                    e.getX(),
                    e.getY(),
                    e.getZ(),
                    16,
                    e.getLevel().dimension(),
                    new ClientboundRemoveMobEffectPacket(e.getId(), effect)
            );

            return true;
        }

        return false;
    }

    /**
     * Can damage source be applicable to current elemental types
     *
     * @param entity - current entity
     * @param source - damage source
     * @param first  - first elemental
     * @param other  - other possible elementals for reactions
     * @return
     */
    public static boolean canApply(LivingEntity entity, DamageSource source, Elementals first, Elementals... other) {
        if (entity != null && first != null && other != null && other.length > 0 && source != null) {
            if (first.is(entity)) {
                for (Elementals elementals : other) {
                    if (elementals.is(source)) {
                        return true;
                    }
                }
            }

            if (first.is(source)) {
                for (Elementals elementals : other) {
                    if (elementals.is(entity)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Safe attribute value
     *
     * @param entity    - current entity
     * @param attribute - current attribute
     * @return
     */
    public static double safeGetAttribute(Entity entity, Attribute attribute) {

        if (entity instanceof LivingEntity && attribute != null) {
            AttributeInstance instance = ((LivingEntity) entity).getAttribute(attribute);
            if (instance != null) {
                return instance.getValue();
            }
        }

        return 0;
    }

    public static void safeAddModifier(LivingEntity e, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = e.getAttribute(attribute);
        if (instance != null) {
            if (instance.hasModifier(modifier)) {
                instance.removeModifier(modifier);
            }

            instance.addPermanentModifier(modifier);
        }
    }

    /**
     * Safe getting enum value from string
     *
     * @param clazz - enum class
     * @param name  - name of enum
     * @param <T>   - type of enum
     * @return - enum member or null if not exist
     */
    @Nullable
    public static <T extends Enum<T>> T safeGet(Class<T> clazz, String name) {
        if (clazz != null || name != null) {
            try {
                return (T) Enum.valueOf(clazz, name);
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.debug(e);
            }
        }

        return null;
    }

    /**
     * retrieves field value
     *
     * @param field  - field value
     * @param source - object source (null for static)
     * @param <T>    - type of field
     * @return field of class
     */
    @Nullable
    public static <T> T safeGet(Field field, Object source) {
        if (field != null) {
            try {
                return (T) field.get(source);
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.debug(e);
            }
        }

        return null;
    }

    /**
     * Copied from Player list
     *
     * @param toRemove      - player to remove
     * @param createFunc    - player create function
     * @param saveInventory - should we save inventory
     * @return - respawned entity
     */
    public static ServerPlayer respawn(ServerPlayer toRemove, Function<ServerPlayer, ServerPlayer> createFunc, boolean saveInventory) {

        MinecraftServer server = toRemove.getServer();
        PlayerList players = server.getPlayerList();

        players.remove(toRemove);
        toRemove.getLevel().removePlayerImmediately(toRemove, Entity.RemovalReason.DISCARDED);
        BlockPos blockpos = toRemove.getRespawnPosition();
        float f = toRemove.getRespawnAngle();
        boolean flag = toRemove.isRespawnForced();
        ServerLevel serverlevel = server.getLevel(toRemove.getRespawnDimension());
        Optional<Vec3> optional;
        if (serverlevel != null && blockpos != null) {
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, saveInventory);
        } else {
            optional = Optional.empty();
        }

        ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : server.overworld();
        ServerPlayer serverplayer = createFunc.apply(toRemove);
        serverplayer.connection = toRemove.connection;
        serverplayer.restoreFrom(toRemove, saveInventory);
        serverplayer.setId(toRemove.getId());
        serverplayer.setMainArm(toRemove.getMainArm());

        for (String s : toRemove.getTags()) {
            serverplayer.addTag(s);
        }

        boolean flag2 = false;
        if (optional.isPresent()) {
            BlockState blockstate = serverlevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = optional.get();
            float f1;
            if (!blockstate.is(BlockTags.BEDS) && !flag1) {
                f1 = f;
            } else {
                Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
                f1 = (float) Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI) - 90.0D);
            }

            serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
            serverplayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
            flag2 = !saveInventory && flag1;
        } else if (blockpos != null) {
            serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while (!serverlevel1.noCollision(serverplayer) && serverplayer.getY() < (double) serverlevel1.getMaxBuildHeight()) {
            serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
        }

        LevelData leveldata = serverplayer.level.getLevelData();
        serverplayer.connection.send(new ClientboundRespawnPacket(serverplayer.level.dimensionType(), serverplayer.level.dimension(), BiomeManager.obfuscateSeed(serverplayer.getLevel().getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), serverplayer.getLevel().isDebug(), serverplayer.getLevel().isFlat(), saveInventory));
        serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
        serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
        serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
        players.sendLevelInfo(serverplayer, serverlevel1);
        players.sendPlayerPermissionLevel(serverplayer);
        serverlevel1.addRespawnedPlayer(serverplayer);
        players.addPlayer(serverplayer);

        Map<UUID, ServerPlayer> playersByUUID = safeGet(playerByUuidField, players);

        if (playersByUUID == null) {
            CrashReport crashreport = CrashReport.forThrowable(new Throwable("Cannot retrieve playersByUUID field from PlayerList class"), "Error during field retrieve");
            throw new ReportedException(crashreport);
        }

        playersByUUID.put(serverplayer.getUUID(), serverplayer);
        serverplayer.initInventoryMenu();
        serverplayer.setHealth(serverplayer.getHealth());
        ForgeEventFactory.firePlayerRespawnEvent(serverplayer, saveInventory);
        if (flag2) {
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), 1.0F, 1.0F));
        }

        return serverplayer;
    }
}
