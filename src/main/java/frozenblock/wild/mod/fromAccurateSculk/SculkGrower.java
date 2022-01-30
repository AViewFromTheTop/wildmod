package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.blocks.SculkVeinBlock;
import frozenblock.wild.mod.registry.RegisterBlocks;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.*;

public class SculkGrower {

    public void sculk(BlockPos blockPos, World world, @Nullable Entity entity) { //Calculate Amount Of Sculk + Initial Radius
        if (entity!=null) {
            world.playSound(null, blockPos, RegisterSounds.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 1F, 1F);
            BlockPos down = blockPos.down();
            if (!world.getGameRules().getBoolean(WildMod.SCULK_THREADING)) {
                if (SculkTags.THREE.contains(entity.getType())) {
                    sculkOptim(3, 4, down, world);
                } else if (SculkTags.FIVE.contains(entity.getType())) {
                    sculkOptim(4, 5, down, world);
                } else if (SculkTags.TEN.contains(entity.getType())) {
                    sculkOptim(9, 10, down, world);
                } else if (SculkTags.TWENTY.contains(entity.getType())) {
                    sculkOptim(19, 20, down, world);
                } else if (SculkTags.FIFTY.contains(entity.getType())) {
                    sculkOptim(59, 50, down, world);
                } else if (SculkTags.ONEHUNDRED.contains(entity.getType())) {
                    sculkOptim(1599, 33, down, world);
                } else if (world.getGameRules().getBoolean(WildMod.CATALYST_DETECTS_ALL)) {
                    sculkOptim((UniformIntProvider.create(1, 7).get(world.getRandom())), (UniformIntProvider.create(1, 7).get(world.getRandom())), down, world);
                }
            }
        }
    }

    public void sculkOptim(float loop, int rVal, BlockPos down, World world) { //Call For Sculk Placement & Increase Radius If Stuck
        int rVal2 = MathHelper.clamp(rVal*world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER),1, 64);
        int timesFailed=0;
        int groupsFailed=1;
        float fLoop = loop * world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER);

        for (int l = 0; l < fLoop;) {
            double a = random() * 2 * PI;
            double r = sqrt((rVal2+(timesFailed/7))) * sqrt(random());
            boolean succeed = placeSculk(down.add((int) (r * sin(a)), 0, (int) (r * cos(a))), world);
            if (!succeed) { ++timesFailed; } else { ++l; }
            if (timesFailed>=groupsFailed*7) {
                ++groupsFailed;
                }
            if (rVal2>64) { break; }
        }
    }
    public boolean placeSculk(BlockPos blockPos, World world) { //Call For Sculk & Call For Veins
        BlockPos NewSculk;
        if (SculkTags.BLOCK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock())) {
            NewSculk = blockPos;
            placeSculkOptim(NewSculk, world);
            return true;
        } else if (SculkTags.BLOCK_REPLACEABLE.contains(world.getBlockState(sculkCheck(blockPos, world, blockPos)).getBlock()) && air(world, sculkCheck(blockPos, world, blockPos))) {
            NewSculk = sculkCheck(blockPos, world, blockPos);
            if (NewSculk != blockPos) {
                placeSculkOptim(NewSculk, world);
                return true;
            }
        } else if (solid(world, sculkCheck(blockPos, world, blockPos))) {
            NewSculk = sculkCheck(blockPos, world, blockPos);
            SculkTags.SCULK.contains(world.getBlockState(NewSculk.up()).getBlock());
            if (!SculkTags.SCULK.contains(world.getBlockState(NewSculk.up()).getBlock())) {
                veins(NewSculk, world);
                return true;
            }
        }
        return false;
    }

    public void placeSculkOptim(BlockPos NewSculk, World world) { //Place Sculk & Call For Veins
        BlockState sculk = RegisterBlocks.SCULK.getDefaultState();
        veins(NewSculk, world);
        world.removeBlock(NewSculk, true);
        world.setBlockState(NewSculk, sculk);
        if (world.getBlockState(NewSculk.up()).getBlock()!= Blocks.WATER) {
            if (world.getBlockState(NewSculk.up()).contains(Properties.WATERLOGGED)&&world.getBlockState(NewSculk.up()).get(Properties.WATERLOGGED).equals(true)) {
                world.setBlockState(NewSculk.up(), Blocks.WATER.getDefaultState());
            } else { world.setBlockState(NewSculk.up(), Blocks.AIR.getDefaultState()); }
        }
    }

    public void veins(BlockPos blockPos, World world) { //Calculate Vein Placement
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.add(1, 1, 0)).getBlock()) && solid(world, blockPos.add(1, 0, 0)) && airveins(world, blockPos.add(1, 0, 0))) {
            veinPlaceOptim(blockPos.add(1, 1, 0), world);
        } else if (sculkCheck(blockPos.add(1, 1, 0), world, blockPos) != blockPos.add(1, 1, 0) && solidrep(world, sculkCheck(blockPos.add(1, 1, 0), world, blockPos)) && airveins(world, sculkCheck(blockPos.add(1, 1, 0), world, blockPos))) {
            veinPlaceOptim(sculkCheck(blockPos.add(1, 1, 0), world, blockPos).up(), world);
        }
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.add(-1, 1, 0)).getBlock()) && solid(world, blockPos.add(-1, 0, 0)) && airveins(world, blockPos.add(-1, 0, 0))) {
            veinPlaceOptim(blockPos.add(-1,1,0), world);
        } else if (sculkCheck(blockPos.add(-1, 1, 0), world, blockPos) != blockPos.add(-1, 1, 0) && solidrep(world, sculkCheck(blockPos.add(-1, 1, 0), world, blockPos)) && airveins(world, sculkCheck(blockPos.add(-1, 1, 0), world, blockPos))) {
            veinPlaceOptim(sculkCheck(blockPos.add(-1,1,0), world, blockPos).up(), world);
        }
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && solid(world, blockPos.add(0, 0, 1)) && airveins(world, blockPos.add(0, 0, 1))) {
            veinPlaceOptim(blockPos.add(0,1,1), world);
        } else if (sculkCheck(blockPos.add(0, 1, 1), world, blockPos) != blockPos.add(0, 1, 1) && solidrep(world, sculkCheck(blockPos.add(0, 1, 1), world, blockPos)) && airveins(world, sculkCheck(blockPos.add(0, 1, 1), world, blockPos))) {
            veinPlaceOptim(sculkCheck(blockPos.add(0,1,1), world, blockPos).up(), world);
        }
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && solid(world, blockPos.add(0, 0, -1)) && airveins(world, blockPos.add(0, 0, -1))) {
            veinPlaceOptim(blockPos.add(0, 1, -1), world);
        } else if (sculkCheck(blockPos.add(0, 1, -1), world, blockPos) != blockPos.add(0, 1, -1) && solidrep(world, sculkCheck(blockPos.add(0, 1, -1), world, blockPos)) && airveins(world, sculkCheck(blockPos.add(0, 1, -1), world, blockPos))) {
            veinPlaceOptim(sculkCheck(blockPos.add(0,1,-1), world, blockPos).up(), world);
        }
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock()) && solid(world, blockPos.add(0, 0, 0)) && airveins(world, blockPos.add(0, 0, 0))) {
            veinPlaceOptim(blockPos.add(0, 1, 0), world);
        } else if (sculkCheck(blockPos.up(), world, blockPos) != blockPos.up() && solidrep(world, sculkCheck(blockPos.up(), world, blockPos)) && airveins(world, sculkCheck(blockPos.up(), world, blockPos))) {
            veinPlaceOptim(sculkCheck(blockPos.up(), world, blockPos).up(), world);
        }
    }

    public void veinPlaceOptim(BlockPos curr, World world) { //Place Veins
        if (SculkTags.ALWAYS_WATER.contains(world.getBlockState(curr).getBlock()) || world.getBlockState(curr)==Blocks.WATER.getDefaultState()) {
            world.setBlockState(curr, vein.with(Properties.WATERLOGGED, true));
            tiltVeins(curr, world);
            tiltVeinsDown(curr, world);
        } else if (world.getBlockState(curr).getBlock() != Blocks.WATER) {
            if (world.getBlockState(curr).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                world.setBlockState(curr, world.getBlockState(curr).with(Properties.DOWN, true));
                tiltVeins(curr, world);
                tiltVeinsDown(curr, world);
            } else
                world.setBlockState(curr, vein);
            tiltVeins(curr, world);
            tiltVeinsDown(curr, world);
        }
    }

/** BLOCKSTATE TWEAKING */
    public void tiltVeins(BlockPos blockPos, World world) { //Tilt Sculk Veins
        BlockState currentSculk = world.getBlockState(blockPos);
        if (!SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.down())).getBlock())) {
            if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(1, 0, 0))).getBlock())) {
                world.setBlockState(blockPos, currentSculk.with(Properties.EAST, true));
                currentSculk = world.getBlockState(blockPos);
                if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(-1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(-1, 0, 0))).getBlock())) {
                    world.setBlockState(blockPos, currentSculk.with(Properties.WEST, true));
                    currentSculk = world.getBlockState(blockPos);
                    if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
                        world.setBlockState(blockPos, currentSculk.with(Properties.NORTH, true));
                        currentSculk = world.getBlockState(blockPos);
                        if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, 1))).getBlock())) {
                            world.setBlockState(blockPos, currentSculk.with(Properties.SOUTH, true));
                        }
                    }
                }
            } else if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(-1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(-1, 0, 0))).getBlock())) {
                world.setBlockState(blockPos, currentSculk.with(Properties.WEST, true));
                currentSculk = world.getBlockState(blockPos);
                if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
                    world.setBlockState(blockPos, currentSculk.with(Properties.NORTH, true));
                    currentSculk = world.getBlockState(blockPos);
                    if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, 1))).getBlock())) {
                        world.setBlockState(blockPos, currentSculk.with(Properties.SOUTH, true));
                    }
                }
            } else if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
                world.setBlockState(blockPos, currentSculk.with(Properties.NORTH, true));
                currentSculk = world.getBlockState(blockPos);
                if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, 1))).getBlock())) {
                    world.setBlockState(blockPos, currentSculk.with(Properties.SOUTH, true));
                }
            } else if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, 1))).getBlock())) {
                world.setBlockState(blockPos, currentSculk.with(Properties.SOUTH, true));
            }
        }
    }
    public void tiltVeinsDown(BlockPos blockPos, World world) { //Tilt Veins Downwards
        BlockState currentSculk;
        if (!SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.down())).getBlock())) {
            if (world.getBlockState(blockPos.add(1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                currentSculk = world.getBlockState(blockPos.add(1, -1, 0));
                world.setBlockState(blockPos.add(1, -1, 0), currentSculk.with(Properties.WEST, true));
                if (world.getBlockState(blockPos.add(-1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                    currentSculk = world.getBlockState(blockPos.add(-1, -1, 0));
                    world.setBlockState(blockPos.add(-1, -1, 0), currentSculk.with(Properties.EAST, true));
                    if (world.getBlockState(blockPos.add(0, -1, -1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                        currentSculk = world.getBlockState(blockPos.add(0, -1, -1));
                        world.setBlockState(blockPos.add(0, -1, -1), currentSculk.with(Properties.SOUTH, true));
                        if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                            currentSculk = world.getBlockState(blockPos.add(0, -1, 1));
                            world.setBlockState(blockPos.add(0, -1, 1), currentSculk.with(Properties.NORTH, true));
                        }
                    }
                }
            } else if (world.getBlockState(blockPos.add(-1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                currentSculk = world.getBlockState(blockPos.add(-1, -1, 0));
                world.setBlockState(blockPos.add(-1, -1, 0), currentSculk.with(Properties.EAST, true));
                if (world.getBlockState(blockPos.add(0, -1, -1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                    currentSculk = world.getBlockState(blockPos.add(0, -1, -1));
                    world.setBlockState(blockPos.add(0, -1, -1), currentSculk.with(Properties.SOUTH, true));
                    if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                        currentSculk = world.getBlockState(blockPos.add(0, -1, 1));
                        world.setBlockState(blockPos.add(0, -1, 1), currentSculk.with(Properties.NORTH, true));
                    }
                }
            } else if (world.getBlockState(blockPos.add(0, -1, -1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                currentSculk = world.getBlockState(blockPos.add(0, -1, -1));
                world.setBlockState(blockPos.add(0, -1, -1), currentSculk.with(Properties.SOUTH, true));
                if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                    currentSculk = world.getBlockState(blockPos.add(0, -1, 1));
                    world.setBlockState(blockPos.add(0, -1, 1), currentSculk.with(Properties.NORTH, true));
                }
            } else if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                currentSculk = world.getBlockState(blockPos.add(0, -1, 1));
                world.setBlockState(blockPos.add(0, -1, 1), currentSculk.with(Properties.NORTH, true));
            }
        }
    }

    /** CAlCULATIONS & CHECKS */
    public BlockPos sculkCheck(BlockPos blockPos, World world, BlockPos blockPos2) { //Call For Up&Down Checks
        if (checkPt1(blockPos, world).getY()!=-64) {
            return checkPt1(blockPos, world);
        } else if (checkPt2(blockPos, world).getY()!=-64) {
            return checkPt2(blockPos, world);
        } else { return blockPos2; }
    }
    public BlockPos checkPt1(BlockPos blockPos, World world) { //Check For Valid Placement Above
        int upward = world.getGameRules().getInt(WildMod.UPWARD_SPREAD);
        int MAX = world.getHeight();
        if (blockPos.getY() + upward >= MAX) {
            upward = (MAX - blockPos.getY())-1;
        }
        for (int h = 0; h < upward; h++) {
            if (solrepsculk(world, blockPos.up(h))) {
                return blockPos.up(h);
            }
        }
        return new BlockPos(0,-64,0);
    }
    public BlockPos checkPt2(BlockPos blockPos, World world) { //Check For Valid Placement Below
        int downward = world.getGameRules().getInt(WildMod.DOWNWARD_SPREAD);
        int MIN = world.getBottomY();
        if (blockPos.getY() - downward <= MIN) {
            downward = (blockPos.getY()-MIN)-1;
        }
        for (int h = 0; h < downward; h++) {
            if (solrepsculk(world, blockPos.down(h))) {
                return blockPos.down(h);
            }
        }
        return new BlockPos(0,-64,0);
    }

    public boolean airveins(World world, BlockPos blockPos) { //Check If Veins Are Above Invalid Block
        if (SculkTags.SCULK.contains(world.getBlockState(blockPos).getBlock())) {
            return false;
        } else if (world.getBlockState(blockPos).isAir()) {
            return false;
        } else if (FluidTags.WATER.contains(world.getFluidState(blockPos).getFluid())) {
            return false;
        } else if (FluidTags.LAVA.contains(world.getFluidState(blockPos).getFluid())) {
            return false;
        } else if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock())) {
            return false;
        } else return !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock());
    }

    public boolean solid(World world, BlockPos blockPos) {
        return (!world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock()));
    }
    public boolean solidrep(World world, BlockPos blockPos) {
        return (!world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && !SculkTags.SCULK.contains(world.getBlockState(blockPos.down()).getBlock()));
    }
    public boolean solrepsculk(World world, BlockPos blockPos) {
        return (!SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock()) && !SculkTags.SCULK.contains(world.getBlockState(blockPos).getBlock()));
    }
    public boolean air(World world, BlockPos blockPos) {
        return SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock());
    }

    /** DEFAULT BLOCKSTATES */
    public BlockState vein = SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.DOWN, true);

}
