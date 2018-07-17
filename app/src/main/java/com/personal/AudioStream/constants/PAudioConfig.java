package com.personal.AudioStream.constants;

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
    // 采样频率
    // public static final int sampleRateInHz = 44100;
    public static final int sampleRateInHz = 8000;

    // 音频数据格式:PCM 16位每个样本，保证设备支持。
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    // 音频获取源        MIC:麦克风    VOICE_CALL：设定录音来源为语音拨出的语音与对方说话的声音  VOICE_COMMUNICATION：摄像头旁边的麦克风
    //public static final int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public static final int audioSource = MediaRecorder.AudioSource.MIC;

    // 输入单声道
    public static final int inputChannelConfig = AudioFormat.CHANNEL_IN_MONO;

    //音频数据格式:PCM 16位每个样本，保证设备支持；每个样本两帧
    public static final int  bytesPerFrame = 2;

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    public static final int FRAME_COUNT = 160;

    /**
     * 音频播放端
     */
    // 音频播放端        STREAM_VOICE_CALL：语音电话声音        STREAM_MUSIC：手机音乐的声音
    // 对VoIP应用来说，使用STREAM_VOICE_CALL，而对于流媒体音乐应用则使用STREAM_MUSIC
    //public static final int streamType = AudioManager.STREAM_VOICE_CALL;
    public static final int streamType = AudioManager.STREAM_MUSIC;

    // 输出单声道
    public static final int outputChannelConfig = AudioFormat.CHANNEL_OUT_MONO;

    // 音频输出模式:发送数据流
    public static final int trackMode = AudioTrack.MODE_STREAM;

}
