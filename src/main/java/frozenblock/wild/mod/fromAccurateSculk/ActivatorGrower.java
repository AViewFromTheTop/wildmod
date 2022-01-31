package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.blocks.SculkShriekerBlock;
import frozenblock.wild.mod.blocks.SculkVeinBlock;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import frozenblock.wild.mod.registry.RegisterBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

import static java.lang.Math.*;


public class ActivatorGrower {
    public void startActivator(LivingEntity entity, BlockPos pos) { //Calculate Initial Radius & Loop
        if (SculkTags.THREE.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
            placeActiveOmptim(3, 4, 7, pos, entity);
        } else if (SculkTags.FIVE.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
            placeActiveOmptim(4, 5, 7, pos, entity);
        } else if (SculkTags.TEN.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
            placeActiveOmptim(9, 10, 6, pos, entity);
        } else if (SculkTags.TWENTY.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
            placeActiveOmptim(19, 20, 9, pos, entity);
        } else if (SculkTags.FIFTY.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
            placeActiveOmptim(59, 50, 14, pos, entity);
        } else if (SculkTags.ONEHUNDRED.contains(entity.getType())) {
            entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos());
            placeActiveOmptim(1000, 33, 20, pos, entity);
        }
    }

    public void placeActiveOmptim(int loop, int rVal, int CHANCE, BlockPos pos, LivingEntity entity) { //Call For Placement
            /*for (int l = 0; l < (loop * entity.world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER)); ++l) {
                double a = random() * 2 * PI;
                double r = sqrt(rVal * entity.world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER)) * sqrt(random());
                int x = (int) (r * cos(a));
                int y = (int) (r * sin(a));
                placeActivator(pos.add(x, 0, y), entity.world, CHANCE);
            }*/
        }

    public void placeActivator(BlockPos blockPos, World world, int chance) { //Place Activators
        BlockState sculk = RegisterBlocks.SCULK.getDefaultState();
        BlockState sensor = Blocks.SCULK_SENSOR.getDefaultState();
        BlockState shrieker = SculkShriekerBlock.SCULK_SHRIEKER_BLOCK.getDefaultState();
        BlockPos NewSculk;
        int chanceCheck = (chance + 4);
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(solidsculkCheck(blockPos, world).up()).getBlock()) && world.getBlockState(solidsculkCheck(blockPos, world)) == sculk) {
            NewSculk = solidsculkCheck(blockPos, world);
            if (NewSculk.getY() != 64) {
                int uniInt = UniformIntProvider.create(1, 20).get(world.getRandom());
                if ((UniformIntProvider.create(0, chance + 5).get(world.getRandom()) > chanceCheck)) {
                    if (uniInt <= 16) {
                        if (world.getBlockState(NewSculk.up()) == Blocks.WATER.getDefaultState()) {
                            world.setBlockState(NewSculk.up(), sensor.with(Properties.WATERLOGGED, true));
                        } else if (world.getBlockState(NewSculk.up()).getBlock() != Blocks.WATER) {
                            if (world.getBlockState(NewSculk.up()) == SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.WATERLOGGED, true)) {
                                world.setBlockState(NewSculk.up(), sensor.with(Properties.WATERLOGGED, true));
                            } else {
                                world.removeBlock(NewSculk.up(), true);
                                world.setBlockState(NewSculk.up(), sensor);
                            }
                        }
                    } else {
                        if (world.getBlockState(NewSculk.up()) == Blocks.WATER.getDefaultState()) {
                            world.setBlockState(NewSculk.up(), shrieker.with(Properties.WATERLOGGED, true));
                        } else if (world.getBlockState(NewSculk.up()).getBlock() != Blocks.WATER) {
                            if (world.getBlockState(NewSculk.up()) == SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.WATERLOGGED, true)) {
                                world.setBlockState(NewSculk.up(), shrieker.with(Properties.WATERLOGGED, true));
                            } else {
                                world.removeBlock(NewSculk.up(), true);
                                world.setBlockState(NewSculk.up(), shrieker);
                            }
                        }
                    }
                }
            }
        }
    }

    /** CAlCULATIONS & CHECKS */
    public BlockPos solidsculkCheck(BlockPos blockPos, World world) {
        if (checkPt1(blockPos, world).getY()!=-64) {
            return checkPt1(blockPos, world);
        } else if (checkPt2(blockPos, world).getY()!=-64) {
            return checkPt2(blockPos, world);
        } else { return new BlockPos(0,-64,0); }
    }
    public BlockPos checkPt1(BlockPos blockPos, World world) {
        int upward = world.getGameRules().getInt(WildMod.UPWARD_SPREAD);
        int MAX = world.getHeight();
        if (blockPos.getY() + upward >= MAX) {
            upward = (MAX - blockPos.getY())-1;
        }
        for (int h = 0; h < upward; h++) {
            if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up(h+1)).getBlock()) &&
                    world.getBlockState(blockPos.add(0,(h),0))== RegisterBlocks.SCULK.getDefaultState()) {
                return blockPos.up(h);
            }
        }
        return new BlockPos(0,-64,0);
    }
    public BlockPos checkPt2(BlockPos blockPos, World world) {
        int downward = world.getGameRules().getInt(WildMod.DOWNWARD_SPREAD);
        int MIN = world.getBottomY();
        if (blockPos.getY() - downward <= MIN) {
            downward = (MIN + blockPos.getY())+1;
        }
        for (int h = 0; h < downward; h++) {
            if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.down(h)).getBlock()) &&
                    world.getBlockState(blockPos.down(h+1))==RegisterBlocks.SCULK.getDefaultState()) {
                return blockPos.down(h+1);
            }
        }
        return new BlockPos(0,-64,0);
    }
}
