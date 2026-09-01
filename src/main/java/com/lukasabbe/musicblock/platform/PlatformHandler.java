package com.lukasabbe.musicblock.platform;

import com.lukasabbe.musicblock.Musicblock;
import com.lukasabbe.musicblock.config.Config;
import com.lukasabbe.musicblock.mixin.StructureTemplateAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlatformHandler {

    public static List<Platform> platforms = new ArrayList<>();
    private static Random rnd = new Random();
    private static Platform activePlatform = null;

    public static final int PLATFORM_SIZE = 26;

    public static void init(){
        Path gamePath = FabricLoader.getInstance().getGameDir();
        Path platformsDir = gamePath.resolve("platforms/");
        if(!Files.exists(platformsDir)) createPlatformDir(platformsDir);

        File dir = new File(platformsDir.toUri());
        File[] files = dir.listFiles();
        if (files == null) return;
        for(File file : files){
            StructureTemplate template = getTemplateFromFile(file);
            List<Block> blocks = getAllBlocksInStructure(template);
            platforms.add(new Platform(blocks, template));
        }

    }


    public static Platform spawnRandomPlatform(ServerLevel level){
        Platform platform = platforms.get(rnd.nextInt(0, platforms.size()));
        BlockPos pos = Config.CONFIG.platformPos.getBlockPos();

        StructurePlaceSettings placeData = new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(true);

        platform.template().placeInWorld(level, pos, pos, placeData, level.getRandom(), Block.UPDATE_ALL);
        activePlatform = platform;
        return platform;
    }

    public static Block getRandomColor(){
        return getRandomColor(activePlatform);
    }

    public static Block getRandomColor(Platform platform){
        return platform.blocks().get(rnd.nextInt(0, activePlatform.blocks().size()));
    }

    public static void removeOtherBlocks(ServerLevel level, Block block){
        BlockPos firstPos = Config.CONFIG.platformPos.getBlockPos();
        BlockPos secondPos = firstPos.immutable().offset(PLATFORM_SIZE - 1, 0, PLATFORM_SIZE - 1);
        Iterable<BlockPos> area = BlockPos.betweenClosed(firstPos, secondPos);
        for(BlockPos pos : area){
            if(!level.getBlockState(pos).is(block)){
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static List<Block> getAllBlocksInStructure(StructureTemplate template){
        List<StructureTemplate.Palette> palettes = ((StructureTemplateAccessor)template).getPalettes();

        if(palettes.isEmpty()) return null;
        Set<Block> allBlocks = new HashSet<>();
        for(var palette : palettes){
            List<StructureTemplate.StructureBlockInfo> blockInfos = palette.blocks();
            for(var blockInfo : blockInfos){
                Block block = blockInfo.state().getBlock();
                allBlocks.add(block);
            }
        }
        return allBlocks.stream().toList();

    }

    private static StructureTemplate getTemplateFromFile(File file){
        CompoundTag nbtData = null;
        try(InputStream stream = Files.newInputStream(file.toPath())){
            nbtData = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());

        } catch (RuntimeException | IOException _) {}

        Level level = Musicblock.server.getLevel(Level.OVERWORLD);
        if (level == null) return null;
        if(nbtData == null) return null;
        StructureTemplate template = new StructureTemplate();
        HolderLookup.Provider reg = level.registryAccess();
        HolderLookup<Block> blockHolderLookup = reg.lookupOrThrow(Registries.BLOCK);
        template.load(blockHolderLookup, nbtData);
        return template;
    }

    private static void createPlatformDir(Path platformsDir) {
        try{
            Files.createDirectory(platformsDir);
        } catch (IOException _) {}
    }
}
