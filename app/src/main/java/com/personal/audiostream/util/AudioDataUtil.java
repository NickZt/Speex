package com.personal.audiostream.util;


import android.util.Log;

import com.personal.speex.SpeexUtil;

/**
 * Created by yanghao1 on 2017/4/19.
 */

public class AudioDataUtil {
    /*When the sampling rate is 8000Hz, it is set to process 160 short data per frame, the compression ratio is 5, and the output per frame is 28 byte data. The features of Speex compression mode are as follows:
     The original text comprehensively considered audio quality, compression ratio and algorithm complexity, and finally chose Mode 5. */

    /*The frame size in hardcoded for this sample code but it doesn't have to be*/
    private static int encFrameSize = 160;
    private static int decFrameSize = 160;
    private static int encodedFrameSize = 28;

    /**
     * 将raw原始音频文件编码为Speex格式
     *
     * @param audioData 原始音频数据
     * @return 编码后的数据
     */
    public static byte[] raw2spx(short[] audioData) {

        // 原始数据中包含的整数个encFrameSize
        int nSamples = audioData.length / encFrameSize;
        byte[] encodedData = new byte[((audioData.length-1) / encFrameSize + 1) * encodedFrameSize];
        short[] rawByte;
        // 将原数据转换成spx压缩的文件
        byte[] encodingData = new byte[encFrameSize];
        int readTotal = 0;
        for (int i = 0; i < nSamples; i++) {
            rawByte = new short[encFrameSize];
            System.arraycopy(audioData, i * encFrameSize, rawByte, 0, encFrameSize);
            int encodeSize = SpeexUtil.init().encode(rawByte, 0, encodingData, rawByte.length);
            Log.e("audio", "raw2spx: "+encodeSize);
            System.arraycopy(encodingData, 0, encodedData, readTotal, encodeSize);
            readTotal += encodeSize;
        }
        Log.e("audio", "raw2spx1: "+readTotal+"=="+audioData.length+"=="+(nSamples * encFrameSize));
        if (audioData.length - nSamples * encFrameSize>0) {
            rawByte = new short[encFrameSize];
            System.arraycopy(audioData, nSamples * encFrameSize, rawByte, 0, audioData.length - nSamples * encFrameSize);
            int encodeSize = SpeexUtil.init().encode(rawByte, 0, encodingData, rawByte.length);
            System.arraycopy(encodingData, 0, encodedData, readTotal, encodeSize);
        }

        Log.e("audio", "raw2spx2: "+readTotal +"=="+encodedData.length);
        return encodedData;

        //byte[] encodedData = new byte[audioData.length / encFrameSize * encodedFrameSize];
        /*byte[] encodedData = new byte[((audioData.length - 1) / encFrameSize + 1) * encodedFrameSize];
        SpeexUtil.init().encode(audioData, 0,encodedData, audioData.length);
        return encodedData;*/
    }

    /**
     * 将Speex编码音频文件解码为raw音频格式
     *
     * @param encodedData 编码音频数据
     * @return 原始音频数据
     */
    public static short[] spx2raw(byte[] encodedData) {
       /* // 原始数据中包含的整数个encFrameSize  644-3854
        //  (3584-1)/160 =22.4  + 1 =23.4  = 23   *  28 = 644
        // 644 / 28 = 23 - 1 = 22*160=3520  3680
     */

        short[] shortRawData = new short[encodedData.length * decFrameSize / encodedFrameSize];
        SpeexUtil.init().decode(encodedData, shortRawData, encodedFrameSize);
        return shortRawData;
    }

    /**
     * 释放音频编解码资源
     */
    public static void free() {
        SpeexUtil.free();
    }
}
