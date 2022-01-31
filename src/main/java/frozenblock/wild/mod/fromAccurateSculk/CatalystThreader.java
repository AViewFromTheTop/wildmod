package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.blocks.SculkBlock;
import frozenblock.wild.mod.blocks.SculkShriekerBlock;
import frozenblock.wild.mod.blocks.SculkVeinBlock;
import frozenblock.wild.mod.registry.RegisterBlocks;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.entity.Entity;
import net.minecraft.server.ServerTask;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.Math.*;

public class CatalystThreader {
    public static int running;

    public static void main(World world, Entity entity, BlockPos blockPos, int div) throws InterruptedException {
        if (div > 0) {
            world.playSound(null, blockPos, RegisterSounds.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 1F, 1F);
            SculkThread T1 = new SculkThread("sculkThread");
            T1.blockPos = blockPos;
            T1.world = world;
            T1.setPriority(Thread.MAX_PRIORITY);
            T1.l = div;
            T1.entity=entity;
            T1.start();
            T1.join();
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
    public Entity entity;
    public int l;

    SculkThread( String name) {
        threadName = name;
    }
    @Override
    public void run() {
        try {
            CatalystThreader.running++;
            sculk(blockPos, world, entity);
            CatalystThreader.running--;
        } catch (Exception e) {
            e.printStackTrace();
            CatalystThreader.running--;
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
    /** DEFAULT VARIABLES */
    public static final BlockState vein = SculkVeinBlock.SCULK_VEIN.getDefaultState().with(Properties.DOWN, true);
    public static final BlockState water = Blocks.WATER.getDefaultState();
    public static final BlockState air = Blocks.AIR.getDefaultState();
    public static final Block waterBlock = Blocks.WATER;
    public static final BooleanProperty waterLogged = Properties.WATERLOGGED;

    /** MAIN CODE */
    public void sculk(BlockPos blockPos, World world, @Nullable Entity entity) { //Choose Amount Of Sculk + Initial Radius
        if (entity!=null) {
            world.playSound(null, blockPos, RegisterSounds.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 1F, 1F);
            BlockPos down = blockPos.down();
            if (SculkTags.THREE.contains(entity.getType())) {
                sculkOptim(3*l, 4, down, world);
            } else if (SculkTags.FIVE.contains(entity.getType())) {
                sculkOptim(5*l, 5, down, world);
            } else if (SculkTags.TEN.contains(entity.getType())) {
                sculkOptim(10*l, 10, down, world);
            } else if (SculkTags.TWENTY.contains(entity.getType())) {
                sculkOptim(20*l, 20, down, world);
            } else if (SculkTags.FIFTY.contains(entity.getType())) {
                sculkOptim(50*l, 50, down, world);
            } else if (SculkTags.ONEHUNDRED.contains(entity.getType())) {
                sculkOptim(1000*l, 33, down, world);
            } else if (world.getGameRules().getBoolean(WildMod.CATALYST_DETECTS_ALL)) {
                sculkOptim((UniformIntProvider.create(1, 7).get(world.getRandom())), (UniformIntProvider.create(1, 7).get(world.getRandom())), down, world);
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
            double r = sqrt(rVal2+(groupsFailed-1)) * sqrt(random());
            boolean succeed = placeSculk(down.add((int) (r * sin(a)), 0, (int) (r * cos(a))), world);
            if (!succeed) { ++timesFailed; } else { ++l; }
            if (timesFailed>=groupsFailed*7) {
                ++groupsFailed;
            }
            if (rVal2>64) {
                break;
            }
        }
    }

    public boolean placeSculk(BlockPos blockPos, World world) { //Call For Sculk & Call For Veins
        BlockPos NewSculk;
        if (SculkTags.BLOCK_REPLACEABLE.contains(world.getBlockState(blockPos).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock())) {
            NewSculk = blockPos;
            placeSculkOptim(NewSculk, world);
            return true;
        } else {
            NewSculk = sculkCheck(blockPos, world);
            if (NewSculk != null) {
                if (SculkTags.BLOCK_REPLACEABLE.contains(world.getBlockState(NewSculk).getBlock()) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(NewSculk.up()).getBlock())) {
                    placeSculkOptim(NewSculk, world);
                    return true;
                } else if (solid(world, NewSculk)) {
                    if (!SculkTags.SCULK.contains(world.getBlockState(NewSculk.up()).getBlock())) {
                        veins(NewSculk, world);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public void placeSculkOptim(BlockPos NewSculk, World world) { //Place Sculk & Call For Veins
        veins(NewSculk, world);
        callPlace(world, SculkBlock.SCULK_BLOCK.getDefaultState(), NewSculk);
        BlockState upBlock = world.getBlockState(NewSculk.up());
        if (upBlock.getBlock()!=waterBlock) {
            if (upBlock.contains(waterLogged) && upBlock.get(waterLogged)) {
                callPlace(world, water, NewSculk.up());
            } else { callPlace(world, air, NewSculk.up()); }
        }
    }

    public void veinPlaceOptim(BlockPos curr, World world) {
        if (SculkTags.ALWAYS_WATER.contains(world.getBlockState(curr).getBlock()) || world.getBlockState(curr)==water) {
            callVeinPlace(world, vein.with(waterLogged, true), curr);
        } else if (world.getBlockState(curr).getBlock() != waterBlock) {
            callVeinPlace(world, vein, curr);
        }
    }

    public void tiltVeinsDown(BlockPos blockPos, World world) { //Tilt Veins Downwards
        if (!SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.down())).getBlock())) {
            if (world.getBlockState(blockPos.add(1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                world.setBlockState(blockPos.add(1, -1, 0), world.getBlockState(blockPos.add(1, -1, 0)).with(Properties.WEST, true));
            }
            if (world.getBlockState(blockPos.add(-1, -1, 0)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                world.setBlockState(blockPos.add(-1, -1, 0), world.getBlockState(blockPos.add(-1, -1, 0)).with(Properties.EAST, true));
            }
            if (world.getBlockState(blockPos.add(0, -1, -1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                world.setBlockState(blockPos.add(0, -1, -1), world.getBlockState(blockPos.add(0, -1, -1)).with(Properties.SOUTH, true));
            }
            if (world.getBlockState(blockPos.add(0, -1, 1)).getBlock() == SculkVeinBlock.SCULK_VEIN) {
                world.setBlockState(blockPos.add(0, -1, 1), world.getBlockState(blockPos.add(0, -1, 1)).with(Properties.NORTH, true));}
        }
    }
    public void tiltVeins(BlockPos blockPos, World world) { //Tilt Sculk Veins
        if (!SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.down())).getBlock())) {
            if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(1, 0, 0))).getBlock())) {
                world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.EAST, true));
            }
            if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(-1, 1, 0)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(-1, 0, 0))).getBlock())) {
                world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.WEST, true));
            }
            if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, -1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, -1))).getBlock())) {
                world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.NORTH, true));
            }
            if (SculkTags.VEIN_CONNECTABLE.contains(world.getBlockState(blockPos.add(0, 1, 1)).getBlock()) && !SculkTags.SCULK_UNBENDABLE.contains((world.getBlockState(blockPos.add(0, 0, 1))).getBlock())) {
                world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.SOUTH, true));
            }
        }
    }
    public void veins(BlockPos blockPos, World world) { //Calculate Vein Placement
        veinsPartTwo(world,blockPos.add(1, 1, 0),blockPos.add(1, 0, 0));
        veinsPartTwo(world,blockPos.add(-1, 1, 0),blockPos.add(-1, 0, 0));
        veinsPartTwo(world,blockPos.add(0, 1, 1),blockPos.add(0, 0, 1));
        veinsPartTwo(world,blockPos.add(0, 1, -1),blockPos.add(0, 0, -1));
        veinsPartTwo(world,blockPos.up(),blockPos);
    }

    public void veinsPartTwo(World world, BlockPos one, BlockPos two) {
        if (SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(one).getBlock()) && solid(world, two) && airveins(world, two)) {
            veinPlaceOptim(one, world);
        } else { BlockPos check = sculkCheck(one, world);
            if (check != null && solidrep(world, check) && airveins(world, check)) {
                veinPlaceOptim(check.up(), world);
            }
        }
    }

    /** CAlCULATIONS & CHECKS */
    public BlockPos sculkCheck(BlockPos blockPos, World world) { //Call For Up&Down Checks
        if (checkPtA(blockPos, world)!=null) {
            return checkPtA(blockPos, world);
        } else if (checkPt1(blockPos, world)!=null) {
            return checkPt1(blockPos, world);
        } else if (checkPt2(blockPos, world)!=null) {
            return checkPt2(blockPos, world);
        } else { return null; }
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
        return null;
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
        return null;
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
        return null;
    }

    public boolean airveins(World world, BlockPos blockPos) { //Check If Veins Are Above Invalid Block
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        if (SculkTags.SCULK.contains(block)) {
            return false;
        } else if (state.isAir()) {
            return false;
        } else if (FluidTags.WATER.contains(world.getFluidState(blockPos).getFluid())) {
            return false;
        } else if (FluidTags.LAVA.contains(world.getFluidState(blockPos).getFluid())) {
            return false;
        } else if (SculkTags.SCULK_REPLACEABLE.contains(block)) {
            return false;
        } else return !SculkTags.SCULK_UNTOUCHABLE.contains(block);
    }

    public boolean solid(World world, BlockPos blockPos) {
        return (blockPos!=null && !world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(world.getBlockState(blockPos).getBlock()));
    }
    public boolean solidrep(World world, BlockPos blockPos) {
        Block block = world.getBlockState(blockPos).getBlock();
        return (!world.getBlockState(blockPos).isAir() && !SculkTags.SCULK_UNTOUCHABLE.contains(block) && SculkTags.SCULK_REPLACEABLE.contains(block) && !SculkTags.SCULK.contains(world.getBlockState(blockPos.down()).getBlock()));
    }
    public boolean solrepsculk(World world, BlockPos blockPos) {
        Block block = world.getBlockState(blockPos).getBlock();
        return (!SculkTags.SCULK_REPLACEABLE.contains(block) && SculkTags.SCULK_REPLACEABLE.contains(world.getBlockState(blockPos.up()).getBlock()) && !SculkTags.SCULK.contains(block));
    }

    public void callPlace(World world, BlockState blockState, BlockPos pos) {
        Runnable runa2 = () -> world.setBlockState(pos, blockState);
        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa2));
    }
    public void callVeinPlace(World world, BlockState blockState, BlockPos pos) {
        Runnable runa2 = () -> {
            if (world.getBlockState(pos).getBlock()!=SculkVeinBlock.SCULK_VEIN) world.setBlockState(pos, blockState);
            tiltVeins(pos, world);
            tiltVeinsDown(pos, world);
        };

        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa2));
    }
    public void callDestroy(World world, BlockPos pos) {
        Runnable runa = () -> world.setBlockState(pos, Blocks.AIR.getDefaultState());

        Objects.requireNonNull(world.getServer()).send(new ServerTask(0, runa));
    }
}