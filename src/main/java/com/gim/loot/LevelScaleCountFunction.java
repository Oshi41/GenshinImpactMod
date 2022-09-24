package com.gim.loot;

import com.gim.GenshinHeler;
import com.gim.registry.Attributes;
import com.gim.registry.LootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Function applies as set_count functions but depends on entity level
 */
public class LevelScaleCountFunction extends LootItemConditionalFunction {
    private final Tuple<Integer, Integer> count;
    private final Tuple<Integer, Integer> level;

    public LevelScaleCountFunction(LootItemCondition[] conditions, Tuple<Integer, Integer> count, Tuple<Integer, Integer> level) {
        super(conditions);
        this.count = count;
        this.level = level;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int amount = 0;

        double lvl = GenshinHeler.safeGetAttribute(context.getParamOrNull(LootContextParams.THIS_ENTITY), Attributes.level);

        int minLevel = level.getA();
        int maxLevel = level.getB();
        int minCount = count.getA();
        Integer maxCount = count.getB();

        // accepting current level
        if (minLevel <= lvl && lvl <= maxLevel) {
            double totalDiff = maxLevel - minLevel;
            double current = lvl - minLevel;
            double percentage = current / totalDiff;

            // 0 checking
            if (percentage == 0 && minCount == 0 && context.getRandom().nextInt(maxCount + 1) == 0) {
                amount = 1;
            } else {
                // calculating final amount
                amount = (int) ((maxCount - minCount) * percentage + minCount);
            }
        }

        amount = Mth.clamp(amount, 0, stack.getMaxStackSize());
        stack.setCount(amount);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.LEVEL_SCALE_CONDITION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LevelScaleCountFunction> {
        public void serialize(JsonObject json, LevelScaleCountFunction func, JsonSerializationContext context) {
            super.serialize(json, func, context);

            JsonObject count = new JsonObject();
            json.add("count", count);
            count.addProperty("min", func.count.getA());
            count.addProperty("max", func.count.getB());

            JsonObject level = new JsonObject();
            json.add("level", level);
            level.addProperty("min", func.level.getA());
            level.addProperty("max", func.level.getB());
        }

        public LevelScaleCountFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            JsonObject countJson = json.getAsJsonObject("count");
            Tuple<Integer, Integer> count = new Tuple<>(
                    GsonHelper.getAsInt(countJson, "min", 0),
                    GsonHelper.getAsInt(countJson, "max", 0)
            );

            if (count.getA() < 0) {
                throw new IllegalArgumentException("Count can't be less 0");
            }
            if (count.getB() <= count.getA()) {
                throw new IllegalArgumentException("Max count can't be equals or less than min count");
            }

            JsonObject levelJson = GsonHelper.getAsJsonObject(json, "level", new JsonObject());
            Tuple<Integer, Integer> level = new Tuple<>(
                    GsonHelper.getAsInt(levelJson, "min", 0),
                    GsonHelper.getAsInt(levelJson, "max", Integer.MAX_VALUE)
            );

            if (level.getA() < 0) {
                throw new IllegalArgumentException("Level can't be less 0");
            }

            if (level.getB() <= level.getA()) {
                throw new IllegalArgumentException("Max level can't be equals or less than min level");
            }

            return new LevelScaleCountFunction(conditions, count, level);
        }
    }
}
