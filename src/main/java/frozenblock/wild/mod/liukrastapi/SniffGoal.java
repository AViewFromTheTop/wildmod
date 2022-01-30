package frozenblock.wild.mod.liukrastapi;

import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.registry.RegisterEntities;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.List;


public class SniffGoal extends Goal {

    private final WardenEntity mob;
    private final double speed;

    public SniffGoal(WardenEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
    }


    public boolean canStart() {
        boolean exit = false;
        LivingEntity sniffEntity = null;
        if (this.mob.mostSuspiciousAround()!=null) {
            sniffEntity=this.mob.mostSuspiciousAround();
        }
        if (sniffEntity==null) {
            LivingEntity closestPlayer = this.mob.getWorld().getClosestPlayer(this.mob, 16);
            sniffEntity=closestPlayer;
            if (closestPlayer==null) {
                Box box = new Box(this.mob.getBlockPos().add(-16,-16,-16), this.mob.getBlockPos().add(16,16,16));
                List<LivingEntity> entities = this.mob.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
                LivingEntity chosen = this.mob.getWorld().getClosestEntity(entities, TargetPredicate.DEFAULT, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
                if (chosen!=null && MathAddon.distance(chosen.getX(), chosen.getY(), chosen.getZ(), this.mob.getX(), this.mob.getY(), this.mob.getZ())<=16 && chosen.getType()!=RegisterEntities.WARDEN) {
                    sniffEntity=chosen;
                }
            }
        }
        if (this.mob.emergeTicksLeft>0) {
            return false;
        }
        if (this.mob.getWorld().getTime()-this.mob.vibrationTimer>160 && sniffEntity!=null && this.mob.sniffCooldown<=0) {
            exit = true;
        }

        int r = this.mob.getRoarTicksLeft1();
        if (r > 0) {
            exit = false;
        }
        if (this.mob.world.getDifficulty().getId()==0) { return false; }
        if (exit && sniffEntity instanceof PlayerEntity) {
            exit = !((PlayerEntity)sniffEntity).getAbilities().creativeMode;
        }
        return exit;
    }

    public boolean shouldContinue() {
        return false;
    }

    public void start() {
        LivingEntity sniffEntity = null;
        if (this.mob.mostSuspiciousAround()!=null) {
            sniffEntity=this.mob.mostSuspiciousAround();
        }
        if (sniffEntity==null) {
            LivingEntity closestPlayer = this.mob.getWorld().getClosestPlayer(this.mob, 16);
            sniffEntity=closestPlayer;
            if (closestPlayer==null) {
                Box box = new Box(this.mob.getBlockPos().add(-16,-16,-16), this.mob.getBlockPos().add(16,16,16));
                List<LivingEntity> entities = this.mob.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
                LivingEntity chosen = this.mob.getWorld().getClosestEntity(entities, TargetPredicate.DEFAULT, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
                if (chosen!=null && MathAddon.distance(chosen.getX(), chosen.getY(), chosen.getZ(), this.mob.getX(), this.mob.getY(), this.mob.getZ())<=16) {
                    sniffEntity=chosen;
                }
            }
        }

        if (sniffEntity!=null) {
            if (MathAddon.distance(sniffEntity.getX(), sniffEntity.getY(), sniffEntity.getZ(), this.mob.getX(), this.mob.getY(), this.mob.getZ()) <= 16) {
                this.mob.sniffTicksLeft = 44;
                this.mob.sniffCooldown = 144 + UniformIntProvider.create(-20,80).get(this.mob.getRandom());
                this.mob.sniffX = sniffEntity.getBlockPos().getX();
                this.mob.sniffY = sniffEntity.getBlockPos().getY();
                this.mob.sniffZ = sniffEntity.getBlockPos().getZ();
                this.mob.sniffEntity=sniffEntity.getUuidAsString();
                this.mob.getWorld().playSound(null, this.mob.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SNIFF, SoundCategory.HOSTILE, 1F, 1F);
                this.mob.leaveTime = this.mob.getWorld().getTime() + 1200;
            }
        }
    }

    public void stop() {
    }
}
