package com.gim.entity.hilichurlian;

import com.gim.GenshinHeler;
import com.gim.entity.IBothAttacker;
import com.gim.entity.ICustomSwing;
import com.gim.entity.ThrowableItem;
import com.gim.goals.GenshinMeleeAttackGoal;
import com.gim.goals.GenshinRangeAttackGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.BitSet;
import java.util.function.Consumer;

public class Hilichurl extends Monster implements IBothAttacker, ICustomSwing {
    private static final EntityDataAccessor<BitSet> FLAGS = SynchedEntityData.defineId(Hilichurl.class,
            GenshinHeler.BIT_SET);

    public Hilichurl(EntityType<? extends Monster> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(FLAGS, new BitSet());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Slime.class, true));

        this.targetSelector.addGoal(3, new GenshinMeleeAttackGoal(this, 1, 30, false));
        this.targetSelector.addGoal(3, new GenshinRangeAttackGoal(this, 1, 60, 60, 10, 20));

        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
    }

    @Override
    public void performRangedAttack(LivingEntity entity, float p_33318_) {
        swing(InteractionHand.MAIN_HAND, true);

        if (getLevel().isClientSide())
            return;

        BlockState blockState = getLevel().getBlockState(blockPosition());
        if (blockState.isAir()) {
            blockState = Blocks.STONE.defaultBlockState();
        }

        ThrowableItem item = new ThrowableItem(this, blockState.getBlock().asItem().getDefaultInstance(), 0.5, null);
        getLevel().addFreshEntity(item);
    }

    @Override
    public int getMaxSwingTime() {
        // default value
        return isRangeAttacking()
                // dig animation
                ? 30
                // regular melee swing amount
                : 20;
    }

    @Override
    public void updateSwingTime() {
        updateSwingTimeFixed();
    }

    @Override
    public void swing(InteractionHand hand, boolean flag) {
        swingFixed(hand, flag);
    }

    @Override
    public void onFlagsChanged(Consumer<BitSet> change) {
        BitSet instance = BitSet.valueOf(getEntityData().get(FLAGS).toByteArray());
        change.accept(instance);
        getEntityData().set(FLAGS, instance);
    }

    @Override
    public BitSet getFlags() {
        return getEntityData().get(FLAGS);
    }
}
