package com.gim.client.entity.hilichurlian.hilichurl;

import com.gim.GenshinHeler;
import com.gim.client.GenshinClientHooks;
import com.gim.entity.hilichurlian.Hilichurl;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class HilichurlModel extends PlayerModel<Hilichurl> {
    /**
     * Contains initial poses for model parts
     */
    private final Map<ModelPart, Position> _initialPoses;
    private final ModelPart ear;

    public HilichurlModel(ModelPart modelPart, boolean p_170822_) {
        super(modelPart, p_170822_);
        ear = modelPart.getChild("ear");
        _initialPoses = Stream.of(head, body, rightArm, leftArm, leftLeg, rightLeg).collect(Collectors.toMap(
                x -> x,
                x -> new Vec3(x.x, x.y, x.z)
        ));
    }

    @Override
    public void setupAnim(Hilichurl hilichurl, float p_103396_, float p_103397_, float p_103398_, float p_103399_, float p_103400_) {
        super.setupAnim(hilichurl, p_103396_, p_103397_, p_103398_, p_103399_, p_103400_);

        // clear all model custom positions
        for (Map.Entry<ModelPart, Position> entry : _initialPoses.entrySet()) {
            Position position = entry.getValue();
            entry.getKey().setPos((float) position.x(), (float) position.y(), (float) position.z());
        }

        if (hilichurl.isMeleeAttacking() && hilichurl.attackAnim > 0) {
            rotate(rightArm,
                    null, null, 0,
                    new Vec3(-64.1f, -58.59f, 26.23f), new Vec3(-184.28f, -49.45f, -79.15f),
                    hilichurl.attackAnim);

        } else if (hilichurl.isRangeAttacking() && hilichurl.attackAnim > 0) {
            // is currently digging (2/3 time of attack)
            // swing time goes from MAX --> 0
            boolean isDigging = hilichurl.attackAnim < 0.66;

            // throw rock percentage
            float throwPercentage = isDigging
                    ? 0
                    : Mth.clamp((hilichurl.attackAnim - 0.66f) * 3, Float.MIN_NORMAL, 1);

            if (isDigging) {
                float duration = 10;
                float ticksAmount = Math.abs(((hilichurl.tickCount % (duration * 2 + 1)) - duration) / duration);

                // always look at floor
                rotate(head,
                        new Vec3(0, 5.5, -15.5), null, 0,
                        new Vec3(-45, 0, 0), Vec3.ZERO, 0);
                rotate(body,
                        new Vec3(0, 5, -8), null, 0,
                        new Vec3(60, 0, 0), Vec3.ZERO, 0);

                // digging items
                rotate(rightArm,
                        new Vec3(-5, 5, -8), null, 0,
                        Vec3.ZERO, new Vec3(60, 0, 0), ticksAmount);
                rotate(leftArm,
                        new Vec3(5, 5, -8), null, 0,
                        Vec3.ZERO, new Vec3(60, 0, 0), 1 - ticksAmount);
            } else {
                // rising up
                rotate(head,
                        new Vec3(0, 5.5, -15.5), null, throwPercentage,
                        null, Vec3.ZERO, throwPercentage);
                rotate(body,
                        new Vec3(0, 5, -8), null, throwPercentage,
                        null, Vec3.ZERO, throwPercentage);

                // rising arm for throwing
                rotate(rightArm,
                        new Vec3(-5, 5, -8), null, throwPercentage,
                        null, new Vec3(-139, 7.7, -12.8), throwPercentage);
                rotate(leftArm,
                        new Vec3(5, 5, -8), null, throwPercentage,
                        null, Vec3.ZERO, throwPercentage);
            }
        }

        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);

        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);

        jacket.copyFrom(body);

        ear.copyFrom(head);
        hat.copyFrom(head);
    }

    private void rotate(ModelPart part, @Nullable Position posStart, @Nullable Position posEnd, float posPercentage, @Nullable Position rotStart, @Nullable Position rotEnd, float rotPercentage) {
        if (posStart != null || posEnd != null) {
            if (posStart == null)
                posStart = _initialPoses.get(part);
            if (posEnd == null)
                posEnd = _initialPoses.get(part);

            part.x = (float) ((posEnd.x() - posStart.x()) * posPercentage + posStart.x());
            part.y = (float) ((posEnd.y() - posStart.y()) * posPercentage + posStart.y());
            part.z = (float) ((posEnd.z() - posStart.z()) * posPercentage + posStart.z());
        }

        if (rotStart != null || rotEnd != null) {
            if (rotStart == null)
                rotStart = new Vec3(part.xRot, part.yRot, part.zRot);
            if (rotEnd == null)
                rotEnd = new Vec3(part.xRot, part.yRot, part.zRot);

            part.xRot = (float) Math.toRadians((rotStart.x() - rotEnd.x()) * rotPercentage + rotStart.x());
            part.yRot = (float) Math.toRadians((rotStart.y() - rotEnd.y()) * rotPercentage + rotStart.y());
            part.zRot = (float) Math.toRadians((rotStart.z() - rotEnd.z()) * rotPercentage + rotStart.z());
        }
    }
}
