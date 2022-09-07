package com.gim.entity;

import com.gim.GenshinHeler;
import com.gim.attack.GenshinDamageSource;
import com.gim.registry.Entities;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ParametricTransformer extends LivingEntity {
    private static final String OwnerName = "Owner";
    private static final String OwnerNameId = "OwnerID";
    private final NonNullList<ItemStack> stacks = NonNullList.create();

    /**
     * damage for current process
     */
    private int damage;

    /**
     * Current parametric owner
     */
    private LivingEntity owner;

    public ParametricTransformer(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setCustomNameVisible(true);
    }

    public ParametricTransformer(LivingEntity owner, List<ItemStack> items, int damage) {
        this(Entities.parametric_transformer, owner.getLevel());
        this.owner = owner;
        this.stacks.addAll(items);
        this.damage = damage;

        updateName();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return stacks;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {
        // ignored
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource))
            return true;

        return damageSource instanceof GenshinDamageSource && ((GenshinDamageSource) damageSource).getElement() != null;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source))
            return;

        super.actuallyHurt(source, damage);
        updateName();
    }

    /**
     * Name contains amount of health percentage
     */
    private void updateName() {
        int amount = (int) (getMaxHealth() - getHealth());
        setCustomName(new TextComponent(amount + "%"));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getInt("Damage");
        GenshinHeler.load(tag.get("Items"), stacks);
        owner = GenshinHeler.load(tag, getLevel());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Damage", damage);
        tag.put("Items", GenshinHeler.save(stacks));
        GenshinHeler.save(owner, tag);
    }

    @Override
    public void die(DamageSource source) {
        if (!isRemoved() && !dead) {
            this.dead = true;

            if (owner instanceof Player && stacks != null && !stacks.isEmpty()) {
                awardOwner((Player) owner);
            }

            this.level.broadcastEntityEvent(this, (byte) 3);
        }
    }

    /**
     * Awarding owner with amount of item
     *
     * @param owner - placer
     */
    private void awardOwner(Player owner) {
        for (ItemStack stack : stacks) {
            owner.getInventory().placeItemBackInInventory(stack);
        }
    }
}
