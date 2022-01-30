package frozenblock.wild.mod.mixins;

import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.fromAccurateSculk.SensorLastEntity;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {
    //FROM ACCURATE SCULK
    private ServerWorld world;

    public SculkSensorBlockMixin(ServerWorld world) {
        this.world = world;
    }

    @Inject(method = "setActive", at = @At("TAIL"))
    private static void setActive(World world, BlockPos pos, BlockState state, int power, CallbackInfo info) {
        world.emitGameEvent(RegisterAccurateSculk.CLICK, pos.add(0.5, 0, 0.5));

        int lastEntity = SensorLastEntity.getLastEntity(pos);
        LivingEntity target = (LivingEntity) world.getEntityById(lastEntity);
        BlockPos lastEventPos = SensorLastEntity.getLastPos(pos);
        GameEvent event = SensorLastEntity.getLastEvent(pos);
        if (event != null && lastEntity != -1 && lastEventPos != null && target != null) {
            if (SculkSensorBlock.FREQUENCIES.containsKey(event)) {
                Box box = new Box(pos.getX() - 18, pos.getY() - 18, pos.getZ() - 18, pos.getX() + 18, pos.getY() + 18, pos.getZ() + 18);
                List<WardenEntity> list = world.getNonSpectatingEntities(WardenEntity.class, box);
                Iterator<WardenEntity> var11 = list.iterator();
                WardenEntity wardenEntity;
                while (var11.hasNext()) {
                    wardenEntity = var11.next();
                    if (wardenEntity.getBlockPos().isWithinDistance(pos, 16)) {
                        if (event != GameEvent.PROJECTILE_LAND && event != GameEvent.BLOCK_DESTROY && event != GameEvent.BLOCK_PLACE && event != GameEvent.BLOCK_ATTACH
                                && event != GameEvent.BLOCK_CHANGE && event != GameEvent.BLOCK_CLOSE && event != GameEvent.BLOCK_OPEN && event != GameEvent.BLOCK_PRESS
                                && event != GameEvent.BLOCK_SWITCH && event != GameEvent.BLOCK_DETACH && event != GameEvent.BLOCK_UNPRESS && event != GameEvent.BLOCK_UNSWITCH
                                && event != GameEvent.RING_BELL) {
                            wardenEntity.listen(lastEventPos, wardenEntity.getWorld(), target, wardenEntity.eventSuspicionValue(event, target), pos);
                        } else {
                            wardenEntity.listen(lastEventPos, wardenEntity.getWorld(), null, 1, pos);
                        }
                    }
                }
            }
        }
    }
}
