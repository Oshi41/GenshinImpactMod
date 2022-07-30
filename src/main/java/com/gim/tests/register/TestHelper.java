package com.gim.tests.register;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.GenshinCharacters;
import com.gim.registry.Registries;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class TestHelper {

    /**
     * Creating new server player for testing
     *
     * @param helper - game testing helper
     */
    public static ServerPlayer createPlayer(GameTestHelper helper, boolean fillCharacters) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "mock-test"));

        if (fillCharacters) {
            IGenshinInfo info = getCap(helper, player, Capabilities.GENSHIN_INFO);
            for (IGenshinPlayer genshinPlayer : Registries.characters().getValues()) {
                info.addNewCharacter(genshinPlayer, player);
            }
        }

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
}
