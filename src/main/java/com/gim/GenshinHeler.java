package com.gim;

import com.gim.attack.GenshinDamageSource;
import com.gim.capability.shield.IShield;
import com.gim.entity.ICustomSwing;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.gim.registry.Entities;
import com.google.common.collect.*;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

public class GenshinHeler {

    /**
     * Serializer for flags
     */
    public static final EntityDataSerializer<BitSet> BIT_SET = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf byteBuf, BitSet bitSet) {
            byte[] bytes = bitSet.toByteArray();

            byteBuf.writeByte(bytes.length);
            for (byte b : bytes) {
                byteBuf.writeByte(b);
            }
        }

        @Override
        public BitSet read(FriendlyByteBuf byteBuf) {
            byte[] bytes = new byte[byteBuf.readByte()];

            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = byteBuf.readByte();
            }

            return BitSet.valueOf(bytes);
        }

        @Override
        public BitSet copy(BitSet bitSet) {
            return BitSet.valueOf(bitSet.toByteArray());
        }
    };

    /**
     * Set of entities forbidden to apply genshin elemental statuses or attribute scaling
     */
    public static final Set<EntityType> FORBIDDEN = new HashSet<>();

    static {
        EntityDataSerializers.registerSerializer(BIT_SET);
    }

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
                ? (float) (16 * value / (value + 2000))
                : 0;
    }

    /**
     * Defence bonus
     * https://genshin-impact.fandom.com/wiki/Damage#Defense
     *
     * @param e - current entity
     * @return - bonus of defence attribute
     */
    public static float defenceBonus(Entity e, float damage, double attackerLevel) {
        double defence = safeGetAttribute(e, Attributes.defence);
        double victimLevel = safeGetAttribute(e, Attributes.level);

        double reduction = (defence + (int) victimLevel) / (30 + defence + 3 * attackerLevel);
        double multiplier = 1 - reduction;
        return (float) (damage * multiplier);
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
        // bonus for current elemental attack. Can be zero if non elemental attack happens
        float elementalBonus = 0;
        // elemental resistance. Can be 0 if non elemental attack happens
        float elementalResistance = 0;

        // some null checking
        if (source.getEntity() instanceof LivingEntity attacker) {

            // find elemental from attack
            Elementals elemental = Arrays.stream(Elementals.values()).filter(x -> x.is(source)).findFirst().orElse(null);
            // if elemental attack hapens
            if (elemental != null) {
                // checking possible bonus for current elemental
                if (elemental.getBonus() != null && (!(source instanceof GenshinDamageSource) || !((GenshinDamageSource) source).shouldIgnoreBonus())) {
                    elementalBonus = (float) Math.max(0, safeGetAttribute(attacker, elemental.getBonus()));
                }

                // checking possible resistance for current elemental
                if (elemental.getResistance() != null && (!(source instanceof GenshinDamageSource) || !((GenshinDamageSource) source).shouldIgnoreElementalResistance())) {
                    elementalResistance = (float) Math.max(0, safeGetAttribute(attacker, elemental.getResistance()));
                }
            }

            if ((!(source instanceof GenshinDamageSource) || !((GenshinDamageSource) source).shouldIgnoreResistance())) {
                double level = safeGetAttribute(attacker, Attributes.level);
                damage = defenceBonus(entity, damage, Math.max(0, level));
            }
        }

        // calcuating raw damage (by  majesty and elemental bonuses)
        float rawDamage = damage * (1 + elementalBonus);
        // calculating resistance for raw damage
        float resist = rawDamage * elementalResistance;

        // final result is damage without resist with defence
        AtomicDouble result = new AtomicDouble(rawDamage - resist);

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
     * @param e       - current entity
     * @param effects - list of removing effects
     * @return
     */
    public static boolean removeEffects(Entity e, MobEffect... effects) {
        if (e instanceof LivingEntity livingEntity
                && e.getLevel() instanceof ServerLevel serverLevel
                && effects != null
                && effects.length > 0) {
            int count = 0;

            for (MobEffect effect : effects) {
                if (livingEntity.removeEffect(effect)) {
                    serverLevel.getServer().getPlayerList().broadcast(
                            null,
                            e.getX(),
                            e.getY(),
                            e.getZ(),
                            16,
                            e.getLevel().dimension(),
                            new ClientboundRemoveMobEffectPacket(e.getId(), effect)
                    );

                    count++;
                }
            }

            return count > 0;
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

        return -1;
    }

    public static double safeGetAttribute(AttributeMap map, Attribute attribute) {
        if (map != null && attribute != null) {
            if (map.hasAttribute(attribute)) {
                AttributeInstance instance = map.getInstance(attribute);
                if (instance != null) {
                    return instance.getValue();
                }
            }
        }

        return -1;
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
        if (clazz != null && name != null && !name.isEmpty()) {
            try {
                return (T) Enum.valueOf(clazz, name);
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.debug("Error during String-->Enum conversion");
                GenshinImpactMod.LOGGER.debug(e);
            }
        }

        return null;
    }

//    /**
//     * retrieves field value
//     *
//     * @param field  - field value
//     * @param source - object source (null for static)
//     * @param <T>    - type of field
//     * @return field of class
//     */
//    @Nullable
//    public static <T> T safeGet(Field field, Object source) {
//        if (field != null) {
//            try {
//                return (T) field.get(source);
//            } catch (Exception e) {
//                GenshinImpactMod.LOGGER.debug(e);
//            }
//        }
//
//        return null;
//    }

    public static <T> int indexOf(Collection<T> collection, T item) {
        return Iterators.indexOf(collection.iterator(), input -> input == item);
    }

    /**
     * Setting transparency to color
     *
     * @param source - color source
     * @param alpha  - [0-255]
     * @return
     */
    public static Color withAlpha(Color source, float alpha) {
        return new Color(source.getRed() / 255f, source.getGreen() / 255f, source.getBlue() / 255f, alpha / 255f);
    }

    public static double gaussian(Random random, double from, double to) {
        double distance = to - from;
        double middle = from + distance / 2.0;
        double scale = to - middle;
        return random.nextGaussian(middle, scale);
    }


    private static final Field capabilityManagerField = ObfuscationReflectionHelper.findField(CapabilityProvider.class, "capabilities");
    private static final Field namesField = ObfuscationReflectionHelper.findField(CapabilityDispatcher.class, "names");
    private static final Field capsField = ObfuscationReflectionHelper.findField(CapabilityDispatcher.class, "caps");
    private static final Field providersField = ObfuscationReflectionHelper.findField(CapabilityManager.class, "providers");


    /**
     * Returns capability provider from entity
     *
     * @param owner   - capability owner
     * @param capName - capability name
     */
    @Nullable
    public static ICapabilityProvider from(CapabilityProvider owner, String capName) {
        try {
            Capability capability = ((Map<String, Capability>) providersField.get(CapabilityManager.INSTANCE)).get(capName.intern());

            if (capability != null) {
                CapabilityDispatcher dispatcher = (CapabilityDispatcher) capabilityManagerField.get(owner);
                ICapabilityProvider[] caps = (ICapabilityProvider[]) capsField.get(dispatcher);

                for (ICapabilityProvider cap : caps) {
                    if (cap.getCapability(capability).isPresent()) {
                        return cap;
                    }
                }
            }
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.debug("Error while retrieving ICapabilityProvider from Entity");
            GenshinImpactMod.LOGGER.debug(e);
        }

        return null;
    }

    public static AttributeMap union(AttributeMap... maps) {
        return new AttributeMap(unionSupplier(maps));
    }

    public static AttributeSupplier unionSupplier(AttributeMap... maps) {
        if (maps == null || maps.length == 0) {
            return new AttributeSupplier(new HashMap<>());
        }

        Map<Attribute, AttributeInstance> instanceMap = new HashMap<>();

        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            for (AttributeMap attributeMap : maps) {
                AttributeInstance instance = attributeMap.getInstance(attribute);
                if (instance != null) {
                    instanceMap.put(attribute, instance);
                }
            }
        }

        return new AttributeSupplier(instanceMap);
    }

    /**
     * Prints all info about current modifiers
     *
     * @param map - modifiers map
     */
    public static List<Component> from(Multimap<Attribute, AttributeModifier> map) {
        int i = 0;
        ArrayList<Component> components = new ArrayList<>();

        for (Map.Entry<Attribute, AttributeModifier> entry : map.entries()) {
            AttributeModifier attributemodifier = entry.getValue();
            double d0 = attributemodifier.getAmount();
            boolean flag = false;

            double d1;
            if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                if (entry.getKey().equals(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)) {
                    d1 = d0 * 10.0D;
                } else {
                    d1 = d0;
                }
            } else {
                d1 = d0 * 100.0D;
            }

            if (i == 0) {
                components.add(new TranslatableComponent("gim.main_stat"));
            }

            if (flag) {
                components.add((new net.minecraft.network.chat.TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            } else if (d0 > 0.0D) {
                components.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0D) {
                d1 *= -1.0D;
                components.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.RED));
            }

            i++;
        }

        return components;
    }

    public static List<Component> from(AttributeMap map) {
        HashMultimap<Attribute, AttributeModifier> multimap = HashMultimap.create();

        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            if (map.hasAttribute(attribute)) {
                for (AttributeModifier attributeModifier : map.getInstance(attribute).getModifiers()) {
                    multimap.put(attribute, attributeModifier);
                }
            }
        }

        return from(multimap);
    }

    /**
     * Returns swing amount
     *
     * @param entity - for this entity
     */
    public static int getCurrentSwingDuration(LivingEntity entity) {
        if (entity instanceof ICustomSwing) {
            return ((ICustomSwing) entity).getCurrentSwingDuration();
        }

        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else {
            return entity.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
        }
    }

    public static RecipeManager getRecipeManager(LivingEntity entity) {
        if (entity == null)
            return null;

        MinecraftServer server = entity.getLevel().isClientSide()
                ? clientServer()
                : entity.getLevel().getServer();

        if (server == null)
            return null;

        return server.getRecipeManager();
    }

    @OnlyIn(Dist.CLIENT)
    private static MinecraftServer clientServer() {
        return net.minecraft.client.Minecraft.getInstance().getSingleplayerServer();
    }

    /**
     * Returns random value inside the min and max values
     * Uses normal distribution that means min value chooses more often than max
     * inner calculation was made that last value chance approximately 1.05%
     *
     * @param random - current random
     * @param min    - min inclusive
     * @param max    - max inclusive
     * @return - random number inside diapason
     * @throws IllegalArgumentException
     */
    public static double getRandomNormalDistrib(Random random, double min, double max) {
        if (random == null) {
            throw new IllegalArgumentException("Random is null!");
        }

        if (min == max) {
            throw new IllegalArgumentException("Max and min parameter cannot be same number!");
        }

        double mean = Math.min(min, max);
        // some calculated value
        // can visualize here https://homepage.divms.uiowa.edu/~mbognar/applets/normal.html
        // so the last value is about 1.05% of chance
        double sd = (Math.max(min, max) - mean) / 4 * 1.3;

        // using only 'half' of normal distribution graphic
        double result = Math.abs(random.nextGaussian() * sd) + mean;
        return Mth.clamp(result, min, max);
    }

    /**
     * Randomly select keys based on own probability chance.
     *
     * @param map    - probability map. Contains key and probability number strictly more zero. Resulting chance is calculated
     *               by probability number / sum of all probability numbers inside map
     * @param random - random number
     * @param <T>    - any key type
     */
    public static <T, T1 extends Number> T selectWeighted(Map<T, T1> map, Random random) {
        if (map == null || map.isEmpty() || random == null) {
            throw new IllegalArgumentException(random == null ? "random is null" : "map is null or empty");
        }

        // storing intervals for key
        // intervals work as probability distribution.
        // random need to hit the interval for key to be selected
        Table<T, Double, Double> table = HashBasedTable.create();
        double total = 0;

        for (Map.Entry<T, T1> entry : map.entrySet()) {
            if (entry.getValue().doubleValue() < 1) {
                GenshinImpactMod.LOGGER.debug("Skip wrong key with probability: " + entry.getValue() + " for key " + entry.getKey().toString());
                continue;
            }

            table.put(entry.getKey(), total, total + entry.getValue().doubleValue());
            total += entry.getValue().doubleValue();
        }

        if (table.isEmpty() || total < 1) {
            throw new IllegalArgumentException("Seems like there is no valid possibility table. Check for negative or zero possibility values");
        }

        // choosing random number
        double hit = random.nextDouble(total + Double.MIN_NORMAL);

        // iterating through all table cells
        for (Table.Cell<T, Double, Double> cell : table.cellSet()) {
            // hit point should be inside interval
            if (cell.getColumnKey() <= hit && hit <= cell.getValue()) {
                return cell.getRowKey();
            }
        }

        GenshinImpactMod.LOGGER.debug("Seems like we didn't find any range. Workaround: just select value from original map");
        return map.keySet().stream().findFirst().orElse(null);
    }

    /**
     * Saving items inside list tag
     *
     * @param stacks - saved stacks. Do not saving empty stacks
     * @return
     */
    public static ListTag save(Collection<ItemStack> stacks) {
        ListTag result = new ListTag();

        if (stacks != null) {
            for (ItemStack itemStack : stacks) {
                if (itemStack.isEmpty()) continue;

                result.add(itemStack.save(new CompoundTag()));
            }
        }

        return result;
    }

    /**
     * Loading items from raw Tag. Loading only not empty stacks
     *
     * @param items  - Items tag. Should be ListTag type
     * @param stacks - array where want to load
     */
    public static void load(Tag items, Collection<ItemStack> stacks) {
        if (items instanceof ListTag && stacks != null) {
            // clear prev values
            stacks.clear();

            for (Tag tag : ((ListTag) items)) {
                if (tag instanceof CompoundTag) {
                    ItemStack stack = ItemStack.of((CompoundTag) tag);

                    // adding non empty stack
                    if (!stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
        }
    }

    /**
     * Saving owner inside tag
     *
     * @param entity - owner
     * @param tag    - tag compound
     */
    public static void save(Entity entity, CompoundTag tag) {
        if (entity != null && tag != null) {
            tag.putInt("OwnerID", entity.getId());
            tag.putUUID("OwnerUUID", entity.getUUID());
        }
    }

    /**
     * Loading owner from tag
     *
     * @param tag   - compound tag
     * @param level - current level
     */
    @Nullable
    public static <T extends Entity> T load(CompoundTag tag, Level level) {
        Entity result = null;

        if (tag != null && level != null) {
            if (tag.contains("OwnerID")) {
                result = level.getEntity(tag.getInt("OwnerID"));
            }

            if (tag.hasUUID("OwnerUUID")) {
                UUID uuid = tag.getUUID("OwnerUUID");

                // wrong UUID entity
                if (result != null && result.getUUID() != uuid) {
                    result = null;
                }

                if (result == null && level.getServer() != null) {
                    result = level.getServer().getPlayerList().getPlayer(uuid);
                }
            }
        }

        if (result == null)
            return null;

        try {
            return (T) result;
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.warn("GenshinHeler.load(CompoundTag, Level)");
            GenshinImpactMod.LOGGER.warn(e);
            return null;
        }
    }

    /**
     * Can this entity type accept attribute scaling and applying genshin attributes
     *
     * @param type - entity type
     */
    public static boolean acceptGenshinEffects(EntityType type) {
        // type is not an LivingEntity
        if (type == null || !DefaultAttributes.hasSupplier(type))
            return false;

        return !FORBIDDEN.contains(type);
    }

    /**
     * Can this entity type accept attribute scaling and applying genshin attributes
     *
     * @param e - entity
     */
    public static boolean acceptGenshinEffects(Entity e) {
        return acceptGenshinEffects(e.getType());
    }

    /**
     * check if number belongs range [min, max]
     *
     * @param num          - number
     * @param minInclusive - min inclusive
     * @param maxInclusive - max inclusive
     */
    public static boolean between(Number num, Number minInclusive, Number maxInclusive) {
        return minInclusive.doubleValue() <= num.doubleValue()
                && num.doubleValue() <= maxInclusive.doubleValue();
    }
}
