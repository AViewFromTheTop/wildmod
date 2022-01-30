package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.blocks.SculkShriekerBlock;
import frozenblock.wild.mod.blocks.SculkVeinBlock;
import frozenblock.wild.mod.registry.RegisterBlocks;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.server.ServerTask;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

import java.util.Objects;

import static java.lang.Math.*;

public class CatalystThreader {

    public static void main(World world, BlockPos blockPos, int l, int r, int div, int chance) throws InterruptedException {
        float threads = world.getGameRules().getInt(WildMod.SCULK_THREADS);
        int lbd = (int) Math.ceil(l * div);
        if (div > 0) {
            world.playSound(null, blockPos, RegisterSounds.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 1F, 1F);
            if (threads >= 21) {
                for (int h = 0; h < lbd; h++) {
                    SculkThread T1 = new SculkThread("Sculk" + h);
                    T1.blockPos = blockPos;
                    T1.world = world;
                    T1.l = 1;
                    T1.r = r;
                    T1.setPriority(Thread.MAX_PRIORITY);
                    T1.start();
                }
                ActivatorThread T2 = new ActivatorThread("activatorThread");
                T2.blockPos = blockPos;
                T2.world = world;
                T2.r = sqrt(r * world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER));
                T2.chance = chance;
                T2.setPriority(Thread.MIN_PRIORITY);
                T2.l = l*world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER);
                T2.start();
            } else {
                if ((int) Math.floor(l / threads * div) >= 1) {
                    int numThreads = (int) Math.floor(l / threads * div);
                    for (int h = 0; (h < numThreads) || h < l; h++) {
                            SculkThread T1 = new SculkThread("Sculk" + h);
                            T1.blockPos = blockPos;
                            T1.world = world;
                            T1.l = div;
                            T1.r = r;
                            T1.setPriority(Thread.MAX_PRIORITY);
                            T1.start();
                        }
                    ActivatorThread T2 = new ActivatorThread("activatorThread");
                    T2.blockPos = blockPos;
                    T2.world = world;
                    T2.r = sqrt(r * world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER));
                    T2.chance = chance;
                    T2.setPriority(Thread.MIN_PRIORITY);
                    T2.l = l*world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER);
                    T2.start();
                } else for (int h = 0; h < l; h++) {
                    SculkThread T1 = new SculkThread("Sculk" + h);
                    T1.blockPos = blockPos;
                    T1.world = world;
                    T1.r = r;
                    T1.setPriority(Thread.MAX_PRIORITY);
                    T1.l = div;
                    T1.start();
                }
                ActivatorThread T2 = new ActivatorThread("activatorThread");
                T2.blockPos = blockPos;
                T2.world = world;
                T2.r = sqrt(r * world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER));
                T2.chance = chance;
                T2.setPriority(Thread.MIN_PRIORITY);
                T2.l = l*world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER);
                T2.start();
                }
        }
    }
}

class ActivatorThread extends Thread {
    private Thread t;
    public String threadName;
    public World world;
    public BlockPos blockPos;
    public double l;
    public double r;
    public int chance;
    ActivatorThread(String name) {
        this.threadName=name;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(50);
            placeActiveOptim(l, r, blockPos, world, chance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    public void placeActiveOptim(double loop, double rVal, BlockPos pos, World world, int chnce) {
        int chanceCheck = chnce + 4;
        for (int l = 0; l < loop; l++) {
            if (UniformIntProvider.create(0, chnce+5).get(world.getRandom()) > chanceCheck) {
                double a = random() * 2 * PI;
                double ra = rVal * sqrt(random());
                int x = (int) (ra * cos(a));
                int y = (int) (ra * sin(a));
                BlockPos NewSculk = solidsculkCheck(pos.add(x, 0, y), world);
                if (NewSculk.getY() != -64) {
                        int uniInt = UniformIntProvider.create(1, 20).get(world.getRandom());
                        if (uniInt <= 16) {
                            ActivOptim1(sensor, world, NewSculk.up());
                        } else {
                            ActivOptim1(shrieker, world, NewSculk.up());
                        }
                    }
                }
            }
        }

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
                    world.getBlockState(blockPos.add(0,(h),0))==RegisterBlocks.SCULK.getDefaultState()) {
                return blockPos.up(h);
            }
        }
        return new BlockPos(0,-64,0);
    }
    public BlockPos checkPt2(BlockPos blockPos, World world) {
        int downward = world.getGameRules().getInt(WildMod.DOWNWARD_SPREAD);
        int MIN = world.getBottomY();
        if (blockPos.getY() - downward <= MIN) {
            downward = (blockPos.getY()-MIN)-1;
        }
        for (int h = 0; h < downward; h++) {
            if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.down(h)).getBlock()) &&
                    world.getBlockState(blockPos.down(h+1))==RegisterBlocks.SCULK.getDefaultState()) {
                return blockPos.down(h+1);
            }
        }
        return new BlockPos(0,-64,0);
    }

    BlockState sensor = Blocks.SCULK_SENSOR.getDefaultState().with(Properties.SCULK_SENSOR_PHASE, SculkSensorPhase.COOLDOWN);
    BlockState shrieker = SculkShriekerBlock.SCULK_SHRIEKER_BLOCK.getDefaultState().with(NewProperties.SCULK_SHRIEKER_PHASE, SculkShriekerPhase.COOLDOWN);

    public void ActivOptim1(BlockState blockState, World world, BlockPos NewSculk) {
          if (world.getBlockState(NewSculk).getBlock() != Blocks.WATER) {
            if (world.getBlockState(NewSculk) == SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.WATERLOGGED, true)) {
                callPlace(world, blockState.with(Properties.WATERLOGGED, true), NewSculk);
                world.createAndScheduleBlockTick(NewSculk, blockState.getBlock(), 20);
            } else
                callPlace(world, blockState, NewSculk);
              world.createAndScheduleBlockTick(NewSculk, blockState.getBlock(), 20);
        } else if (world.getBlockState(NewSculk) == Blocks.WATER.getDefaultState()) {
              callPlace(world, blockState.with(Properties.WATERLOGGED, true), NewSculk);
              world.createAndScheduleBlockTick(NewSculk, blockState.getBlock(), 20);
          }
    }

    public void callPlace(World world, BlockState blockState, BlockPos pos) {
        Runnable runa2 = () -> world.setBlockState(pos, blockState);
        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa2));
    }
}

class SculkThread extends Thread {
    private Thread t;
    public String threadName;
    public World world;
    public BlockPos blockPos;
    public int l;
    public int r;

    SculkThread( String name) {
        threadName = name;
    }
@Override
    public void run() {
        try {
        sculkOptim(l, r, blockPos, world);
} catch (Exception e) {
        e.printStackTrace();
    }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    public void sculkOptim(float loop, int rVal, BlockPos down, World world) throws InterruptedException { //Call For Sculk Placement & Increase Radius If Stuck
        int rVal2 = MathHelper.clamp(rVal*world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER),1, 64);
        int timesFailed=0;
        int groupsFailed=1;
        float fLoop = loop * world.getGameRules().getInt(WildMod.SCULK_MULTIPLIER);

        for (int l = 0; l < fLoop;) {
            Thread.sleep(5);
            double a = random() * 2 * PI;
            double r = sqrt((rVal2+(timesFailed/7))) * sqrt(random());
            if (!placeSculk(down.add((int) (r * sin(a)), 0, (int) (r * cos(a))), world)) { ++timesFailed; } else { ++l; }
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
        callDestroy(world, NewSculk);
        callPlace(world, sculk, NewSculk);
        if (world.getBlockState(NewSculk.up()).getBlock()!= Blocks.WATER) {
            if (world.getBlockState(NewSculk.up()).contains(Properties.WATERLOGGED)&&world.getBlockState(NewSculk.up()).get(Properties.WATERLOGGED).equals(true)) {
                callPlace(world, Blocks.WATER.getDefaultState(), NewSculk.up());
            } else { callPlace(world, Blocks.AIR.getDefaultState(), NewSculk.up()); }
        }
    }

    public boolean solid(World world, BlockPos blockPos) {
        return (!world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock()));
    }
    public BlockPos sculkCheck(BlockPos blockPos, World world, BlockPos blockPos2) { //Call For Up&Down Checks
        if (checkPtA(blockPos, world).getY()!=-64) {
            return checkPtA(blockPos, world);
        } else if (checkPt1(blockPos, world).getY()!=-64) {
            return checkPt1(blockPos, world);
        } else if (checkPt2(blockPos, world).getY()!=-64) {
            return checkPt2(blockPos, world);
        } else { return blockPos2; }
    }
    public BlockPos checkPtA(BlockPos blockPos, World world) {
        int downward = 8;
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
    public BlockPos checkPt1(BlockPos blockPos, World world) {
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
    public BlockPos checkPt2(BlockPos blockPos, World world) {
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
    public boolean solrepsculk(World world, BlockPos blockPos) {
        return (!SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock()) && !SculkTags.SCULK.contains(world.getBlockState(blockPos).getBlock()));
    }
    public boolean air(World world, BlockPos blockPos) {
        return SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock());
    }

    public void veinPlaceOptim(BlockPos curr, World world) {
        if (SculkTags.ALWAYS_WATER.contains(world.getBlockState(curr).getBlock()) || world.getBlockState(curr)==Blocks.WATER.getDefaultState()) {
            callVeinPlace(world, vein.with(Properties.WATERLOGGED, true), curr);
        } else if (world.getBlockState(curr).getBlock() != Blocks.WATER) {
            //if (world.getBlockState(curr).getBlock()==SculkVein.SCULK_VEIN) {
            //    callVeinPlace(world, world.getBlockState(curr).with(Properties.DOWN, true), curr);
            //} else
                callVeinPlace(world, vein, curr);
        }
    }
    public BlockState vein = SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.DOWN, true);
    public void tiltNorth(BlockPos blockPos, World world) {
        if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
            world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.NORTH, true));
        }
    }
    public void tiltSouth(BlockPos blockPos, World world) {
        if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
            world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.SOUTH, true));
        }
    }
    public void tiltEast(BlockPos blockPos, World world) {
        if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
            world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.EAST, true));
        }
    }
    public void tiltWest(BlockPos blockPos, World world) {
        if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(-1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
            world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.WEST, true));
        }
    }
    public void tiltDownNorth(BlockPos blockPos, World world) {
        if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
            world.setBlockState(blockPos.add(0, -1, 1), world.getBlockState(blockPos.add(0, -1, 1)).with(Properties.NORTH, true));
        }
    }
    public void tiltDownSouth(BlockPos blockPos, World world) {
        if (world.getBlockState(blockPos.add(0, -1, -1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
            world.setBlockState(blockPos.add(0, -1, -1), world.getBlockState(blockPos.add(0, -1, -1)).with(Properties.SOUTH, true));
        }
    }
    public void tiltDownEast(BlockPos blockPos, World world) {
        if (world.getBlockState(blockPos.add(-1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
            world.setBlockState(blockPos.add(-1, -1, 0), world.getBlockState(blockPos.add(-1, -1, 0)).with(Properties.EAST, true));
        }
    }
    public void tiltDownWest(BlockPos blockPos, World world) {
        if (world.getBlockState(blockPos.add(1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
            world.setBlockState(blockPos.add(1, -1, 0), world.getBlockState(blockPos.add(1, -1, 0)).with(Properties.WEST, true));
        }
    }
    public void veins(BlockPos blockPos, World world) {
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
            veinPlaceOptim(sculkCheck(blockPos.add(0,1,0), world, blockPos).up(), world);
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
    public boolean airveins(World world, BlockPos blockPos) {
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
    public boolean solidrep(World world, BlockPos blockPos) {
        return (!world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && !SculkTags.SCULK.contains(world.getBlockState(blockPos.down()).getBlock()));
    }

    public void callPlace(World world, BlockState blockState, BlockPos pos) {
        Runnable runa2 = () -> world.setBlockState(pos, blockState);
        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa2));
    }
    public void callVeinPlace(World world, BlockState blockState, BlockPos pos) {
        Runnable runa2 = () -> {
            if (world.getBlockState(pos).getBlock()!=SculkVeinBlock.SCULK_VEIN) world.setBlockState(pos, blockState);
            tiltNorth(pos, world);
            tiltSouth(pos, world);
            tiltEast(pos, world);
            tiltWest(pos, world);
            tiltDownNorth(pos, world);
            tiltDownSouth(pos, world);
            tiltDownEast(pos, world);
            tiltDownWest(pos, world);
        };

        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa2));
    }
    public void callDestroy(World world, BlockPos pos) {
        Runnable runa = () -> world.setBlockState(pos, Blocks.AIR.getDefaultState());
         
        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa));
    }
}
