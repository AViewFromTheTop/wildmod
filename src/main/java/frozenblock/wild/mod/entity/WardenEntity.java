package frozenblock.wild.mod.entity;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.liukrastapi.MathAddon;
import frozenblock.wild.mod.liukrastapi.SniffGoal;
import frozenblock.wild.mod.liukrastapi.WardenGoal;
import frozenblock.wild.mod.liukrastapi.WardenWanderGoal;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import frozenblock.wild.mod.registry.RegisterSounds;
import frozenblock.wild.mod.registry.RegisterStatusEffects;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WardenEntity extends HostileEntity {
    /** WELCOME TO THE WARDEN MUSEUM
     * <p>
     * ALL THESE WILL LINK TO THE FIRST METHOD IN THEIR GIVEN SECTIONS
     * <li> SUSPICION {@link WardenEntity#addSuspicion(LivingEntity, int)}
     * <p>
     * <li> SNIFFING {@link WardenEntity#getSniffEntity()}
     * <p>
     * <li> ATTACKING & ROARING {@link WardenEntity#setRoarAnimationProgress(double)}
     * <p>
     * <li> NBT, VALUES & BOOLEANS {@link WardenEntity#writeCustomDataToNbt(NbtCompound)}
     * <p>
     * <li> OVERRIDES & NON-WARDEN-SPECIFIC {@link WardenEntity#getHurtSound(DamageSource)}
     * <p>
     * <li> VISUAlS {@link WardenEntity#CreateVibration(World, WardenEntity, BlockPos)}
     * <p>
     * <li> TICKMOVEMENT METHODS {@link WardenEntity#tickEmerge()}
     * </ul><p>
     * ALL VALUES ARE STORED AT THE END OF THIS MUSEUM.
     * */

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new WardenGoal(this, speed));
        this.goalSelector.add(2, new SniffGoal(this, speed));
        this.goalSelector.add(1, new WardenWanderGoal(this, 0.4));
    }

    public void tickMovement() {
        this.tickBurn();
        if (!this.isAiDisabled()) {
            if (this.attackTicksLeft1 > 0) { --this.attackTicksLeft1; }
            if (this.roarTicksLeft1 > 0) { --this.roarTicksLeft1; }
            if (this.attackCooldown > 0) { --this.attackCooldown; }
            this.tickEmerge();
            this.tickStuck();
            this.tickSniff();
            if (this.ticksToDarkness > 0) { --this.ticksToDarkness; }
            if (this.ticksToDarkness==0) {
                this.sendDarkness(48, this.getBlockPos(), this.world);
                this.ticksToDarkness=100;
            }
        }
        //Heartbeat & Anger
        this.heartbeatTime = (int) (60 - ((MathHelper.clamp(this.trueOverallAnger(),0,45)*1.1)));
        if (this.world.getTime()>=this.nextHeartBeat) {
            this.world.playSound(null, this.getBlockPos().up(), RegisterSounds.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1F, (float) (0.85F + (MathHelper.clamp(this.trueOverallAnger(),0,45)*0.0066)));
            this.nextHeartBeat=this.world.getTime()+heartbeatTime;
            this.lastHeartBeat=this.world.getTime();
            this.world.sendEntityStatus(this, (byte)8);
        }
        if (this.world.getTime()-this.timeSinceNonEntity>300 && this.nonEntityAnger>0) { --this.nonEntityAnger; }
        super.tickMovement();
    }

    public void handleStatus(byte status) {
        if (!this.isAiDisabled() && status == 4) { //Set Attack Ticks
            this.attackTicksLeft1 = 10;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_AMBIENT, SoundCategory.HOSTILE, 1.0F,1.0F);
        } else if(!this.isAiDisabled() && status == 3) { //Set Roar Ticks
            this.roarTicksLeft1 = 10;
        } else if(!this.isAiDisabled() && status == 5) { //Emerging
            this.emergeTicksLeft=160;
            this.hasEmerged=false;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
        } else if(!this.isAiDisabled() && status == 6) { //Digging Back
            this.emergeTicksLeft=60;
            this.hasEmerged=true;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_DIG, SoundCategory.HOSTILE, 1F, 1F);
        } else if (!this.isAiDisabled() && status == 7) { //Set Last Vibration Time
            this.vibrationTimer=this.world.getTime();
        } else if (!this.isAiDisabled() && status == 8) { //Set Last Client Beat Time
            this.lastClientHeartBeat=this.world.getTime();
        } else if (!this.isAiDisabled() && status == 9) { //Set Client Emerge Ticks
            this.clientEmergeTicks=160;
        } else if (!this.isAiDisabled() && status == 10) { //Set Client Dig Ticks
            this.clientDigTicks=60;
        } else if (!this.isAiDisabled() && status == 11) { //Set Client isEmerging
            this.isEmerging=true;
        } else if (!this.isAiDisabled() && status == 12) { //Set Client isEmerging To False
            this.isEmerging=false;
        } else if (!this.isAiDisabled() && status == 13) { //Set Client isDigging
            this.isDigging=true;
        } else if (!this.isAiDisabled() && status == 14) { //Set Client isDigging To False
            this.isDigging=false;
        } else if (!this.isAiDisabled() && status == 15) { //Set Client emergeStart
            this.emergeStart=this.world.getTime();
        } else if (!this.isAiDisabled() && status == 16) { //Set Client emergeStop
            this.emergeStop=this.world.getTime()+160;
        } else if (!this.isAiDisabled() && status == 17) { //Set Client digStart
            this.digStart=this.world.getTime();
        } else if (!this.isAiDisabled() && status == 18) { //Set Client digStop
            this.digStop=this.world.getTime()+60;
        } else if (!this.isAiDisabled() && status == 19) { //Subtract Client Emerge Ticks
            if (this.clientEmergeTicks>0) { this.clientEmergeTicks=this.clientEmergeTicks-1; }
        } else if (!this.isAiDisabled() && status == 22) { //Subtract Client Dig Ticks
            if (this.clientDigTicks>0) { this.clientDigTicks=this.clientDigTicks-1; }
        } else { super.handleStatus(status); }
    }

    public void listen(BlockPos eventPos, World eventWorld, LivingEntity eventEntity, int suspicion, BlockPos vibrationPos) {
        boolean shouldListen = true;
        if (eventEntity instanceof PlayerEntity) { shouldListen = !((PlayerEntity)eventEntity).getAbilities().creativeMode; }
        if (!this.isAiDisabled() && shouldListen && !(this.emergeTicksLeft > 0) && this.world.getTime() - this.vibrationTimer >= 23) {
            this.sniffTicksLeft=-1;
            this.lasteventpos = eventPos;
            this.lasteventworld = eventWorld;
            this.lastevententity = eventEntity;
            this.hasDetected = true;
            this.leaveTime = this.world.getTime() + 1200;
            this.world.sendEntityStatus(this, (byte)7);
            this.vibrationTimer=this.world.getTime();
            this.world.playSound(null, this.getBlockPos().up(2), RegisterSounds.ENTITY_WARDEN_VIBRATION, SoundCategory.HOSTILE, 0.5F, world.random.nextFloat() * (this.trueOverallAnger()/120F) + 0.9F);
            if (vibrationPos != null) { CreateVibration(this.world, this, vibrationPos); }
            else { CreateVibration(this.world, this, lasteventpos); }
            if (eventEntity != null) {
                addSuspicion(eventEntity, suspicion);
                if (this.world.getTime()-reactionSoundTimer>40) { this.reactionSoundTimer=this.world.getTime();
                    if (getSuspicion(eventEntity)<13 && getSuspicion(eventEntity)>10) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (getSuspicion(eventEntity)<25) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (this.trueOverallAnger()<40) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (this.trueOverallAnger()>=41) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    }
                }
            } else { this.timeSinceNonEntity = this.world.getTime();
                this.nonEntityAnger=this.nonEntityAnger+3;
                if (this.world.getTime()-reactionSoundTimer>40) {
                    this.reactionSoundTimer = this.world.getTime();
                    if (this.trueOverallAnger()<25) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    }
                }
            }
        }
    }

    /** SUSPICION */
    public void addSuspicion(LivingEntity entity, int suspicion) {
        if (this.world.getDifficulty().getId() != 0) {
            if (!this.entityList.isEmpty()) {
                if (this.entityList.contains(entity.getUuid().hashCode())) {
                    int slot = this.entityList.indexOf(entity.getUuid().hashCode());
                    this.susList.set(slot, this.susList.getInt(slot) + suspicion);
                    if (this.susList.getInt(slot) >= 45 && this.getTrackingEntity() == null) {
                        this.trackingEntity = entity.getUuidAsString();
                        this.world.playSound(null, this.getBlockPos().up(), RegisterSounds.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 1F, 1F);
                        this.roar();
                    }
                } else { this.entityList.add(entity.getUuid().hashCode());
                    this.susList.add(suspicion);
                }
            } else { this.entityList.add(entity.getUuid().hashCode());
                this.susList.add(suspicion);
            }
        }
    }
    public int getSuspicion(Entity entity) {
        if (!this.entityList.isEmpty() && entity!=null) {
            if (this.entityList.contains(entity.getUuid().hashCode())) {
                return this.susList.getInt(this.entityList.indexOf(entity.getUuid().hashCode()));
            }
        } return 0;
    }
    public int eventSuspicionValue(GameEvent event, LivingEntity livingEntity) {
        int total=1;
        if (event==GameEvent.PROJECTILE_LAND) { return 0; }
        if (SculkSensorBlock.FREQUENCIES.containsKey(event)) { total=total + SculkSensorBlock.FREQUENCIES.getInt(event); }
        if (livingEntity instanceof PlayerEntity) {
        return MathHelper.clamp(total, 3,15);
        }
        return MathHelper.clamp(total, 2,15);
    }
    public int trueOverallAnger() {
        int anger=0;
        if (this.world.getDifficulty().getId()!=0) {
            Box box = new Box(this.getBlockPos().add(-24, -24, -24), this.getBlockPos().add(24, 24, 24));
            List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, box);
            if (!entities.isEmpty()) {
                for (LivingEntity target : entities) {anger = anger + this.getSuspicion(target); }
            }
            anger = anger + nonEntityAnger;
            anger = MathHelper.clamp(anger, 0, 50);
        } return anger;
    }
    public LivingEntity getTrackingEntity() {
        Box box = new Box(this.getBlockPos().add(-18,-18,-18), this.getBlockPos().add(18,18,18));
        List<LivingEntity> entities = this.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                    if (Objects.equals(this.trackingEntity, target.getUuidAsString()) && MathAddon.distance(target.getX(), target.getY(), target.getZ(), this.getX(), this.getY(), this.getZ()) <= 16) { return target; }
            }
        } return null;
    }
    public LivingEntity mostSuspiciousAround() {
        int highest = 0;
        LivingEntity most = null;
        Box box = new Box(this.getBlockPos().add(-16,-16,-16), this.getBlockPos().add(16,16,16));
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (this.getBlockPos().getSquaredDistance(target.getBlockPos())<=16 && this.getSuspicion(target)>highest) {
                    highest = this.getSuspicion(target);
                    most = target;
                }
            }
        } return most;
    }

    /** SNIFFING */
    public LivingEntity getSniffEntity() {
        Box box = new Box(this.getBlockPos().add(-18,-18,-18), this.getBlockPos().add(18,18,18));
        List<LivingEntity> entities = this.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (Objects.equals(this.sniffEntity, target.getUuidAsString()) && MathAddon.distance(target.getX(), target.getY(), target.getZ(), this.getX(), this.getY(), this.getZ()) <= 16) { return target; }
            }
        } return null;
    }
    /** ATTACKING & ROARING */
    public void setRoarAnimationProgress(double a) { this.roarAnimationProgress = a; }
    public void roar() {
        this.attackTicksLeft1 = 10;
        this.world.sendEntityStatus(this, (byte)3);
    }

    public boolean tryAttack(Entity target) {
        if (this.world.getDifficulty().getId()!=0) {
            boolean bl = target.damage(DamageSource.mob(this), this.getAttackDamage());
            if (bl && this.attackCooldown <= 0) {
                this.attackTicksLeft1 = 10;
                this.world.sendEntityStatus(this, (byte) 4);
                target.setVelocity(target.getVelocity().add(0.0D, 0.4000000059604645D, 0.0D));
                this.applyDamageEffects(this, target);
                world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_ATTACK, SoundCategory.HOSTILE, 1.0F, 1.0F);
                this.attackCooldown = 35;
            } return bl;
        } return false;
    }

    /** NBT, VALUES & BOOLEANS */
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putLong("vibrationTimer", this.vibrationTimer);
        nbt.putLong("leaveTime", this.leaveTime);
        nbt.putInt("emergeTicksLeft", this.emergeTicksLeft);
        nbt.putBoolean("hasEmerged", this.hasEmerged);
        nbt.putBoolean("hasSentStatusStart", this.hasSentStatusStart);
        nbt.putIntArray("entityList", this.entityList);
        nbt.putIntArray("susList", this.susList);
        nbt.putString("trackingEntity", this.trackingEntity);
        nbt.putInt("sniffTicksLeft", this.sniffTicksLeft);
        nbt.putInt("sniffCooldown", this.sniffCooldown);
        nbt.putInt("attackCooldown", this.attackCooldown);
        nbt.putInt("sniffX", this.sniffX);
        nbt.putInt("sniffY", this.sniffY);
        nbt.putInt("sniffZ", this.sniffZ);
        nbt.putString("sniffEntity", this.sniffEntity);
        nbt.putInt("nonEntityAnger", this.nonEntityAnger);
        nbt.putLong("timeSinceNonEntity", this.timeSinceNonEntity);
    }
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.vibrationTimer = nbt.getLong("vibrationTimer");
        this.leaveTime = nbt.getLong("leaveTime");
        this.emergeTicksLeft = nbt.getInt("emergeTicksLeft");
        this.hasEmerged = nbt.getBoolean("hasEmerged");
        this.hasSentStatusStart = nbt.getBoolean("hasSentStatusStart");
        this.entityList = IntArrayList.wrap(nbt.getIntArray("entityList"));
        this.susList = IntArrayList.wrap(nbt.getIntArray("susList"));
        this.trackingEntity = nbt.getString("trackingEntity");
        this.sniffTicksLeft = nbt.getInt("sniffTicksLeft");
        this.sniffCooldown = nbt.getInt("sniffCooldown");
        this.attackCooldown = nbt.getInt("attackCooldown");
        this.sniffX = nbt.getInt("sniffX");
        this.sniffY = nbt.getInt("sniffY");
        this.sniffZ = nbt.getInt("sniffZ");
        this.sniffEntity = nbt.getString("sniffEntity");
        this.nonEntityAnger = nbt.getInt("nonEntityAnger");
        this.timeSinceNonEntity = nbt.getLong("timeSinceNonEntity");
    }

    public float heartbeatTime() {return heartbeatTime;}
    public int getAttackTicksLeft1() {return this.attackTicksLeft1;}
    public double getRoarAnimationProgress() {return this.roarAnimationProgress;}
    public int getRoarTicksLeft1() {return this.roarTicksLeft1;}
    public int getEmergeTicksLeft() {return this.emergeTicksLeft;}
    public int getSniffTicksLeft() {return this.sniffTicksLeft;}
    public boolean getHasEmerged() {return this.hasEmerged;}

    @Deprecated
    public boolean canFollow(Entity entity, boolean mustBeTracking) {
        Box box = new Box(this.getBlockPos().add(-20,-20,-20), this.getBlockPos().add(20,20,20));
        List<Entity> entities = world.getNonSpectatingEntities(Entity.class, box);
        if (!entities.isEmpty() && entities.contains(entity)) {
            if (MathAddon.distance(entity.getX(), entity.getY(), entity.getZ(), this.getX(), this.getY(), this.getZ()) <= 18) {
                if (mustBeTracking) {
                    return entity == this.getTrackingEntity();
                } else {
                    return true;
                }
            }
        } return false;
    }
    @Deprecated
    public int mostSuspiciousAroundInt() {
        int value=0;
        if (mostSuspiciousAround()!=null) {value=this.getSuspicion(mostSuspiciousAround());}
        return value;
    }
    @Deprecated
    public int getHighestSuspicionInt() {
        int highest = 0;
        if (!this.susList.isEmpty()) {
            for (int i=0; i<this.susList.size(); i++) {
                if (this.susList.getInt(i)>highest) {highest=this.susList.getInt(i);}
            }
        } return highest;
    }

    /** OVERRIDES & NON-WARDEN-SPECIFIC */
    protected SoundEvent getHurtSound(DamageSource source) {return RegisterSounds.ENTITY_WARDEN_HURT;}
    protected SoundEvent getStepSound() {return RegisterSounds.ENTITY_WARDEN_STEP;}
    protected void playStepSound(BlockPos pos, BlockState state) { this.playSound(this.getStepSound(), 1.0F, 1.0F); }
    protected SoundEvent getAmbientSound(){return RegisterSounds.ENTITY_WARDEN_AMBIENT;}
    protected SoundEvent getDeathSound() { return RegisterSounds.ENTITY_WARDEN_DEATH; }
    protected boolean isDisallowedInPeaceful() { return false; }
    @Override
    public void emitGameEvent(GameEvent event, @Nullable Entity entity, BlockPos pos) {}
    @Override
    public void emitGameEvent(GameEvent event, @Nullable Entity entity) {}
    @Override
    public void emitGameEvent(GameEvent event, BlockPos pos) {}
    @Override
    public void emitGameEvent(GameEvent event) {}

    public WardenEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.LAVA, 16.0F);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 8.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 8.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
        this.stepHeight = 1.0F;
    }

    private float getAttackDamage() { return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE); }
    public static DefaultAttributeContainer.Builder createWardenAttributes() {return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 31.0D);}
    protected boolean burnsInDaylight() { return true; }
    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.handleStatus((byte) 5);
        this.leaveTime=this.world.getTime()+1200;
        this.setPersistent();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    /** VISUALS */
    public void CreateVibration(World world, WardenEntity warden, BlockPos blockPos2) {
        EntityPositionSource wardenPositionSource = new EntityPositionSource(this.getId()) {
            @Override
            public Optional<BlockPos> getPos(World world) {
                return Optional.of(warden.getCameraBlockPos());
            }
            @Override
            public PositionSourceType<?> getType() {
                return PositionSourceType.ENTITY;
            }
        };
        this.delay = this.distance = (int)Math.floor(Math.sqrt(warden.getCameraBlockPos().getSquaredDistance(blockPos2, false))) * 2;
        ((ServerWorld)world).sendVibrationPacket(new Vibration(blockPos2, wardenPositionSource, this.delay));
    }
    public void digParticles(World world, BlockPos pos, int ticks) {
        if (world instanceof ServerWorld) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeInt(ticks);
            for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, pos, 32)) {
                ServerPlayNetworking.send(player, RegisterAccurateSculk.WARDEN_DIG_PARTICLES, buf);
            }
        }
    }

    /** TICKMOVEMENT METHODS */
    public void tickEmerge() {
        if (!this.hasSentStatusStart) {
            this.world.sendEntityStatus(this, (byte)9);
            this.hasSentStatusStart=true;
        }
        if (this.emergeTicksLeft > 0 && !this.hasEmerged) {
            this.setInvulnerable(true);
            this.setVelocity(0, 0, 0);
            this.world.sendEntityStatus(this, (byte)19);
            this.emergeTicksLeft--;
        }
        if (this.emergeTicksLeft == 0 && !this.hasEmerged) {
            this.setInvulnerable(false);
            this.hasEmerged = true;
            this.world.sendEntityStatus(this, (byte)12);
            this.emergeTicksLeft = -1;
        }
        if (this.emergeTicksLeft > 0 && this.hasEmerged) {
            digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            this.setInvulnerable(true);
            this.setVelocity(0, 0, 0);
            this.world.sendEntityStatus(this, (byte)22);
            --this.emergeTicksLeft;
        }
        if (this.emergeTicksLeft == 0 && this.hasEmerged) { this.remove(RemovalReason.DISCARDED); }
        if (world.getTime() == this.leaveTime) {
            this.world.sendEntityStatus(this, (byte)10);
            this.world.sendEntityStatus(this, (byte)13);
            this.handleStatus((byte) 6);

        }
    }
    public void sendDarkness(int dist, BlockPos blockPos, World world) {
        if (world instanceof ServerWorld) {
            if (world.getGameRules().getBoolean(WildMod.DARKNESS_ENABLED)) {
                Box box = (new Box(blockPos.add(-50, -50, -50), blockPos.add(50, 50, 50)));
                List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator<PlayerEntity> var11 = list.iterator();
                PlayerEntity playerEntity;
                while (var11.hasNext()) {
                    playerEntity = var11.next();
                    if (playerEntity.getBlockPos().isWithinDistance(blockPos, (dist + 1))) {
                        playerEntity.addStatusEffect(new StatusEffectInstance(RegisterStatusEffects.DARKNESS, 300, 0, true, false, false));
                    }
                }
            }
        }
    }
    public void tickSniff() {
        if (this.sniffTicksLeft > 0) { --this.sniffTicksLeft; }
        if (this.sniffCooldown > 0) { --this.sniffCooldown; }
        if (this.sniffTicksLeft == 0) {
            this.sniffTicksLeft = -1;
            if (this.getSniffEntity() != null) {
                LivingEntity sniffEntity = this.getSniffEntity();
                this.addSuspicion(sniffEntity, 5);
                if (sniffEntity != this.getTrackingEntity()) {
                    this.getNavigation().startMovingTo(sniffX, sniffY, sniffZ, (speed + (MathHelper.clamp(this.getSuspicion(sniffEntity), 0, 45) * 0.006) + (this.trueOverallAnger() * 0.002)));
                } else if (sniffEntity == this.getTrackingEntity()) {
                    this.getNavigation().startMovingTo(sniffX, sniffY, sniffZ, (speed + (MathHelper.clamp(this.getSuspicion(sniffEntity), 0, 45) * 0.013) + (this.trueOverallAnger() * 0.002)));
                }
            }
        }
    }
    public void tickStuck() {
        if (this.stuckPos != null && this.getBlockPos().getSquaredDistance(this.stuckPos) < 2 && this.hasEmerged && this.hasDetected && !(this.sniffTicksLeft > 0)) {
            this.timeStuck++;
        } else {
            this.timeStuck = 0;
            this.stuckPos = this.getBlockPos();
        }
        if (this.timeStuck >= 30 && this.hasEmerged && this.world.getTime() - this.vibrationTimer < 120 && this.world.getTime() - this.timeSinceLastRecalculation > 49 && !(this.sniffTicksLeft > 0)) {
            this.getNavigation().recalculatePath();
            this.timeSinceLastRecalculation = this.world.getTime();
        }
    }
    public void tickBurn() {
    if (this.isAlive()) {
        if (world.getGameRules().getBoolean(WildMod.WARDEN_BURNS)) {
            boolean bl = this.burnsInDaylight() && this.isAffectedByDaylight();
            if (bl) { ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageable()) {itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                        if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                            this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                            this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    } bl = false;
                }
                if (bl) { this.setOnFireFor(8); }
            }
        }
    }
    }

    //Animation
    private double roarAnimationProgress;
    //Movement
    public int timeStuck=0;
    public BlockPos stuckPos;
    public long timeSinceLastRecalculation;
    public BlockPos lasteventpos;
    public World lasteventworld;
    public LivingEntity lastevententity;
    public LivingEntity navigationEntity;
    public int sniffX;
    public int sniffY;
    public int sniffZ;
    //Lists & Entity Tracking
    public IntArrayList entityList = new IntArrayList();
    public IntArrayList susList = new IntArrayList();
    public String trackingEntity = "null";
    public String sniffEntity = "null";
    //Anger & Heartbeat
    public int heartbeatTime = 60;
    public int nonEntityAnger;
    public long nextHeartBeat;
    public long lastHeartBeat;
    //Emerging & Digging
    public boolean hasDetected=false;
    public boolean hasEmerged;
    public boolean hasSentStatusStart;
    public int emergeTicksLeft;
    //Timers
    public long leaveTime;
    public long vibrationTimer = 0;
    public int attackCooldown;
    public long reactionSoundTimer;
    private int attackTicksLeft1;
    private int roarTicksLeft1;
    public int sniffTicksLeft;
    public int ticksToDarkness;
    //Stopwatches
    public long timeSinceNonEntity;
    public int sniffCooldown;

    protected int delay = 0;
    protected int distance;
    private static final double speed = 0.4D;

    //CLIENT VARIABLES (Use world.sendEntityStatus() to set these, we need to make "fake" variables for the client to use since that method is buggy)
    public long lastClientHeartBeat; //Status 8
    public int clientEmergeTicks; //Set to 160: Status 9. Subtract: Status 19.
    public int clientDigTicks; //Set to 60: Status 10. Subtract: Status 22.
    public boolean isEmerging; //Set to true: Status 11. Set to false: Status 12.
    public boolean isDigging; //Set to true: Status 13. Set to false: Status 14.
    public long emergeStart; //Status 15
    public long digStart; //Status 16
    public long emergeStop; //Status 17
    public long digStop; //Status 18
}
