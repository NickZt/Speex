package com.personal.audiostream.output;

import android.media.AudioTrack;
import android.os.Handler;

import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.constants.PAudioConfig;


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
                PAudioConfig.SAMPLE_RATE_IN_HZ,
                PAudioConfig.outputChannelConfig,
                PAudioConfig.AUDIO_FORMAT);
        //  Related 音频播放
        audioTrack = new AudioTrack(
                PAudioConfig.streamType,
                PAudioConfig.SAMPLE_RATE_IN_HZ,
                PAudioConfig.outputChannelConfig,
                PAudioConfig.AUDIO_FORMAT,
                outAudioBufferSize,
                PAudioConfig.trackMode);
        //在音轨上设置指定的左和右输出值。
        audioTrack.setStereoVolume(
                AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());
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
//                Log.e("audio", "Tracker: "+bytesPkg.length );
//                Log.e("audio", "Tracker===\n "+ Arrays.toString(bytesPkg) );
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
