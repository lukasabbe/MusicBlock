package com.lukasabbe.musicblock.music;

import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PausableAudioPlayer {
    private final StaticAudioChannel channel;
    private final OpusEncoder encoder;
    private final short[] fullAudio;

    private ScheduledExecutorService executor;
    private int currentPosition = 0;

    private volatile boolean isPaused = false;
    private volatile boolean isPlaying = false;
    private volatile float volume = 0.5f;
    private volatile boolean isStopped = false;

    private final int FRAME_SIZE = 960;

    public PausableAudioPlayer(StaticAudioChannel channel, OpusEncoder encoder, short[] fullAudio) {
        this.channel = channel;
        this.encoder = encoder;
        this.fullAudio = fullAudio;
    }

    public void play() {
        if (isPlaying && !isPaused) return;

        if (isPaused) {
            isPaused = false;
            return;
        }

        isPlaying = true;
        isPaused = false;
        currentPosition = 0;

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {

            if (isPaused) return;

            if (currentPosition >= fullAudio.length) {
                stop();
                return;
            }

            int endPos = Math.min(currentPosition + FRAME_SIZE, fullAudio.length);
            short[] frame = Arrays.copyOfRange(fullAudio, currentPosition, endPos);

            if (frame.length < FRAME_SIZE) {
                short[] paddedFrame = new short[FRAME_SIZE];
                System.arraycopy(frame, 0, paddedFrame, 0, frame.length);
                frame = paddedFrame;
            }

            if(volume < 1.0f){
                for(int i = 0; i<frame.length; i++){
                    frame[i] = (short) (frame[i] * volume);
                }
            }

            byte[] encodedOpusData = encoder.encode(frame);
            channel.send(encodedOpusData);

            currentPosition += FRAME_SIZE;

        }, 0, 20, TimeUnit.MILLISECONDS);
    }

    public void pause() {
        isPaused = true;
    }

    public void stop() {
        if (isStopped) return;
        isStopped = true;
        isPlaying = false;
        isPaused = false;
        currentPosition = 0;

        if (executor != null) {
            executor.shutdown();
        }
        encoder.close();
    }
    public boolean isPaused() {
        return isPaused;
    }
}
