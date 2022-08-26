package com.gim.tests.characters;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.AnemoTraveler;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.registry.Entities;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;

@GameTestHolder(GenshinImpactMod.ModID + ".character")
public class AnemoTravelerTests {
    private final int burstEnergy = 60;
    private final int burstCooldown = 20 * 15; // 15 seconds
    private final int skillCooldown = 20 * 8; // 8 seconds

    @CustomGameTest(timeoutTicks = 500)
    public void anemoTravelerTests_burstEnergy(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        serverPlayer.setGameMode(GameType.SURVIVAL);

        IGenshinInfo genshinInfo = TestHelper.getCap(helper, serverPlayer, Capabilities.GENSHIN_INFO);
        // looking for anemo traveler
        IGenshinPlayer anemoTraveler = genshinInfo.getAllPersonages().stream().filter(x -> x.getRegistryName().getPath().equals("anemo_traveler")).findFirst().orElse(null);
        if (anemoTraveler == null) {
            helper.fail("There is no anemo_traveler registered!", serverPlayer);
        }

        // setting current stack as anemo traveler only
        genshinInfo.setCurrentStack(serverPlayer, List.of(anemoTraveler));
        GenshinEntityData entityData = genshinInfo.getPersonInfo(anemoTraveler);

        IEnergyStorage energyStorage = entityData.energy();
        // remove all energy from character
        energyStorage.extractEnergy(Integer.MAX_VALUE, false);
        if (energyStorage.getEnergyStored() > 0) {
            helper.fail(String.format("Seems like burst energy for Anemo Traveler is still not zero: [%s]", energyStorage.getEnergyStored()), serverPlayer);
        }

        // checking energy condition
        for (int i = 0; i < burstEnergy; i++) {
            energyStorage.receiveEnergy(1, false);
            boolean canUseBurst = genshinInfo.canUseBurst(serverPlayer);
            boolean canActuallyUseBurst = i == burstEnergy - 1;
            if (canActuallyUseBurst != canUseBurst) {
                String text = String.format("Burst energy for anemo traveler is [%s], for launch [%s].\n" +
                                "%s launch but we %s",
                        energyStorage.getEnergyStored(),
                        burstEnergy,
                        canUseBurst ? "Should" : "Shouldn't",
                        canActuallyUseBurst ? "should" : "shouldn't"
                );
                helper.fail(text, serverPlayer);
            }
        }

        // performing burst
        genshinInfo.onBurst(serverPlayer);

        // tick we started burst
        int startTick = serverPlayer.tickCount;

        // refill energy so it's not a problem now
        energyStorage.receiveEnergy(burstEnergy, false);

        // checking that we can't use burst untill the last tick
        // checking we can't use skill during burst animation
        for (int i = 1; i <= burstCooldown + AnemoTraveler.BURST_ANIM_TIME; i++) {
            boolean canLaunchSkill = i >= AnemoTraveler.BURST_ANIM_TIME;
            boolean canLaunchBurst = i >= burstCooldown + AnemoTraveler.BURST_ANIM_TIME;

            helper.runAfterDelay(i, () -> {
                boolean canUseSkill = genshinInfo.canUseSkill(serverPlayer);
                boolean canUseBurst = genshinInfo.canUseBurst(serverPlayer);

                if (canLaunchBurst != canUseBurst) {
                    helper.fail(String.format("[1] We %s launch burst, but we actually %s. Burst animation tick [%s], ticks till last burst [%s], energy [%s]",
                            canLaunchBurst ? "should" : "shouldn't",
                            canUseBurst ? "should" : "shouldn't",
                            entityData.getBurstTicksAnim(),
                            serverPlayer.tickCount - startTick,
                            energyStorage.getEnergyStored()
                    ), serverPlayer);
                }

                if (canLaunchSkill != canUseSkill) {
                    helper.fail(String.format("[1] We %s launch skill, but we actually %s. Burst animation tick [%s], ticks till last burst [%s], energy [%s]",
                            canLaunchSkill ? "should" : "shouldn't",
                            canUseSkill ? "should" : "shouldn't",
                            entityData.getBurstTicksAnim(),
                            serverPlayer.tickCount - startTick,
                            energyStorage.getEnergyStored()
                    ), serverPlayer);
                }
            });
        }

        // running skill next tick after burst cooldown completed
        helper.runAfterDelay(burstCooldown + AnemoTraveler.BURST_ANIM_TIME + 1, () -> genshinInfo.onSkill(serverPlayer));

        // while skill animation cannot launch anything
        // after skill can launch burst
        // after cooldown can launch skill
        for (int i = 1; i <= skillCooldown + AnemoTraveler.SKILL_ANIM_TIME; i++) {
            int delay = burstCooldown + AnemoTraveler.BURST_ANIM_TIME + 1 + i;

            final boolean canLaunchSkill = i >= skillCooldown;
            final boolean canLaunchBurst = i >= AnemoTraveler.SKILL_ANIM_TIME;

            helper.runAfterDelay(delay, () -> {
                boolean canUseBurst = genshinInfo.canUseBurst(serverPlayer);
                boolean canUseSkill = genshinInfo.canUseSkill(serverPlayer);

                if (canLaunchBurst != canUseBurst) {
                    helper.fail(String.format("[2] We %s launch burst, but we actually %s. Skill animation tick [%s], ticks till last skill [%s], energy [%s]",
                            canLaunchBurst ? "should" : "shouldn't",
                            canUseBurst ? "should" : "shouldn't",
                            entityData.getSkillTicksAnim(),
                            serverPlayer.tickCount - startTick - burstCooldown - 1,
                            energyStorage.getEnergyStored()
                    ), serverPlayer);
                }

                if (canLaunchSkill != canUseSkill) {
                    helper.fail(String.format("[2] We %s launch skill, but we actually %s. Skill animation tick [%s], ticks till last skill [%s], energy [%s]",
                            canLaunchSkill ? "should" : "shouldn't",
                            canUseSkill ? "should" : "shouldn't",
                            entityData.getSkillTicksAnim(),
                            serverPlayer.tickCount - startTick - burstCooldown - 1,
                            energyStorage.getEnergyStored()
                    ), serverPlayer);
                }
            });

        }

        // checking if tornado entity was spawned
        helper.runAfterDelay(AnemoTraveler.BURST_ANIM_TIME + 1, () -> helper.assertEntityPresent(Entities.tornado, new BlockPos(3,3,3), 7));
    }
}
