#include <jni.h>
#include <string>
#include <unistd.h>
#include <stdio.h>


/*
    在C语言中标准输出的方法是printf，但是打印出来的内容在logcat看不到，需要使用
    __android_log_print()方法打印log，才能在logcat看到，由于该方法名比较长，我们在
    这里需要定义宏，使得在C语言中能够向Android一样打印log。
    注意：该方法还需要在gradle中声明ldLibs "log"，详见build.gradle
*/
#include <android/log.h>
#include <speex/speex_bits.h>
#include <speex/speex.h>


#define TAG "Voice_*C*_Speex" // LOG标志
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // LOGD DEBUG
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // LOGI
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // LOGW
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // LOGE
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // LOGF

/* 只有返回1的时候表明走了程序，大于1的情况表明正在运行，为0表明结束或未开始 */
static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;// 编码SpeexBits变量，解码SpeexBits变量
void *enc_state;// 编码器状态
void *dec_state;// 解码器状态

static JavaVM *gJavaVM;// Java虚拟机

/*本jni只有普通的编码功能，无降噪等*/

extern "C"
JNIEXPORT jint

JNICALL
Java_com_personal_speex_SpeexUtil_open(
        JNIEnv *env,
        jobject /* this */,jint compression) {
    int tmp;
    // 不能重复Open
    if (codec_open++ != 0)
        return (jint)codec_open;

    // 初始化SpeexBits数据结构
    speex_bits_init(&ebits);
    speex_bits_init(&dbits);
    /*
     * speex_nb_mode:窄带模式
     * speex_wb_mode:宽带模式
     * speex_uwb_mode:超宽带模式
     // 设置编码为窄带编码,初始化编码器
     */
    enc_state = speex_encoder_init(&speex_nb_mode);
    dec_state = speex_decoder_init(&speex_nb_mode);

    // 压缩比例
    tmp = compression;
    // 设置压缩质量(0~10)
    speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
    // 设置编码的比特率，即语音质量。由参数tmp控制;设置编码器音频帧大小
    speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
    speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);
    return (jint)codec_open;
}


extern "C"
JNIEXPORT jint

JNICALL
Java_com_personal_speex_SpeexUtil_getFrameSize(
        JNIEnv *env,
        jobject /* this */) {
    if (!codec_open)
        return 0;
    return (jint)enc_frame_size;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_com_personal_speex_SpeexUtil_decode(
        JNIEnv *env,
        jobject /* this */, jbyteArray encoded, jshortArray lin, jint size) {
    jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];

    /*int length = env->GetArrayLength(encoded);
    int nSamples = length / size;
    int i = 0;
    if (!codec_open)
        return 0;
    for(i = 0; i < nSamples; i++) {
        // 从Java中拷贝数据到C中，size = 28个字节
        env->GetByteArrayRegion(encoded, i * size, size, buffer);
        // 编码数据到dbits中
        speex_bits_read_from(&dbits, (char *)buffer, size);
        // 解码到output_buffer，28个字节到160个short
        speex_decode_int(dec_state, &dbits, output_buffer);
        // 将C层的short类型数据写入Java层的short数组中
       // speex_preprocess_run(preprocess_state, output_buffer);
        env->SetShortArrayRegion(lin, i * dec_frame_size, dec_frame_size, output_buffer);
    }
    LOGD("########## output_buffer = %d", nSamples);
    LOGD("########## output_buffer = %d", output_buffer);
    return (jint)nSamples;*/


   /* jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];*/
    jsize encoded_length = size;
    if (!codec_open)
        return 0;
    LOGD("########## encoded_length = %d", encoded_length);
    LOGD("########## dec_frame_size = %d", dec_frame_size);
    speex_bits_reset(&dbits);

    for (int i = 0; i < encoded_length; i++)
    {
        env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
        LOGD("########## buffer = %c", buffer[i]);
    }

    speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
    speex_decode_int(dec_state, &dbits, output_buffer);
    env->SetShortArrayRegion(lin, 0, dec_frame_size,
                             output_buffer);

    return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_com_personal_speex_SpeexUtil_encode(
        JNIEnv *env,
        jobject /* this */, jshortArray lin, jint offset, jbyteArray encoded, jint size) {
   /* jshort buffer[enc_frame_size];
    jbyte output_buffer[enc_frame_size];
    int nSamples = size / enc_frame_size;
    int i, tot_bytes, curr_bytes = 0;

    if (!codec_open)
        return 0;

    for (i = 0; i < nSamples; i++) {
        // 从Java中拷贝数据到C中，每次拷贝enc_frame_size = 160个short
        env->GetShortArrayRegion(lin, i*enc_frame_size, enc_frame_size, buffer);
        // 降噪、增益、静音检测等处理
        *//*speex_preprocess_run(preprocess_state, buffer);*//*
        // 编码数据到ebits中
        speex_bits_reset(&ebits);
        speex_encode_int(enc_state, buffer, &ebits);
        // 将编码数据写入output_buffer，每次最多写入enc_frame_size = 160个，实际写入curr_bytes个char
        curr_bytes = speex_bits_write(&ebits, (char *)output_buffer, enc_frame_size);
        // 将C层的char类型数据写入Java层的字节数组中，开始写入index为tot_bytes，本次写入curr_bytes
        env->SetByteArrayRegion(encoded, tot_bytes, curr_bytes, output_buffer);
        // 更新总数
        tot_bytes += curr_bytes;
    }
    return (jint)tot_bytes;*/

    jshort buffer[enc_frame_size];
    jbyte output_buffer[enc_frame_size];
    int nsamples = (size-1)/enc_frame_size + 1;
    int i, tot_bytes = 0;
    if (!codec_open)
        return 0;
    speex_bits_reset(&ebits);
    for (i = 0; i < nsamples; i++) {
        env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
        speex_encode_int(enc_state, buffer, &ebits);
    }
    tot_bytes = speex_bits_write(&ebits, (char *)output_buffer, enc_frame_size);
    env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);
    return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_com_personal_speex_SpeexUtil_close(
        JNIEnv *env,
        jobject /* this */) {

    if (--codec_open != 0)		// 如果没有开启过，则返回0
        return 0;
    speex_bits_destroy(&ebits);	// 回收空间
    speex_bits_destroy(&dbits);
    speex_decoder_destroy(dec_state);
    speex_encoder_destroy(enc_state);

    return 1;
}
