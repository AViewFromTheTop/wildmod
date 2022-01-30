package frozenblock.wild.mod.registry;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.blocks.*;
import frozenblock.wild.mod.items.FrogSpawnItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public abstract class RegisterBlocks {

    public static final AbstractBlock.Settings DEEPSLATE_FRAME_SETTINGS = FabricBlockSettings
            .of(Material.STONE, MapColor.DEEPSLATE_GRAY)
            .requiresTool()
            .strength(3.0F, 6.0F)
            .sounds(new BlockSoundGroup(1.0f, 1.0f,
                    RegisterSounds.BLOCK_DEEPSLATE_FRAME_BREAK,
                    RegisterSounds.BLOCK_DEEPSLATE_FRAME_STEP,
                    RegisterSounds.BLOCK_DEEPSLATE_FRAME_PLACE,
                    RegisterSounds.BLOCK_DEEPSLATE_FRAME_HIT,
                    RegisterSounds.BLOCK_DEEPSLATE_FRAME_STEP
            ));

    public static final AbstractBlock.Settings FROG_SPAWN_PROPERTIES = FabricBlockSettings
            .of(Material.EGG)
            .sounds(new BlockSoundGroup(1.0f, 1.5f,
                    SoundEvents.ENTITY_TURTLE_EGG_BREAK,
                    SoundEvents.BLOCK_METAL_STEP,
                    SoundEvents.ENTITY_TURTLE_LAY_EGG,
                    SoundEvents.BLOCK_METAL_STEP,
                    SoundEvents.BLOCK_METAL_STEP
            ));

    public static final FabricBlockSettings MUD_BRICKS_SETTINGS = FabricBlockSettings
            .of(Material.STONE)
            .strength(0.5f, 1.0f)
            .requiresTool()
            .sounds(new BlockSoundGroup(1.0f, 1.1f,
                    RegisterSounds.BLOCK_MUD_BRICKS_BREAK,
                    RegisterSounds.BLOCK_MUD_BRICKS_STEP,
                    RegisterSounds.BLOCK_MUD_BRICKS_PLACE,
                    RegisterSounds.BLOCK_MUD_BRICKS_HIT,
                    RegisterSounds.BLOCK_MUD_BRICKS_STEP
            ));

    public static final FabricBlockSettings FROGLIGHT_SETTINGS = FabricBlockSettings
            .of(Material.PLANT)
            .strength(0.1f, 1f)
            .requiresTool()
            .luminance(15);

    public static final Block DEEPSLATE_FRAME = new PillarBlock(DEEPSLATE_FRAME_SETTINGS);

    public static final Block MUD_BLOCK = new MudBlock();

    public static final Block MUD_BRICKS = new MudBricks(MUD_BRICKS_SETTINGS);
    public static final WallBlock MUD_BRICKS_WALL = new MudBricksWall(MUD_BRICKS_SETTINGS);
    public static final SlabBlock MUD_BRICKS_SLAB = new MudBricksSlab(MUD_BRICKS_SETTINGS);
    public static final StairsBlock MUD_BRICKS_STAIRS = new CustomStairs(Blocks.OAK_STAIRS.getDefaultState(), MUD_BRICKS_SETTINGS);

    public static final Block SCULK_CATALYST = SculkCatalystBlock.SCULK_CATALYST_BLOCK;

    public static final Block SCULK = SculkBlock.SCULK_BLOCK;

    public static final Block OCHRE_FROGLIGHT = new PillarBlock(FROGLIGHT_SETTINGS);
    public static final Block PEARLESCENT_FROGLIGHT = new PillarBlock(FROGLIGHT_SETTINGS);
    public static final Block VERDANT_FROGLIGHT = new PillarBlock(FROGLIGHT_SETTINGS);


    // ALL BLOCKS HERE HAVE NO COLLISION
    public static final Block FROG_SPAWN = new FrogSpawnBlock(FROG_SPAWN_PROPERTIES.nonOpaque().noCollision());
    public static final Block SCULK_VEIN = SculkVeinBlock.SCULK_VEIN;

    public static void RegisterBlocks() {

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "mud_block"), MUD_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "mud_block"), new BlockItem(MUD_BLOCK, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "mud_bricks"), MUD_BRICKS);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "mud_bricks"), new BlockItem(MUD_BRICKS, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "mud_brick_wall"), MUD_BRICKS_WALL);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "mud_brick_wall"), new BlockItem(MUD_BRICKS_WALL, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "mud_brick_slab"), MUD_BRICKS_SLAB);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "mud_brick_slab"), new BlockItem(MUD_BRICKS_SLAB, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "mud_brick_stairs"), MUD_BRICKS_STAIRS);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "mud_brick_stairs"), new BlockItem(MUD_BRICKS_STAIRS, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "sculk"), SCULK);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "sculk"), new BlockItem(SCULK, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "sculk_vein"), SCULK_VEIN);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "sculk_vein"), new BlockItem(SCULK_VEIN, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "sculk_catalyst"), SCULK_CATALYST);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "sculk_catalyst"), new BlockItem(SCULK_CATALYST, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "sculk_shrieker"), SculkShriekerBlock.SCULK_SHRIEKER_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "sculk_shrieker"), new BlockItem( SculkShriekerBlock.SCULK_SHRIEKER_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "sculk_sensor"), new BlockItem(Blocks.SCULK_SENSOR, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "deepslate_frame"), DEEPSLATE_FRAME);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "deepslate_frame"), new BlockItem(DEEPSLATE_FRAME, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "frog_spawn"), FROG_SPAWN);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "frog_spawn"), new FrogSpawnItem(FROG_SPAWN, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "pearlescent_froglight"), PEARLESCENT_FROGLIGHT);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "pearlescent_froglight"), new BlockItem(PEARLESCENT_FROGLIGHT, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "verdant_froglight"), VERDANT_FROGLIGHT);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "verdant_froglight"), new BlockItem(VERDANT_FROGLIGHT, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, new Identifier(WildMod.MOD_ID, "ochre_froglight"), OCHRE_FROGLIGHT);
        Registry.register(Registry.ITEM, new Identifier(WildMod.MOD_ID, "ochre_froglight"), new BlockItem(OCHRE_FROGLIGHT, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        MangroveWoods.RegisterMangrove();
    }
}
