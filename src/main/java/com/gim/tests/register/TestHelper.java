package com.gim.tests.register;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.Registries;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;

import java.util.*;

public class TestHelper {

    /**
     * Creating new server player for testing
     *
     * @param helper - game testing helper
     */
    public static ServerPlayer createFakePlayer(GameTestHelper helper, boolean fillCharacters) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "mock-test")) {
            @Override
            public void tick() {
                super.tick();

                if (!this.level.isClientSide && !this.containerMenu.stillValid(this)) {
                    this.closeContainer();
                    this.containerMenu = this.inventoryMenu;
                }

                ForgeHooks.onLivingUpdate(this);
            }
        };

        if (fillCharacters) {
            IGenshinInfo info = getCap(helper, player, Capabilities.GENSHIN_INFO);
            for (IGenshinPlayer genshinPlayer : Registries.characters().getValues()) {
                info.addNewCharacter(genshinPlayer, player);
            }
        }

        helper.getLevel().addNewPlayer(player);

        return player;
    }

    /**
     * Performs right click on block
     *
     * @return Opened container
     */
    public static <T> T rightClick(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        InteractionResult result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.ZERO, Direction.NORTH, pos, true));

        if (result == InteractionResult.FAIL) {
            helper.fail("Right click on block was failed", pos);
        }

        try {
            return (T) player.containerMenu;
        } catch (Exception e) {
            helper.fail(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns checked capability
     */
    public static <T> T getCap(GameTestHelper helper, Player player, Capability<T> cap) {
        T result = (T) player.getCapability(cap).orElse(null);
        if (result == null) {
            helper.fail(String.format("No capability {%s} provided", cap.getName()), player);
        }

        return result;
    }

    public static void setGameTime(ServerPlayer player, int ticks) {
        player.getStats().setValue(player, Stats.CUSTOM.get(Stats.PLAY_TIME), ticks);
    }

    /**
     * If current bit presented
     *
     * @param mask     - bit mask
     * @param bitIndex - bit number (from 1)
     * @return
     */
    public static Boolean isBitPresented(int mask, int bitIndex) {
        return (mask & 1 << bitIndex) == (1 << bitIndex);
    }

    /**
     * Generates wrong data from original json element
     * Can be used from test data
     *
     * @param wrong   - all wrong json elements
     * @param correct - correct one
     * @param exclude - excliding properties
     */
    public static void generateWrongData(List<JsonElement> wrong, JsonElement correct, String... exclude) {
        if (correct.isJsonObject()) {
            JsonObject jsonObject = correct.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                for (String val : generateWrongStrings(entry.getKey())) {
                    JsonObject copy = jsonObject.deepCopy();
                    copy.remove(entry.getKey());
                    copy.add(val, entry.getValue());
                    wrong.add(copy);
                }


                // exclude properties
                if (Arrays.stream(exclude).noneMatch(x -> Objects.equals(x, entry.getKey()))) {
                    ArrayList<JsonElement> wrongVals = new ArrayList<>();
                    generateWrongData(wrongVals, entry.getValue(), exclude);

                    for (JsonElement wrongElement : wrongVals) {
                        JsonObject copy = jsonObject.deepCopy();
                        copy.add(entry.getKey(), wrongElement);
                        wrong.add(copy);
                    }
                }
            }
        }

        // change all primitive values
        if (correct.isJsonPrimitive()) {
            JsonPrimitive primitive = correct.getAsJsonPrimitive();

            if (!primitive.isBoolean()) {
                wrong.add(new JsonPrimitive(false));
            }

            if (!primitive.isString()) {
                wrong.add(new JsonPrimitive("Some string"));
            }

            if (!primitive.isNumber()) {
                wrong.add(new JsonPrimitive(123.547f));
            }
        }

        if (correct.isJsonArray()) {
            JsonArray array = correct.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                ArrayList<JsonElement> wrongs = new ArrayList<>();
                generateWrongData(wrongs, element, exclude);

                for (JsonElement incorrect : wrongs) {
                    JsonArray copy = array.deepCopy();
                    copy.set(i, incorrect);
                    wrong.add(copy);
                }
            }
        }
    }

    /**
     * Generates wrong strings: with/without symbol and replaced
     */
    private static List<String> generateWrongStrings(String origin) {
        return List.of(
                origin + "1",
                origin.substring(1),
                origin.substring(0, origin.length() - 1),
                '試' + origin.substring(1)
        );
    }
}
