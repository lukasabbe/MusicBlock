package com.lukasabbe.musicblock.platform;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public record Platform(List<Block> blocks, StructureTemplate template) {
}
