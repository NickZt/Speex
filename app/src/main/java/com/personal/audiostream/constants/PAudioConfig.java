package com.personal.audiostream.constants;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * 录音配置类
 * Created by personal on 2018/7/9.
 */

public class PAudioConfig {
    /**
     * 音频采集端
     */
    // Sampling frequency
//     public static final int SAMPLE_RATE_IN_HZ = 44100;
    public static final int SAMPLE_RATE_IN_HZ = 8000;

    // Audio data format:PCM 16Bit per sample，Guarantee equipment support。
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Audio source MIC: Microphone VOICE_CALL: Set the recording source to be the voice dialed by the voice and the voice of the other party VOICE_COMMUNICATION: The microphone next to the camera
    //public static final int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    // Input mono
    public static final int INPUT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    //Audio data format:PCM 16Bit per sample，Guarantee equipment support；Two frames per sample
    public static final int BYTES_PER_FRAME = 2;

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    public static final int FRAME_COUNT = 160;

    /**
     * Audio player
     */
    // Audio player STREAM_VOICE_CALL: Voice call sound STREAM_MUSIC: Mobile phone music sound
    // For VoIP applications, use STREAM_VOICE_CALL, and for streaming music applications, use STREAM_MUSIC
    //public static final int streamType = AudioManager.STREAM_VOICE_CALL;
    public static final int streamType = AudioManager.STREAM_MUSIC;

    // 输出单声道
    public static final int outputChannelConfig = AudioFormat.CHANNEL_OUT_MONO;

    // 音频输出模式: send 数据流
    public static final int trackMode = AudioTrack.MODE_STREAM;

}
