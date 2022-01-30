package frozenblock.wild.mod.mixins;

import frozenblock.wild.mod.blocks.SculkShriekerBlock;
import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.fromAccurateSculk.SensorLastEntity;
import frozenblock.wild.mod.fromAccurateSculk.ShriekCounter;
import frozenblock.wild.mod.registry.RegisterBlocks;
import frozenblock.wild.mod.registry.RegisterEntities;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(at = @At("HEAD"), method = "onUse")
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.getBlockState(hit.getBlockPos()).getBlock() == Blocks.DIRT) {
            ItemStack itemStack = player.getStackInHand(hand);

            if (itemStack.isItemEqual(Items.POTION.getDefaultStack())) {
                if (PotionUtil.getPotion(itemStack) == Potions.WATER) {
                    world.setBlockState(pos, RegisterBlocks.MUD_BLOCK.getDefaultState());
                    if (!world.isClient) {
                        world.playSound(
                                null,
                                pos,
                                SoundEvents.BLOCK_GRAVEL_BREAK,
                                SoundCategory.BLOCKS,
                                1f,
                                1f
                        );
                        ((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, RegisterBlocks.MUD_BLOCK.getDefaultState()),
                                hit.getBlockPos().getX() + 0.5,
                                hit.getBlockPos().getY() + 0.5,
                                hit.getBlockPos().getZ() + 0.5,
                                100,
                                0.2,
                                0.2,
                                0.2,
                                10
                        );

                    }
                    if (player.canModifyBlocks()) {
                        if (!player.isCreative()) {
                            ItemStack removeItem = new ItemStack(Items.GLASS_BOTTLE);
                            player.setStackInHand(hand, removeItem);
                        }
                    }
                }
            }
        }
    }

    @Inject(at =  @At("TAIL"), method = "onEntityCollision")
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        if(world.getBlockState(pos) == Blocks.SCULK_SENSOR.getDefaultState() && entity!=null && entity.getType()!=RegisterEntities.WARDEN) {
            if (entity instanceof LivingEntity) {
                SensorLastEntity.addEntity(entity, pos, entity.getBlockPos(), null);
                int lastEntity = SensorLastEntity.getLastEntity(pos);
                LivingEntity target = (LivingEntity) world.getEntityById(lastEntity);
                BlockPos lastEventPos = SensorLastEntity.getLastPos(pos);
                if (lastEventPos != null) {
                    Box box = new Box(pos.getX() - 18, pos.getY() - 18, pos.getZ() - 18, pos.getX() + 18, pos.getY() + 18, pos.getZ() + 18);
                    List<WardenEntity> list = world.getNonSpectatingEntities(WardenEntity.class, box);
                    Iterator<WardenEntity> var11 = list.iterator();
                    WardenEntity wardenEntity;
                    while (var11.hasNext()) {
                        wardenEntity = var11.next();
                        if (wardenEntity.getBlockPos().isWithinDistance(pos, 16)) {
                            wardenEntity.listen(lastEventPos, wardenEntity.getWorld(), target, 10, pos);
                        }
                    }
                }
            }
            SculkSensorBlock.setActive(world, pos, state, 15);
        }
        assert entity != null;
        if(entity.getType()!=RegisterEntities.WARDEN && world.getBlockState(pos) == SculkShriekerBlock.SCULK_SHRIEKER_BLOCK.getDefaultState() || world.getBlockState(pos) == SculkShriekerBlock.SCULK_SHRIEKER_BLOCK.getDefaultState().with(Properties.WATERLOGGED, true)) {
            if (!ShriekCounter.findWarden(world, pos)) {
                ((SculkShriekerBlock) Objects.requireNonNull(world.getBlockState(pos)).getBlock()).writeDir(world, pos, entity.getBlockPos());
                SculkShriekerBlock.setStepActive(world, pos, state);
            }
        }
    }

}
