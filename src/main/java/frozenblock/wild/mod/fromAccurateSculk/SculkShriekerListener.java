/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SculkShriekerListener
implements GameEventListener {
    protected final SculkSensorListener.Callback callback;
    protected final PositionSource positionSource;
    protected final int range;
    protected Optional<GameEvent> event = Optional.empty();
    protected int distance;
    protected int delay = 0;

    public SculkShriekerListener(PositionSource positionSource, int i, SculkSensorListener.Callback callback) {
        this.positionSource = positionSource;
        this.range = i;
        this.callback =  callback;
    }

    public void tick(World world) {
        if (this.event.isPresent()) {
            --this.delay;
            if (this.delay <= 0) {
                this.delay = 0;
                this.callback.accept(world, this, this.event.get(), this.distance);
                this.event = Optional.empty();
            }
        }
    }

    @Override
    public PositionSource getPositionSource() {
        return this.positionSource;
    }

    @Override
    public int getRange() {
        return this.range;
    }

    @Override
    public boolean listen(World world, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
        if (!this.shouldActivate(gameEvent)) {
            return false;
        }
        Optional<BlockPos> optional = this.positionSource.getPos(world);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos blockPos2 = optional.get();
        if (ShriekCounter.findWarden(world, blockPos2)) {
            return false;
        }
        if (!this.callback.accepts(world, this, blockPos, gameEvent, entity)) {
            return false;
        }
        if (this.isOccluded(world, blockPos, blockPos2)) {
            return false;
        }
        this.listen(world, gameEvent, blockPos, blockPos2);
        return true;
    }

    private boolean shouldActivate(GameEvent gameEvent) {
        if (gameEvent.equals(RegisterAccurateSculk.CLICK)) { return true; }
        return false;
    }

    private void listen(World world, GameEvent gameEvent, BlockPos blockPos, BlockPos blockPos2) {
        this.event = Optional.of(gameEvent);
        if (world instanceof ServerWorld) {
            if (gameEvent==RegisterAccurateSculk.CLICK) {
                this.delay = this.distance = MathHelper.floor(Math.sqrt(blockPos.getSquaredDistance(blockPos2, false))) * 2;
                ((ServerWorld) world).sendVibrationPacket(new Vibration(blockPos, this.positionSource, this.delay));
                BlockEntity sensor = world.getBlockEntity(blockPos);
                if (sensor instanceof SculkSensorBlockEntity) {
                    SculkSensorBlockEntity sculkSensorBlockEntity = (SculkSensorBlockEntity)sensor;
                    writeValue(sculkSensorBlockEntity.getLastVibrationFrequency(), blockPos2, world);
                }
            }
        }
    }

    public void writeValue(int i, BlockPos blockPos, World world) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof SculkShriekerBlockEntity shriekerBlockEntity) {
            shriekerBlockEntity.setLastVibrationFrequency(i);
        }
    }

    private boolean isOccluded(World world, BlockPos blockPos, BlockPos blockPos2) {
        return world.raycast(new BlockStateRaycastContext(Vec3d.ofCenter(blockPos), Vec3d.ofCenter(blockPos2), blockState -> blockState.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType() == HitResult.Type.BLOCK;
    }

    public static interface Callback {
        public boolean accepts(World var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5);

        public void accept(World var1, GameEventListener var2, GameEvent var3, int var4);
    }
}

