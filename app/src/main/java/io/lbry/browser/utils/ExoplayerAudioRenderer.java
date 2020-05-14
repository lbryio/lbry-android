package io.lbry.browser.utils;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.TeeAudioProcessor;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.util.ArrayList;

public class ExoplayerAudioRenderer extends DefaultRenderersFactory {

    private TeeAudioProcessor.AudioBufferSink audioBufferSink;

    public ExoplayerAudioRenderer(Context context, TeeAudioProcessor.AudioBufferSink audioBufferSink) {
        super(context);
        this.audioBufferSink = audioBufferSink;
    }

    @Override
    protected void buildAudioRenderers(
            Context context,
            int extensionRendererMode,
            MediaCodecSelector mediaCodecSelector,
            @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
            boolean playClearSamplesWithoutKeys,
            boolean enableDecoderFallback,
            AudioProcessor[] audioProcessors,
            Handler eventHandler,
            AudioRendererEventListener eventListener,
            ArrayList<Renderer> out) {
        AudioProcessor[] audioProcessorList = { new TeeAudioProcessor(audioBufferSink) };
        super.buildAudioRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                drmSessionManager,
                playClearSamplesWithoutKeys,
                enableDecoderFallback,
                audioProcessorList,
                eventHandler,
                eventListener,
                out);
    }
}
