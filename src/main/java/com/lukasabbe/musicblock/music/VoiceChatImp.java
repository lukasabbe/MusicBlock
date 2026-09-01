package com.lukasabbe.musicblock.music;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class VoiceChatImp implements VoicechatPlugin {

    public static VoicechatServerApi serverApi;
    public static VoicechatApi api;
    public static VolumeCategory music;
    @Override
    public String getPluginId() {
        return "musicblock";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent voicechatServerStartedEvent) {
        VoiceChatImp.serverApi = voicechatServerStartedEvent.getVoicechat();
        music = api.volumeCategoryBuilder()
                .setId("music")
                .setName("Music blocks")
                .setDescription("The music playing when playing music blocks")
                .build();
        VoiceChatImp.serverApi.registerVolumeCategory(music);
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoiceChatImp.api = api;
    }
}
