package com.lukasabbe.musicblock.music;

import java.util.ArrayList;
import java.util.List;

public class MetaData {
    public static MetaData data = new MetaData();
    public List<MusicMeta> musicMetas = new ArrayList<>();
    public static void init(){
        data.musicMetas.add(new MusicMeta("template", "template", "template"));
    }
}
