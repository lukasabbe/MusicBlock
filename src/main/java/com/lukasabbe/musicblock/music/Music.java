package com.lukasabbe.musicblock.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Music {
    public short[] musicSample;

    public Music(File mp3) {
        try{
            var decoder = VoiceChatImp.api.createMp3Decoder(new FileInputStream(mp3));
            if(decoder == null) return;
            this.musicSample = decoder.decode();
            if(decoder.getAudioFormat().getChannels() == 2){
                this.musicSample = downmixStereoToMono(this.musicSample);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private short[] downmixStereoToMono(short[] stereoSamples){
        short[] monoSamples = new short[stereoSamples.length / 2];
        for (int i = 0; i < monoSamples.length; i++) {
            int left = stereoSamples[i * 2];
            int right = stereoSamples[i * 2 + 1];
            monoSamples[i] = (short) ((left + right) / 2);
        }
        return monoSamples;
    }
}
