package frozenblock.wild.mod.entity;

import frozenblock.wild.mod.liukrastapi.AnimationAPI;
import frozenblock.wild.mod.liukrastapi.MathAddon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class FrogEntityModel extends EntityModel<FrogEntity> {
    private final ModelPart root;
    private final ModelPart main;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart croaking_body;
    private final ModelPart tongue;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    
    private double Animationtime;
    private FrogEntity c;
    private boolean croak = true;
    private double croakstartTime;
    private float togueBegin;

    public FrogEntityModel(ModelPart root) {
        this.root = root.getChild("root");
        this.right_leg = this.root.getChild("right_leg");
        this.left_leg = this.root.getChild("left_leg");
        this.main = this.root.getChild("main");
        this.body = this.main.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.tongue = this.body.getChild("tongue");
        this.croaking_body = this.body.getChild("croaking_body");
        this.head = this.body.getChild("head");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();


        ModelPartData modelPartData1 = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F,24.0F,0.0F));
        ModelPartData modelPartData2 = modelPartData1.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(-3.0F,-2.0F,4.0F));
        ModelPartData modelPartData3 = modelPartData2.addChild("body", ModelPartBuilder.create().uv(3,1).cuboid(-6.5F, -2.0F, -8.0F, 7.0F, 3.0F, 9.0F).uv(23,22).cuboid(-6.5F, -1.0F, -8.0F, 7.0F, 0.0F, 9.0F), ModelTransform.pivot(6.0F,0.0F,0.0F));
        ModelPartData modelPartData4 = modelPartData3.addChild("head", ModelPartBuilder.create().uv(23,13).cuboid(-3.5F, -1.0F, -7.0F, 7.0F, 0.0F, 9.0F).uv(0,13).cuboid(-3.5F, -2.0F, -7.0F, 7.0F, 3.0F, 9.0F), ModelTransform.pivot(-3.0F,-2.0F,-1.0F));
        ModelPartData modelPartData5 = modelPartData4.addChild("eyes", ModelPartBuilder.create(), ModelTransform.pivot(-0.5F,0.0F,2.0F));
        modelPartData5.addChild("right_eye", ModelPartBuilder.create().uv(0,0).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(-1.5F,-3.0F,-6.5F));
        modelPartData5.addChild("left_eye", ModelPartBuilder.create().uv(0,5).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(2.5F,-3.0F,-6.5F));
        modelPartData3.addChild("croaking_body", ModelPartBuilder.create().uv(26, 5).cuboid(3.5f, 1.1f, 3.9f,7, 2, 3, new Dilation(-0.1f)), ModelTransform.pivot(-10F,-3F,-8F));
        modelPartData3.addChild("tongue", ModelPartBuilder.create().uv(17,13).cuboid(-2.0F, 0.0F, -7.1F, 4.0F, 0.0F, 7.0F), ModelTransform.pivot(-3.0F,-1.1F,1.0F));
        modelPartData3.addChild("left_arm", ModelPartBuilder.create().uv(0,32).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F).uv(18,40).cuboid(-4.0F, 3.01F, -5.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(1.0F,-1.0F,-6.5F));
        modelPartData3.addChild("right_arm", ModelPartBuilder.create().uv(0,38).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F).uv(2,40).cuboid(-4.0F, 3.01F, -5.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(-7.0F,-1.0F,-6.5F));
        modelPartData1.addChild("left_leg", ModelPartBuilder.create().uv(14,25).cuboid(-1.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F).uv(2,32).cuboid(-2.0F, 3.01F, -4.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(3.5F,-3.0F,4.0F));
        modelPartData1.addChild("right_leg", ModelPartBuilder.create().uv(0,25).cuboid(-2.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F).uv(18,32).cuboid(-6.0F, 3.01F, -4.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(-3.5F,-3.0F,4.0F));
        return TexturedModelData.of(modelData,48,48);

    }
    @Override
    public void setAngles(@NotNull FrogEntity entity, float limbAngle, float limbDistance, float time, float netHeadYaw, float headPitch){
        //this.main.pivotY = - 2 + AnimationAPI.easeInOutSine(100, 160, 10, time) + AnimationAPI.easeInOutSine(160, 220, 10, time);

        if(entity.getTongue() == 10) {
            this.togueBegin = 100;
        } else if(this.togueBegin > 0) {
            this.togueBegin = this.togueBegin - 0.6f;
        }
        this.tongue.pivotZ = -this.togueBegin/15;
        this.head.pitch = -this.togueBegin/100;

        c = entity;
        this.Animationtime = time;
        float animationspeed = 2F;
        float defaultmultiplier = 0.7F * limbDistance;

        if(!entity.isSubmergedInWater()) {
            if(entity.isOnGround()) {
                float rightanimation = (float) MathAddon.cutCos(limbAngle * animationspeed, 0, false) * defaultmultiplier;
                float leftanimation = (float) MathAddon.cutCos(limbAngle * animationspeed, 0, true) * defaultmultiplier;

                this.main.roll = -2 * rightanimation;
                this.main.pitch = -rightanimation;

                this.right_arm.roll = 2 * rightanimation;

                this.body.roll = leftanimation;
                this.body.pitch = -leftanimation;
                this.left_arm.roll = -leftanimation;

                this.right_arm.pitch = -9 * leftanimation;
                this.left_arm.pitch = -9 * rightanimation;

                this.right_leg.pitch = 9 * rightanimation;
                this.left_leg.pitch = 9 * leftanimation;

                float translationanimation1 = (float) MathAddon.cutSin(limbAngle * animationspeed, 0, true) * defaultmultiplier;
                float translationanimation2 = (float) MathAddon.cutSin(limbAngle * animationspeed, 0, false) * defaultmultiplier;

                if (limbDistance < 0.13) {
                    this.right_arm.pivotZ = -6.5f - 30 * translationanimation1;
                    this.left_arm.pivotZ = -6.5f - 30 * translationanimation2;

                    this.right_leg.pivotZ = 4 + 30 * translationanimation2;
                    this.left_leg.pivotZ = 4 + 30 * translationanimation1;
                } else {
                    this.right_arm.pivotZ = -6.5f;
                    this.left_arm.pivotZ = -6.5f;

                    this.right_leg.pivotZ = 4;
                    this.left_leg.pivotZ = 4;
                }
            } else {
                this.main.roll = 0;
                this.main.pitch = 0;
                this.right_arm.roll = 0;

                this.body.roll = 0;
                this.body.pitch = 0;
                this.left_arm.roll = 0;

                this.right_arm.pitch = -(float)Math.PI/2;
                this.left_arm.pitch = -(float)Math.PI/2;

                this.right_leg.pitch = (float)Math.PI/2;
                this.left_leg.pitch = (float)Math.PI/2;

                this.right_arm.pivotZ = -6.5f;
                this.left_arm.pivotZ = -6.5f;

                this.right_leg.pivotZ = 4;
                this.left_leg.pivotZ = 4;
            }
        } else {
            this.main.roll = 0;
            this.main.pitch = 0;
            this.body.roll = 0;
            this.body.pitch = 0;

            this.left_arm.roll = MathHelper.cos(limbAngle * animationspeed) * defaultmultiplier;
            this.right_arm.roll = -MathHelper.cos(limbAngle * animationspeed) * defaultmultiplier;

            float cosAnim = (float) MathAddon.cutCos(limbAngle * animationspeed, 0, true) * defaultmultiplier;
            float regAnim = (float) MathAddon.cutCos(limbAngle * animationspeed, 0, true) * (float)(Math.PI/2);

            this.right_arm.pitch = regAnim;
            this.left_arm.pitch = regAnim;

            this.left_leg.pitch = regAnim;
            this.right_leg.pitch = regAnim;

            if (limbDistance < 0.13) {
                this.right_arm.pivotZ = -6.5f - 30 * cosAnim;
                this.left_arm.pivotZ = -6.5f - 30 * cosAnim;

                this.right_leg.pivotZ = 4 + 30 * cosAnim;
                this.left_leg.pivotZ = 4 + 30 * cosAnim;
            } else {
                this.right_arm.pivotZ = -6.5f;
                this.left_arm.pivotZ = -6.5f;

                this.right_leg.pivotZ = 4;
                this.left_leg.pivotZ = 4;
            }

        }
    }
    @Override
    public void render(MatrixStack matrixStack, VertexConsumer	buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        root.render(matrixStack, buffer, packedLight, packedOverlay);
        float animation;

        if(this.croak) {
            if(Math.random() < 0.005) {
                this.croak = false;
                this.croakstartTime = this.Animationtime;
            }
        } else {
            double time = this.Animationtime - this.croakstartTime;
            animation = (float) MathAddon.cutSin(time/10, 0, false);
            matrixStack.translate(animation/4, 1.33+animation/6, animation/4-0.05);
            if(this.c.isOnGround()) {
                matrixStack.scale(1.3f*animation, 2*animation, 2*animation);
                this.croaking_body.render(matrixStack, buffer, packedLight, packedOverlay);
            } else {
                matrixStack.scale(0, 0, 0);
            }
            if(time > 0.1) {
                if(animation == 0) {
                    this.croak = true;
                }
            }
        }
    }
}