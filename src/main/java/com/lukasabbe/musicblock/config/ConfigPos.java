package com.lukasabbe.musicblock.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ConfigPos {
    public double x;
    public double y;
    public double z;

    public ConfigPos(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public BlockPos getBlockPos(){
        return new BlockPos((int) x, (int) y, (int) z);
    }
    public Vec3 getVec3(){
        return new Vec3(x, y, z);
    }
}
