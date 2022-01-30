package frozenblock.wild.mod.mixins;

import frozenblock.wild.mod.fromAccurateSculk.SensorLastEntity;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SculkSensorListener.class)
public class SculkSensorListenerMixin {

    @Inject(method = "listen", at = @At("TAIL"))
    public boolean listen(World world, GameEvent event, @Nullable Entity entity, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        SculkSensorListener listener = SculkSensorListener.class.cast(this);
        Optional<BlockPos> optional = listener.getPositionSource().getPos(world);
        if (optional.isPresent() && entity!=null && SculkSensorBlock.FREQUENCIES.containsKey(event)) {
            BlockPos thisPos = (BlockPos) optional.get();
            if (entity != null) {
                if (entity instanceof LivingEntity) {
                    SensorLastEntity.addEntity(entity, thisPos, pos, event);
                }
            }
        }
        return info.getReturnValue();
    }
}
