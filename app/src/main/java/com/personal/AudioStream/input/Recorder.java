package com.personal.AudioStream.input;

import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.output.Tracker;
import com.personal.AudioStream.util.Constants;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 音频录制数据格式ENCODING_PCM_16BIT，返回数据类型为short[]
 *
 */
public class Recorder extends JobHandler {
    private AudioRecord audioRecord;
    // 音频大小
    private int inAudioBufferSize;
    // 录音标志
    private boolean isRecording = false;
    private Tracker tracker;

    public Recorder(Handler handler) {
        super(handler);
        // 获取音频数据缓冲段大小
        inAudioBufferSize = AudioRecord.getMinBufferSize(
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat);
        // 初始化音频录制
        audioRecord = new AudioRecord(Constants.audioSource,
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat, inAudioBufferSize);
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }
    @Override
    public void run() {
        while (isRecording) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                audioRecord.startRecording();
            }
            // 实例化音频数据缓冲
            short[] rawData = new short[inAudioBufferSize];
            audioRecord.read(rawData, 0, inAudioBufferSize);
            AudioData audioData = new AudioData(rawData);
           MessageQueue.getInstance(MessageQueue.ENCODER_DATA_QUEUE).put(audioData);
            Log.e("audio", "recorder: "+audioData.getRawData().length);
            Log.e("audio", "recorder===\n "+ Arrays.toString(audioData.getRawData()));
            //MessageQueue.getInstance(MessageQueue.TRACKER_DATA_QUEUE).put(audioData);
        }
    }

    @Override
    public void free() {
        // 释放音频录制资源
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }
}
