package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.registry.RegisterEntities;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.*;

public class ShriekCounter {
    public static int shrieks;
    private static long timer;

    public static void addShriek(BlockPos pos, World world, int i) {
        if (world.getTime()-timer< -90) {
            timer=0;
        }
        if (!world.isClient() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && world.getGameRules().getBoolean(WildMod.WARDEN_SPAWNING) && world.getTime() > timer) {
            timer=world.getTime()+30;
            if (!findWarden(world, pos) || world.getGameRules().getBoolean(WildMod.NO_WARDEN_COOLDOWN)) {
                if (world.getDifficulty().getId()==0) {
                    i = (int) MathHelper.clamp(i,2,5);
                }else if (world.getDifficulty().getId()==1) {
                    i = (int) MathHelper.clamp(i*1.25,3,10);
                } else if (world.getDifficulty().getId()==2) {
                    i = (int) MathHelper.clamp(i*1.5,4,15);
                } else if (world.getDifficulty().getId()==3) {
                    i = (int) MathHelper.clamp(i*2,5,15);
                }
                shrieks = shrieks + i;
                for (int t = 8; t > 0; t--) {
                    ArrayList<BlockPos> candidates = findBlock(pos.add(-1, 0, -1), t, true, world);
                    if (!candidates.isEmpty()) {
                            int ran = UniformIntProvider.create(0, candidates.size() - 1).get(world.getRandom());
                            BlockPos currentCheck = candidates.get(ran);
                            warn(world, pos);
                            timer=world.getTime()+30;
                            if (angerLevel() == 4) {
                                shrieks = 0;
                                WardenEntity warden = RegisterEntities.WARDEN.create(world);
                                assert warden != null;
                                warden.refreshPositionAndAngles((double) currentCheck.getX() + 1D, currentCheck.up(1).getY(), (double) currentCheck.getZ() + 1D, 0.0F, 0.0F);
                                world.spawnEntity(warden);
                                warden.handleStatus((byte) 5);
                                warden.leaveTime=world.getTime()+1200;
                                warden.setPersistent();
                                world.playSound(null, currentCheck, RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
                                break;
                        }
                        break;
                    }
                }
            } else if (findWarden(world, pos)) {
                shrieks = 0;
            }
        }
    }
    public static boolean findWarden(World world, BlockPos pos) {
        double x1 = pos.getX();
        double y1 = pos.getY();
        double z1 = pos.getZ();
        BlockPos side1 = new BlockPos(x1-50, y1-50, z1-50);
        BlockPos side2 = new BlockPos(x1+50, y1+50, z1+50);
        Box box = (new Box(side1, side2));
        List<WardenEntity> list = world.getNonSpectatingEntities(WardenEntity.class, box);
        if (!list.isEmpty()) {
            Iterator<WardenEntity> var11 = list.iterator();
            WardenEntity warden;
            while (var11.hasNext()) {
                warden = var11.next();
                if (warden.getBlockPos().isWithinDistance(pos, (48))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void warn(World world, BlockPos blockPos) {
        if (angerLevel()==1) {
            double a = random() * 2 * PI;
            double r = sqrt(16) * sqrt(random());
            int x = (int) (r * cos(a));
            int z = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,z);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSE, SoundCategory.NEUTRAL, 0.2F, 1F);
        } else
        if (angerLevel()==2) {
            double a = random() * 2 * PI;
            double r = sqrt(12) * sqrt(random());
            int x = (int) (r * cos(a));
            int z = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,z);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSER, SoundCategory.NEUTRAL, 0.3F, 1F);
        } else
        if (angerLevel()==3) {
            double a = random() * 2 * PI;
            double r = sqrt(8) * sqrt(random());
            int x = (int) (r * cos(a));
            int z = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,z);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSEST, SoundCategory.NEUTRAL, 0.4F, 1F);
        }
    }

    public static int angerLevel() {
        if (shrieks<8) {
            return 1;
        } else if (shrieks<13) {
            return 2;
        } else if (shrieks<=19) {
            return 3;
        } else {
            return 4;
        }
    }

    public static ArrayList<BlockPos> findBlock(BlockPos centerBlock, int radius, boolean hollow, World world) {
        int bx = centerBlock.getX();
        int by = centerBlock.getY();
        int bz = centerBlock.getZ();
        ArrayList<BlockPos> candidates = new ArrayList<>();
        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + ((bz - z) * (bz - z)) + ((by - y) * (by - y)));
                    if (distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
                        BlockPos l = new BlockPos(x, y, z);
                        if (l.getY()>world.getBottomY()) {
                            if (verifyWardenSpawn(l, world)) {
                                candidates.add(l);
                            }
                        }
                    }

                }
            }
        }
        return candidates;
    }

    public static boolean verifyWardenSpawn(BlockPos p, World world) {
        if (canSpawn(world, p) && canSpawn(world, p.add(1,0,0)) && canSpawn(world, p.add(1,0,1)) && canSpawn(world, p.add(0,0,1))) {
            return wardenNonCollide(p, world) && wardenNonCollide(p.add(1, 0, 0), world) && wardenNonCollide(p.add(1, 0, 1), world) && wardenNonCollide(p.add(0, 0, 1), world);
        }
        return false;
    }
    public static boolean canSpawn(World world, BlockPos p) {
        return !SculkTags.WARDEN_SPAWNABLE.contains(world.getBlockState(p).getBlock()) && !world.getBlockState(p).isAir() && world.getBlockState(p).getBlock()!=Blocks.WATER &&  world.getBlockState(p).getBlock()!=Blocks.LAVA;
    }
    public static boolean wardenNonCollide(BlockPos p, World world) {
        return SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up()).getBlock()) && SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up(2)).getBlock()) && SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up(3)).getBlock()) && SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up(4)).getBlock());
    }

}
