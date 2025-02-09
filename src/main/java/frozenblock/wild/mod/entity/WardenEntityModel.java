package frozenblock.wild.mod.entity;

import frozenblock.wild.mod.liukrastapi.AnimationAPI;
import frozenblock.wild.mod.liukrastapi.MathAddon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.PI;

public class WardenEntityModel<T extends WardenEntity> extends EntityModel<WardenEntity> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart right_ear;
    private final ModelPart left_ear;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public WardenEntityModel(ModelPart root) {
        this.body = root.getChild("body");
        this.left_arm = this.body.getChild("left_arm");
        this.right_arm = this.body.getChild("right_arm");
        this.head = this.body.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData modelPartData1 = modelPartData.addChild("body", ModelPartBuilder.create().uv(0,26).cuboid(-9.0F, -21.0F, -5.0F, 18.0F, 21.0F, 11.0F), ModelTransform.pivot(0.0F,13.0F,0.0F));
        ModelPartData modelPartData2 = modelPartData1.addChild("head", ModelPartBuilder.create().uv(0,0).cuboid(-8.0F, -16.0F, -5.0F, 16.0F, 16.0F, 10.0F), ModelTransform.pivot(0.0F,-21.0F,0.0F));
        modelPartData2.addChild("right_ear", ModelPartBuilder.create().uv(106,36).cuboid(-10.0F, -6.5F, 0.0F, 10.0F, 10.0F, 0.001F), ModelTransform.pivot(-8.0F,-12.5F,0.0F));
        modelPartData2.addChild("left_ear", ModelPartBuilder.create().uv(106,46).cuboid(0.0F, -6.5F, 0.0F, 10.0F, 10.0F, 0.001F), ModelTransform.pivot(8.0F,-12.5F,0.0F));
        modelPartData1.addChild("right_arm", ModelPartBuilder.create().uv(52,0).cuboid(-6.0F, -4.0F, -4.0F, 8.0F, 28.0F, 8.0F), ModelTransform.pivot(-11.0F,-17.0F,0.0F));
        modelPartData1.addChild("left_arm", ModelPartBuilder.create().uv(84,0).cuboid(-2.0F, -4.0F, -4.0F, 8.0F, 28.0F, 8.0F), ModelTransform.pivot(11.0F,-17.0F,0.0F));
        modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(82,36).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), ModelTransform.pivot(6.0F,11.0F,0.0F));
        modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(58,36).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), ModelTransform.pivot(-6.0F,11.0F,0.0F));
        return TexturedModelData.of(modelData,128,64);
    }

    @Override
    public void setAngles(WardenEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

        int r = entity.getRoarTicksLeft1();
        long emergeticksleft = entity.clientEmergeTicks; //The time (in ticks) from the start of the Warden's emerge
        long sniffticks = entity.world.getTime()-entity.clientSniffStart; //The time between last sniffing started and now
        int digticks = entity.clientDigTicks; //How much time is left (in ticks) before digging ends.
        float time = animationProgress / 10;
            float t = 2; //Multiplier for animation length
            float j = (float) (180 / PI); //Converts degrees to radians

        if (entity.clientEmergeTicks>0 && animationProgress-entity.emergeAnimStartTime==animationProgress) {
            entity.emergeAnimStartTime=animationProgress;
        }
        if (sniffticks<1) {
            entity.sniffAnimStartTime=animationProgress;
        }

        if (entity.clientEmergeTicks>0) { //EMERGING
            entity.emergeAnimTime=AnimationAPI.animationTimer(animationProgress, entity.emergeAnimStartTime, entity.emergeAnimStartTime+160)/10;
            float emergeTime=entity.emergeAnimTime;
            float bodyY = 13;
            float legY = 11;
            float armY = -17; //Default pivots

            /* Body */
            this.body.pivotY = bodyY + 55 + (AnimationAPI.easeOutSine(t * 0f, t * 0.68f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.68f, t * 1.04f, -30f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.04f, t * 1.2f, +2f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.2f, t * 3.24f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.24f, t * 3.92f, -17f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.92f, t * 4.6f, -5f, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.6f, t * 4.76f, +1.13f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.76f, t * 5.48f, -1.13f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 5.48f, t * 5.84f, -6f, emergeTime)
            );
            this.body.roll = -10 / j + (AnimationAPI.easeOutSine(0, t * 1.12f, 0 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.12f, t * 1.24f, 7.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.28f, t * 1.56f, 13.05f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.56f, t * 2.08f, -10.55f / j, emergeTime)
            );
            this.body.pitch = (AnimationAPI.easeOutSine(0, t * 1.12f, 0 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.12f, t * 1.24f, 5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.28f, t * 1.56f, -16.1f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.56f, t * 2.08f, 11.1f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.8f, t * 3.76f, 32.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.76f, t * 4.4f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.4f, t * 5.04f, -12.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 5.04f, t * 6.04f, 25f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.04f, t * 7.44f, -67.5f / j, emergeTime)
            );

            /* Head */
            this.head.pitch = 95 / j + (AnimationAPI.easeOutSine(0, t * 0.76f, 0 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.76f, t * 1.2f, -120f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.2f, t * 1.44f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.44f, t * 1.84f, -32.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.84f, t * 2.48f, 10f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.48f, t * 2.8f, 42f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.8f, t * 3.76f, 70f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 3.76f, t * 4.92f, -30f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 4.92f, t * 6.16f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.16f, t * 7f, -57.5f / j, emergeTime)
            );
            this.head.roll = (AnimationAPI.easeOutSine(t * 0f, t * 1.2f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.2f, t * 1.44f, 2.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.44f, t * 1.84f, -2.5f / j, emergeTime)
            );

            /* Left Leg */
            this.left_leg.pivotY = legY + 55 + (AnimationAPI.easeOutSine(t * 0f, t * 0.68f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.68f, t * 1.04f, -30f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.04f, t * 1.2f, +2f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.24f, t * 3.92f, -17f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.92f, t * 4.6f, -5f, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.6f, t * 4.76f, +1.13f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.76f, t * 5.48f, -1.13f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 5.48f, t * 5.84f, -5f, emergeTime)
            );

            /* Right Leg */
            this.right_leg.pivotY = legY + 55 + (AnimationAPI.easeOutSine(t * 0f, t * 0.68f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.68f, t * 1.04f, -30f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.04f, t * 1.2f, +2f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.24f, t * 3.92f, -17f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.92f, t * 4.6f, -5f, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.6f, t * 4.76f, +1.13f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.76f, t * 5.48f, -1.13f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 5.48f, t * 5.84f, -5f, emergeTime)
            );

            /* Left Arm */
            this.left_arm.pivotY = armY + (AnimationAPI.easeOutSine(t * 0f, t * 0.48f, -25f, emergeTime) +
                    AnimationAPI.easeInSine(t * 0.48f, t * 0.85f, +23f, emergeTime) +
                    AnimationAPI.easeInSine(t * 0.85f, t * 1.08f, 0f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.08f, t * 1.2f, -2f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.2f, t * 2.8f, 0f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.8f, t * 3.84f, -5f, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.84f, t * 4.12f, +3f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.12f, t * 4.52f, -2f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.52f, t * 5.08f, +0.75f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 5.48f, +1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.04f, t * 7.08f, +6.25f, emergeTime)
            );
            this.left_arm.pivotZ = (AnimationAPI.easeOutSine(t * 0f, t * 2.8f, 0f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.8f, t * 3.84f, +3f, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.84f, t * 4.12f, -1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.12f, t * 4.52f, +4.6f, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.52f, t * 5.08f, -1.6f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 6.04f, -3.1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.04f, t * 7.08f, -1.9f, emergeTime)
            );
            this.left_arm.pitch = (AnimationAPI.easeOutSine(t * 0f, t * 0.46f, -185f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.46f, t * 0.52f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.52f, t * 1.08f, -5 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.08f, t * 1.16f, 65.4f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.16f, t * 1.24f, 30.1f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.24f, t * 1.32f, 5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.32f, t * 1.56f, 7.5f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 1.56f, t * 1.88f, -5 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.88f, t * 2f, -10 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2f, t * 2.08f, -2.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.08f, t * 2.24f, 10 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.24f, t * 2.68f, 0 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.68f, t * 2.8f, 5 / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.8f, t * 2.96f, -37.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.96f, t * 3.12f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.12f, t * 3.28f, 37.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.28f, t * 3.84f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.84f, t * 4.68f, 2.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.68f, t * 5.08f, 13.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 5.4f, -30.25f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 5.4f, t * 5.76f, 14.25f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.76f, t * 6.12f, -14.25f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 6.12f, t * 6.96f, 76.25f / j, emergeTime)
            );
            this.left_arm.yaw = (AnimationAPI.easeOutSine(t * 0f, t * 0.52f, 2f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.52f, t * 1.08f, 13f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.08f, t * 1.16f, -40f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.16f, t * 1.24f, 10f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.24f, t * 1.32f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.32f, t * 1.56f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 1.56f, t * 1.88f, -7.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.88f, t * 2f, -8 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2f, t * 2.08f, -6f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.08f, t * 2.24f, -1 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2.24f, t * 2.68f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.68f, t * 2.8f, -15 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2.8f, t * 2.96f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.96f, t * 3.12f, 30f / j, emergeTime)
            );
            this.left_arm.roll = (AnimationAPI.easeOutSine(t * 0f, t * 0.52f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 0.52f, t * 1.08f, -22.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.08f, t * 1.16f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.16f, t * 1.24f, 12.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.24f, t * 1.32f, -17.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.32f, t * 1.56f, 0f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 1.56f, t * 1.88f, 2.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.88f, t * 2f, 1 / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2f, t * 2.08f, 0.75f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.08f, t * 2.24f, 0.75f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2.24f, t * 2.68f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.68f, t * 2.8f, -2.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2.8f, t * 3.12f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.12f, t * 3.28f, 2.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.28f, t * 4.68f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.68f, t * 5.08f, 3f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 5.4f, 0.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 5.4f, t * 5.76f, 1f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.76f, t * 6.12f, -1f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 6.12f, t * 6.96f, -3.5f / j, emergeTime)
            );

            /* Right Arm */
            this.right_arm.pivotY = armY + (AnimationAPI.easeOutSine(t * 0f, t * 1.24f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.24f, t * 1.48f, -4.4f, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.48f, t * 1.64f, +0.4f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.64f, t * 2.8f, 0f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.8f, t * 3.84f, -3f, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.84f, t * 4.12f, -1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.12f, t * 4.52f, 0f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.52f, t * 5.08f, +0.75f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 5.48f, +1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.04f, t * 7.08f, +6.25f, emergeTime)
            );
            this.right_arm.pivotZ = (AnimationAPI.easeOutSine(t * 0f, t * 2.8f, 0f, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 2.8f, t * 3.84f, +3f, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.84f, t * 4.12f, -1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.12f, t * 4.52f, +4.6f, emergeTime) +
                    AnimationAPI.easeInSine(t * 4.52f, t * 5.08f, -1.6f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 6.04f, -3.1f, emergeTime) +
                    AnimationAPI.easeOutSine(t * 6.04f, t * 7.08f, -1.9f, emergeTime)
            );
            this.right_arm.pitch = 157.5f / j + (AnimationAPI.easeOutSine(t * 0f, t * 1.24f, 0f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.24f, t * 1.32f, -137.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.32f, t * 1.6f, 112.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.6f, t * 1.76f, 155f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.76f, t * 2.16f, -16f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 2.16f, t * 2.6f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 2.6f, t * 3.04f, -36.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.04f, t * 3.2f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 3.2f, t * 3.36f, 35.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.36f, t * 3.84f, 22.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 3.84f, t * 4.68f, 2.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 4.68f, t * 5.08f, 13.42f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.08f, t * 5.4f, -30.17f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 5.4f, t * 5.76f, 14.25f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 5.76f, t * 6.12f, -14.25f / j, emergeTime) +
                    AnimationAPI.easeInOutSine(t * 6.12f, t * 6.64f, 79.25f / j, emergeTime)
            );
            this.right_arm.yaw = 87.5f / j + (AnimationAPI.easeOutSine(t * 0f, t * 1.24f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.24f, t * 1.32f, -47.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.32f, t * 1.6f, -60f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.6f, t * 1.76f, 50f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.76f, t * 3.04f, -30f / j, emergeTime)
            );
            this.right_arm.roll = 180 / j + (AnimationAPI.easeOutSine(t * 0f, t * 1.24f, 0f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.24f, t * 1.32f, -147.5f / j, emergeTime) +
                    AnimationAPI.easeOutSine(t * 1.32f, t * 1.6f, -47.5f / j, emergeTime) +
                    AnimationAPI.easeInSine(t * 1.6f, t * 1.76f, 15f / j, emergeTime)
            );
        } else if (sniffticks < 44) { //SNIFFING
            entity.sniffAnimTime=AnimationAPI.animationTimer(animationProgress, entity.sniffAnimStartTime, entity.sniffAnimStartTime+43)/10;
            float sniffTime=entity.sniffAnimTime;
            /* Body */
            this.body.pitch = (AnimationAPI.easeOutSine(0, t * 0.52f, 7.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 2.08f, -15f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.64f, 7.5f / j, sniffTime)
            );
            this.body.yaw = (AnimationAPI.easeInOutSine(0, t * 0.52f, 27.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 2.08f, -55f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.64f, 27.5f / j, sniffTime)
            );
            this.body.roll = (AnimationAPI.easeOutSine(0, t * 0.52f, 12.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 2.08f, 0f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.64f, -12.5f / j, sniffTime)
            );

            /* Head */
            this.head.pitch = (AnimationAPI.easeInOutSine(0, t * 0.52f, -5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 0.72f, -15f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 0.72f, t * 0.92f, 15f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.92f, t * 1.12f, -15f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 1.12f, t * 1.32f, 15f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.32f, t * 1.52f, -15f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.52f, t * 1.8f, 15f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.8f, t * 2.64f, 5f / j, sniffTime)
            );
            this.head.yaw = (AnimationAPI.easeInOutSine(0, t * 0.52f, 27.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 0.72f, -15f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 0.72f, t * 0.92f, -5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.92f, t * 1.12f, -7.5f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 1.12f, t * 1.32f, -7.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.32f, t * 1.52f, -5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.52f, t * 1.8f, -7.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.8f, t * 2.64f, 20f / j, sniffTime)
            );
            this.head.roll = (AnimationAPI.easeInOutSine(0, t * 0.52f, 12.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.52f, t * 0.72f, -7.5f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 0.72f, t * 0.92f, -2.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.92f, t * 1.12f, -2.5f / j, sniffTime) +
                    AnimationAPI.easeInSine(t * 1.12f, t * 1.32f, -2.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.32f, t * 1.52f, -2.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.52f, t * 1.8f, 0f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 1.8f, t * 2.64f, 5f / j, sniffTime)
            );

            /* Left Arm */
            this.left_arm.pitch = (AnimationAPI.easeInOutSine(0, t * 0.4f, 30f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, -48f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.56f, 18f / j, sniffTime)
            );
            this.left_arm.yaw = (AnimationAPI.easeInOutSine(0, t * 0.4f, -12.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, 20f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.56f, -7.5f / j, sniffTime)
            );
            this.left_arm.roll = (AnimationAPI.easeInOutSine(0, t * 0.4f, -25f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, 7f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.56f, 18f / j, sniffTime)
            );

            /* Right Arm */
            this.right_arm.pitch = (AnimationAPI.easeInOutSine(0, t * 0.4f, -10f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, 10f / j, sniffTime)
            );
            this.right_arm.yaw = (AnimationAPI.easeInOutSine(0, t * 0.4f, 12.5f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, -25f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.56f, 12.5f / j, sniffTime)
            );
            this.right_arm.roll = (AnimationAPI.easeInOutSine(0, t * 0.4f, 25f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 0.4f, t * 2.08f, 0f / j, sniffTime) +
                    AnimationAPI.easeInOutSine(t * 2.08f, t * 2.56f, -25f / j, sniffTime)
            );

        } else {
            /* WALK/IDLE */
            if (r > 0) {
                if (r == 10) {
                    entity.setRoarAnimationProgress(animationProgress);
                } else {
                    if (emergeticksleft == 0 && sniffticks >= 45) {
                        double b = animationProgress - entity.getRoarAnimationProgress();
                        /* Head */
                        this.head.pitch = headPitch * 0.017453292F - (float) MathAddon.cutSin(limbAngle * 0.6662F, 0, false) * 0.7F * limbDistance / 2;
                        this.head.yaw = headYaw * 0.017453292F - (-MathHelper.sin(limbAngle * 0.6662F + 3.1415927F)) * 0.7F * limbDistance / 2;
                        this.head.roll = -MathHelper.sin(limbAngle * 0.6662F + 3.1415927F) * 0.7F * limbDistance / 2;

                        /* Ears */
                        this.left_ear.yaw = MathHelper.sin(animationProgress / 20 * MathHelper.sin(limbAngle * 0.6662F) * 1.4F * limbDistance) / 5 + MathHelper.sin(animationProgress / 20) / 5;
                        this.right_ear.yaw = -MathHelper.sin(animationProgress / 20 * MathHelper.sin(limbAngle * 0.6662F) * 1.4F * limbDistance) / 5 - MathHelper.sin(animationProgress / 20) / 5;

                        /* Body */
                        this.body.pitch = -MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance / 2 + MathHelper.cos(animationProgress / 20) / 20;
                        this.body.roll = MathHelper.cos(limbAngle * 0.6662F) * 0.7F * limbDistance / 4 + MathHelper.cos(animationProgress / 20) / 20;

                        /* Right Arm */
                        this.right_arm.pitch = -MathHelper.cos((limbAngle * 0.6662F) - 0.5F) * 1.4F * limbDistance / 2 - MathHelper.cos(animationProgress / 20) / 20;
                        this.right_arm.roll = (-MathHelper.sin(limbAngle * 0.6662F) * 0.7F * limbDistance / 4 + (-MathHelper.sin(animationProgress / 20) / 20)) + 0.05F;

                        /* Left Arm */
                        this.left_arm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance / 2 + MathHelper.cos(animationProgress / 20) / 20;
                        this.left_arm.roll = (-MathHelper.sin((limbAngle * 0.6662F) - 0.5F) * 0.7F * limbDistance / 4 + (-MathHelper.sin(animationProgress / 20) / 20)) - 0.05F;

                        /* Right Leg */
                        this.right_leg.pitch = MathHelper.clamp(MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance, -35, 35);

                        /* Left Leg */
                        this.left_leg.pitch = MathHelper.clamp(MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance, -35, 35);

                    }
                }
            }

            /* ATTACK ANIMATION */

            if (emergeticksleft <= 0 && sniffticks > 44) {
                //Attack Animation Handler
                int a = entity.getAttackTicksLeft1();

                /* Head */
                this.head.pitch = headPitch * 0.017453292F - (float) MathAddon.cutSin(limbAngle * 0.6662F, 0, false) * 0.7F * limbDistance / 2;
                this.head.yaw = headYaw * 0.017453292F - (-MathHelper.sin(limbAngle * 0.6662F + 3.1415927F)) * 0.7F * limbDistance / 2;
                this.head.roll = -MathHelper.sin(limbAngle * 0.6662F + 3.1415927F) * 0.7F * limbDistance / 2;

                /* Ears */
                this.left_ear.yaw = MathHelper.sin(animationProgress / 20 * MathHelper.sin(limbAngle * 0.6662F) * 1.4F * limbDistance) / 5 + MathHelper.sin(animationProgress / 20) / 5;
                this.right_ear.yaw = -MathHelper.sin(animationProgress / 20 * MathHelper.sin(limbAngle * 0.6662F) * 1.4F * limbDistance) / 5 - MathHelper.sin(animationProgress / 20) / 5;

                /* Body */
                this.body.pitch = -MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance / 2 + MathHelper.cos(animationProgress / 20) / 20;
                this.body.roll = MathHelper.cos(limbAngle * 0.6662F) * 0.7F * limbDistance / 4 + MathHelper.cos(animationProgress / 20) / 20;

                /* Right Arm */
                this.right_arm.pitch = -MathHelper.cos((limbAngle * 0.6662F) - 0.5F) * 1.4F * limbDistance / 2 - MathHelper.cos((animationProgress / 20)) / 20f - (a / 5f);
                this.right_arm.roll = (-MathHelper.sin(limbAngle * 0.6662F) * 0.7F * limbDistance / 4 + (-MathHelper.sin(animationProgress / 20) / 20)) + 0.05F;

                /* Left Arm */
                this.left_arm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance / 2 + MathHelper.cos(animationProgress / 20) / 20;
                this.left_arm.roll = (-MathHelper.sin((limbAngle * 0.6662F) - 0.5F) * 0.7F * limbDistance / 4 + (-MathHelper.sin(animationProgress / 20) / 20)) - 0.05F;

                /* Right Leg */
                this.right_leg.pitch = MathHelper.clamp(MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance, -35, 35);

                /* Left Leg */
                this.left_leg.pitch = MathHelper.clamp(MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance, -35, 35);
            }
        }
    }


    @Override
    public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        body.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
