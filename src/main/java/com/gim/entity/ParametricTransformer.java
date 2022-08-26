package com.gim.entity;

import com.gim.attack.GenshinDamageSource;
import com.gim.recipe.ParametricTransformerRecipe;
import com.gim.registry.Entities;
import com.gim.registry.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class ParametricTransformer extends LivingEntity {
    private static final String OwnerName = "Owner";
    private static final String OwnerNameId = "OwnerID";
    private final NonNullList<ItemStack> stacks = NonNullList.create();

    /**
     * Current parametric owner
     */
    private LivingEntity owner;

    private ParametricTransformerRecipe recipe;

    public ParametricTransformer(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setCustomNameVisible(true);
    }

    public ParametricTransformer(LivingEntity owner, ParametricTransformerRecipe recipe) {
        this(Entities.parametric_transformer, owner.getLevel());
        this.owner = owner;
        this.recipe = recipe;
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
        if (isInvulnerableTo(source) || recipe == null)
            return;

        amount = recipe.getElementalDamage();
        super.actuallyHurt(source, amount);

        updateName();
    }

    private void updateName() {
        int amount = (int) (getMaxHealth() - getHealth());
        setCustomName(new TextComponent(amount + "%"));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains(OwnerName)) {
            int id = tag.getInt(OwnerName);
            Entity entity = getLevel().getEntity(id);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        if (owner != null && tag.contains(OwnerNameId)) {
            UUID uuid = tag.getUUID(OwnerNameId);
            if (uuid != owner.getUUID()) {
                // wrong owner
                owner = null;
            }
        }

        if (getLevel().getServer() != null) {
            ResourceLocation location = new ResourceLocation(tag.getString("Recipe"));
            getLevel().getServer().getRecipeManager().byKey(location).ifPresent(r -> {
                if (r instanceof ParametricTransformerRecipe) {
                    this.recipe = (ParametricTransformerRecipe) r;
                }
            });
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putInt(OwnerName, owner.getId());
            tag.putUUID(OwnerNameId, owner.getUUID());
        }

        tag.putString("Recipe", recipe != null ? recipe.getId().toString() : "");
    }

    @Override
    public void die(DamageSource source) {
        if (!isRemoved() && !dead) {
            this.dead = true;

            if (owner instanceof Player && recipe != null) {
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
        List<ItemStack> result = recipe.randomResultForDay();
        result.add(Items.parametric_transformer.getDefaultInstance());

        for (ItemStack stack : result) {
            owner.getInventory().placeItemBackInInventory(stack);
        }
    }
}
