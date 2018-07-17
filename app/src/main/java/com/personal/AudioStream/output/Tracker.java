package com.personal.AudioStream.output;

import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.constants.PAudioConfig;

import java.util.Arrays;


/**
 * AudioTrack音频播放
 *
 * @author yanghao1
 */
public class Tracker extends JobHandler {

    private AudioTrack audioTrack;
    // 音频大小
    private int outAudioBufferSize;
    // 播放标志
    private boolean isPlaying = true;

    public Tracker(Handler handler) {
        super(handler);
        // 获取音频数据缓冲段大小
        outAudioBufferSize = AudioTrack.getMinBufferSize(
                PAudioConfig.sampleRateInHz, PAudioConfig.outputChannelConfig, PAudioConfig.audioFormat);
        // 初始化音频播放
        audioTrack = new AudioTrack(PAudioConfig.streamType,
                PAudioConfig.sampleRateInHz, PAudioConfig.outputChannelConfig, PAudioConfig.audioFormat,
                outAudioBufferSize, PAudioConfig.trackMode);
        audioTrack.play();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public void run() {
        AudioData audioData;
        while ((audioData = MessageQueue.getInstance(MessageQueue.TRACKER_DATA_QUEUE).take()) != null) {
            if (isPlaying()) {
                short[] bytesPkg = audioData.getRawData();
                Log.e("audio", "Tracker: "+bytesPkg.length );
                Log.e("audio", "Tracker===\n "+ Arrays.toString(bytesPkg) );
                try {
                    audioTrack.write(bytesPkg, 0, bytesPkg.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void free() {
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
    }
}
