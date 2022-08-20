package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinDamageSource;
import com.gim.entity.Energy;
import com.gim.networking.TextParticleMsg;
import com.gim.registry.Elementals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShowDamage {
    private final static List<Player> cryticalHits = new ArrayList<>();

    private final static List<TextParticle> particles = new ArrayList<>();

    public static class TextParticle {
        private static final float MAX_LIVING_TICKS = 50;
        private static final float MIN_SCALE = 0.025f;
        private static final float MAX_SCALE = MIN_SCALE * 3;

        private int livingTicks;
        final Component text;
        private final Entity owner;
        private final IndicatingType type;
        final Vec3 offset;

        Vec3 pos;
        Vec3 prevPos;

        public TextParticle(Component text, Entity owner, IndicatingType type) {
            this.text = text;
            this.owner = owner;
            this.type = type;

            Random random = owner.getLevel().getRandom();

            offset = new Vec3(random.nextGaussian() * 0.008, 0.03 + Math.abs(random.nextGaussian()) * 0.008, random.nextGaussian() * 0.008);
            pos = new Vec3(owner.getX(), owner.getY() + owner.getBbHeight() + 0.5, owner.getZ())
                    .add(random.nextDouble(2.) - 1, 0, random.nextDouble(2.) - 1);
            prevPos = pos;
        }

        @OnlyIn(Dist.CLIENT)
        public TextParticle(FriendlyByteBuf buf) {
            Level level = net.minecraft.client.Minecraft.getInstance().player.getLevel();

            owner = level.getEntity(buf.readInt());
            type = IndicatingType.values()[buf.readByte()];
            text = buf.readComponent();
            livingTicks = buf.readInt();
            offset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

            prevPos = pos;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(owner.getId());
            buf.writeByte(type.ordinal());
            buf.writeComponent(text);
            buf.writeInt(livingTicks);

            buf.writeDouble(offset.x);
            buf.writeDouble(offset.y);
            buf.writeDouble(offset.z);

            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
        }

        /**
         * Called every tick
         * Recalculates position
         */
        public void tick() {
            prevPos = pos;
            pos = pos.add(offset.scale(Math.pow(0.99, livingTicks)));
            livingTicks++;
        }

        /**
         * Is current texture expired
         */
        public boolean isExpired() {
            return livingTicks >= MAX_LIVING_TICKS;
        }

        /**
         * Gets current scale
         */
        public float getScale() {
            // scale cannot be smaller than min value
            float scale = MIN_SCALE;
            // half age - the biggest scale
            float halfAge = MAX_LIVING_TICKS / 2;
            // grow by tick
            float diffByTick = (MAX_SCALE - MIN_SCALE) / halfAge;
            float growTicksCount = livingTicks < halfAge
                    // grown scale per tick (not more than half life period)
                    ? livingTicks
                    // shrinking size after 1/2 life duration
                    : MAX_LIVING_TICKS - livingTicks;

            // scaling
            scale += growTicksCount * diffByTick;
            return scale;
        }

        public Vec3 getPos() {
            return pos;
        }

        public Component getText() {
            return text;
        }

        public int age() {
            return livingTicks;
        }

        public Entity getOwner() {
            return owner;
        }

        public IndicatingType getType() {
            return type;
        }

        public Vec3 getPrevPos() {
            return prevPos;
        }


    }

    /**
     * Indicating type for particle.
     * Order enum type is used as priority to show
     */
    public enum IndicatingType {
        DAMAGE,
        REACTION,
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyCritHit(CriticalHitEvent e) {
        if (e.getDamageModifier() > 1) {
            cryticalHits.add(e.getPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityHurt(LivingDamageEvent event) {
        if (event.getAmount() > Double.MIN_NORMAL) {
            LivingEntity entityLiving = event.getEntityLiving();
            DamageSource damageSource = event.getSource();

            if (entityLiving != null && damageSource != null) {

                if (!entityLiving.getLevel().isClientSide()) {
                    ChatFormatting format = ChatFormatting.GRAY;

                    if (event.getSource() instanceof GenshinDamageSource) {
                        GenshinDamageSource genshinDamageSource = (GenshinDamageSource) event.getSource();

                        if (genshinDamageSource.getElement() != null) {
                            format = genshinDamageSource.getElement().getChatColor();
                        }

                        if (genshinDamageSource.possibleReaction() != null) {
                            addTextInfo(new TextParticle(genshinDamageSource.possibleReaction().text, entityLiving, IndicatingType.REACTION));
                        }
                    }

                    TextComponent component = new TextComponent("" + Math.abs(Math.round(event.getAmount())));

                    Style style = component.getStyle().applyFormat(format);
                    if (cryticalHits.remove(event.getSource().getEntity())) {
                        style = style.withUnderlined(true);

                        // 10% of chance to make energy orb
                        if (entityLiving.getRandom().nextFloat() < 0.5) {
                            // adding element orb here

                            Elementals e = null;
                            if (event.getSource() instanceof GenshinDamageSource) {
                                e = ((GenshinDamageSource) event.getSource()).getElement();
                            }

                            Energy energy = new Energy(event.getSource().getEntity(), entityLiving, 1, e);
                            entityLiving.getLevel().addFreshEntity(energy);
                        }
                    }

                    component.setStyle(style);

                    addTextInfo(new TextParticle(component.copy(), entityLiving, IndicatingType.DAMAGE));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (net.minecraft.client.Minecraft.getInstance().isPaused() || event.phase == TickEvent.Phase.START) {
            return;
        }

        particles.forEach(TextParticle::tick);
        particles.removeIf(TextParticle::isExpired);
    }

    /**
     * Adding
     *
     * @param particle
     */
    private static void addTextInfo(TextParticle particle) {
        if (!particle.getOwner().getLevel().isClientSide()) {
            Vec3 position = particle.getOwner().position();
            Supplier<PacketDistributor.TargetPoint> supplier = PacketDistributor.TargetPoint.p(
                    position.x,
                    position.y,
                    position.z,
                    ForgeMod.NAMETAG_DISTANCE.get().getDefaultValue() / 2.,
                    particle.getOwner().getLevel().dimension()
            );

            PacketDistributor.PacketTarget packetTarget = PacketDistributor.NEAR.with(supplier);
            TextParticleMsg particleMsg = new TextParticleMsg(particle);

            // sending to all clients
            GenshinImpactMod.CHANNEL.send(packetTarget, particleMsg);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void addTextParticle(TextParticle particle) {
        particles.add(particle);
    }

    /**
     * Sorted particles to render
     * Immutable list
     */
    @OnlyIn(Dist.CLIENT)
    public static List<TextParticle> getAll() {
        return particles.stream()
                // Sorting by type of particles
                .sorted(Comparator.comparing(ShowDamage.TextParticle::getType))
                // sorting by age (new one at the end of list so they gonna cover old dmg indicators)
                .sorted(Comparator.comparing(ShowDamage.TextParticle::age))
                .toList();
    }
}
