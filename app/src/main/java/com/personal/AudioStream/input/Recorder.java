package com.personal.AudioStream.input;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.personal.AudioStream.constants.PAudioConfig;
import com.personal.AudioStream.constants.PAudioStatus;
import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.InfoBean;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.data.PAudioSettedBean;
import com.personal.AudioStream.input.paudiorecord.PAudioEncoder;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.util.AudioFileUtils;
import com.personal.AudioStream.util.FileIOUtil;
import com.personal.AudioStream.util.TimeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Telephony.MmsSms.PendingMessages.ERROR_TYPE;


/**
 * 音频录制数据格式ENCODING_PCM_16BIT，返回数据类型为short[]
 * 如果调用onCreate的重载方法则使用新的audiorecord配置，酌情保存文件到本地
 *
 * 该类的功能：一 ：配置功能；1是否开启保存PCM文件；2是否显示波形音量；
 *                  后面四个可能不需要了暂时：3是否显示计时；4是否自动删除文件；5是否自动上传文件；6是否暂停继续；
 *             二 ：文件：1设置保存文件路径及名称；2保存文件；3获取当前保存文件路径；4删除文件；5保存成wmv格式的文件
 *             三 : 录音：1录音暂停（只有保存到本地的时候才有录音暂停及拼接）；2录音开始；3录音停止；4录音音频大小；
 *                        5录音长度；6录音创建及结束时间；7录音的创建人；8录音计时；9录音发送(因为是实时的，所以暂不支持录音cancel发送功能)；
 *                        10录音状态；11录音音量；
 */
public class Recorder extends JobHandler {
    private static final String TAG = "Recorder";
    private AudioRecord audioRecord;
    // 音频大小
    private int inAudioBufferSize;
    // 是否正在录音标志
    private boolean isRecording = false;
    //录音状态
    private int audioStatus = PAudioStatus.STATUS_READY;
    //保存PCM元数据的文件名称
    protected String pcmFileName;

    private int currentPosition = 0;
    private PAudioCallBack audioCallBack;

    //录音的音量大小（分贝）
    private int mVolume;
    private static final int MAX_VOLUME = 2000;

    //一些功能性配置：是否保存文件等
    private PAudioSettedBean mSetted = new PAudioSettedBean();

    private InfoBean lastAudioInfo;
    private InfoBean curAudioInfo;
    private long started;

    //编码格式
    private PAudioEncoder pEncoder;

    //录音读写的大小
    private int readSize;
    private AcousticEchoCanceler aec;
    private AutomaticGainControl agc;
    private NoiseSuppressor nc;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Recorder(Handler handler) {
        super(handler);
        onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Recorder(Handler handler, PAudioSettedBean settedBean) {
        super(handler);
        onCreate();
        this.mSetted = settedBean;
    }

    /**
     * 默认初始化AudioRecord等操作
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate() {
        setInAudioBufferSize(0);
        setAudioRecord(null);
    }

    /**
     * 调用该方法：则使用默认设置的AudioRecord进行录音,并创建根据 <username+时间戳> 或 <时间戳> 的形式命名PCM频源的文件
     *
     * @param audioCallBack 扩展用的接口回调
     * @return 1表示初始化成功；0表示初始化失败
     */
    public int onCreate(PAudioCallBack audioCallBack) {
        if (PAudioStatus.STATUS_START == audioStatus) {
            return 0;
        } else {
            this.audioCallBack = audioCallBack;
            return 1;
        }
    }

    /**
     * 调用该方法：则使用用户定义的AudioRecord进行录音,并创建根据 <username+年月日时分秒> 或 <时间戳> 的形式命名PCM音频源的文件
     *
     * @param audioCallBack     扩展用的接口回调
     * @param audioRecord       使用自定义的AudioRecord进行录音
     * @param inAudioBufferSize 使用对应AudioRecord的配置获取音频缓冲大小
     * @return 1表示初始化成功；0表示初始化失败
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public int onCreate(PAudioCallBack audioCallBack, AudioRecord audioRecord, int inAudioBufferSize) {
        if (PAudioStatus.STATUS_START == audioStatus) {
            return 0;
        } else {
            setInAudioBufferSize(inAudioBufferSize);
            setAudioRecord(audioRecord);
            this.audioCallBack = audioCallBack;
            return 1;
        }
    }

    /**
     * 调用该方法：
     * 1开启录音
     * 2是否保存到本地文件，setSaveFilePath()可以设置保存路径，但是录音文件的名称格式固定：目前是pcm格式（可以转为wmv的格式）
     * 3是否开启计时（目前只记录开始时间和结束时间和时长；计时显示在界面）
     * 4是否显示波形音量
     */
    public void onStart() {
        if (audioStatus == PAudioStatus.STATUS_START ) {
            return;
        }
        audioStatus = PAudioStatus.STATUS_START;// 提早，防止init或startRecording被多次调用


        //音频信息记录
        curAudioInfo = new InfoBean();
        //录音开始时间
        curAudioInfo.setCreateAudioTime(System.currentTimeMillis());
        started = curAudioInfo.getCreateAudioTime();
      /*  //创建保存文件
        if (mSetted.isSaved()) {
            createPCMFile();
        }*/


        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            try {
                isRecording = true;
                audioRecord.startRecording();
                Log.e("audio", "onStart: "+isRecording );
            } catch (Exception ex) {
                audioStatus = PAudioStatus.STATUS_ERROR;
                isRecording = false;
                Log.e("audio", "onStart: "+ex.getMessage() );
                ex.printStackTrace();
            }
        }else {
           /* audioStatus = PAudioStatus.STATUS_READY;
            isRecording = false;
            throw new IllegalArgumentException("Audio启动状态异常：" +audioRecord.getRecordingState());*/
        }
    }

    /**
     * 在本线程run方法运行中，所提供的能够在线程运行时调用的方法
     */
    public void onResume() throws Exception {

    }

    /**
     * 调用该方法：停止录音,并保存文件
     */
    public void onStop() {
        if (!(audioStatus == PAudioStatus.STATUS_START
                || audioStatus == PAudioStatus.STATUS_ERROR
                || audioStatus == PAudioStatus.STATUS_FREE)) {
            return;
        }
        /*//todo 如果有暂停功能需要下面的判断，替代上面的内容
        if (audioStatus != PAudioStatus.STATUS_START || audioStatus !=PAudioStatus.STATUS_PAUSE || audioStatus != PAudioStatus.STATUS_ERROR) {
            return;
        }*/
        audioStatus = PAudioStatus.STATUS_STOP;
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentPosition = 0;
        mVolume = 0;

        /*todo 如果是暂停直接使用curAudioInfo；如果是restart需要获取之前的时间和当前的时间计算，
         todo 设置InfoBean里面需要添加一个restart的次数的字段和是否需要删除的字段；
         todo 下面的根据本项目情况可酌情删除
         */
        //录音结束时间及其时长
        long curEndTime = System.currentTimeMillis();
        curAudioInfo.setAudioTimeLength(curAudioInfo.getAudioTimeLength() + (int)((curEndTime - started)/1000));
        curAudioInfo.setEndAudioTime(curEndTime);
        //保存录音信息InfoBean到数据库;todo 暂时用下面的两行替代
        lastAudioInfo = curAudioInfo;
        curAudioInfo = null;
    }

    /**
     * 调用该方法：暂停录音
     */
    public void onPause() {
        if (audioStatus != PAudioStatus.STATUS_START) {
           return;
        }
        audioStatus = PAudioStatus.STATUS_PAUSE;
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //currentPosition = 0;
        mVolume = 0;
        //录音结束时间及其时长
        long curEndTime = System.currentTimeMillis();
        curAudioInfo.setAudioTimeLength(curAudioInfo.getAudioTimeLength() + (int)((curEndTime - started)/1000));
        curAudioInfo.setEndAudioTime(curEndTime);
        //保存录音信息InfoBean到数据库

    }

    /**
     * 调用该方法：重新开始录音
     */
    public void onRestart() {
        if (audioStatus != PAudioStatus.STATUS_PAUSE) {
            return;
        }
        audioStatus = PAudioStatus.STATUS_START;
        getDBInfo();
        started = System.currentTimeMillis();
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            try {
                audioRecord.startRecording();
                isRecording = true;
            } catch (Exception ex) {
                audioStatus = PAudioStatus.STATUS_ERROR;
                isRecording = false;
                ex.printStackTrace();
            }
        }else {
            audioStatus = PAudioStatus.STATUS_READY;
            isRecording = false;
            throw new IllegalArgumentException("Audio重启状态异常：" +audioRecord.getRecordingState());
        }
    }




    @Override
    public void run() {
        Log.e("audio", "run222: "+isRecording );
        while (isRecording) {
            /*try {
                onResume();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            // 实例化音频数据缓冲
            short[] rawData = new short[inAudioBufferSize];
            readSize = audioRecord.read(rawData, 0, inAudioBufferSize);
            Log.e("audio", "recorder: readSize" +readSize
                    +"\ninAudioBufferSize=="+inAudioBufferSize);
            if (readSize >= 0) {
                AudioData audioData = new AudioData(rawData);
                MessageQueue.getInstance(MessageQueue.ENCODER_DATA_QUEUE).put(audioData);
                if (audioCallBack != null && isRecording) {
                    //audioCallBack.recordProgress(++currentPosition);
                    audioCallBack.volumn(calculateRealVolume(rawData, readSize));
                }
               /* //保存PCM到本地文件,觉得应该另开一条线程做这件事情
                if (mSetted.isSaved()) {
                    recordToFile(rawData);
                }*/
            } else {
                Log.e(TAG, "runelse: "+readSize);
                /*isRecording = false;
                audioStatus = PAudioStatus.STATUS_ERROR;*/
               /* onStop();*/
            }
            //MessageQueue.getInstance(MessageQueue.TRACKER_DATA_QUEUE).put(audioData);//测试用，可删
        }
    }

    /**
     * 调用该方法：释放录音等所有可回收资源，是回收本线程之前的准备工作
     */
    @Override
    public void free() {
        // 释放音频录制资源
        audioStatus = PAudioStatus.STATUS_FREE;
        onStop();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        //clearFiles();
    }

    /**
     * 设置audiorecord的初始化配置
     * @param record 外部传入的audiorecord，如果为null则使用默认获取
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setAudioRecord(AudioRecord record) {
        if (record == null) {
            // 初始化音频录制
            audioRecord = new AudioRecord(
                    PAudioConfig.audioSource,
                    PAudioConfig.sampleRateInHz,
                    PAudioConfig.inputChannelConfig,
                    PAudioConfig.audioFormat,
                    inAudioBufferSize);
        } else {
            if (audioRecord != null) {
                try {
                    audioRecord.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioRecord.release();
                audioRecord = null;
            }
            audioRecord = record;
        }
        //声学回声消除器 AcousticEchoCanceler 消除了从远程捕捉到音频信号上的信号的作用
        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (aec != null) {
                aec.setEnabled(true);
            }
        }

        //自动增益控制 AutomaticGainControl 自动恢复正常捕获的信号输出
        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            if (agc != null) {
                agc.setEnabled(true);
            }
        }

        //噪声抑制器 NoiseSuppressor 可以消除被捕获信号的背景噪音
        if (NoiseSuppressor.isAvailable()) {
            nc = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            if (nc != null) {
                nc.setEnabled(true);
            }
        }
    }

    /**
     * 设置缓冲区域大小
     *
     * @param bufferSize 外部传入的缓冲区大小，如果小于等于0则使用默认获取
     */
    private void setInAudioBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            // 获取音频数据缓冲段大小
            inAudioBufferSize = AudioRecord.getMinBufferSize(
                    PAudioConfig.sampleRateInHz,
                    PAudioConfig.inputChannelConfig,
                    PAudioConfig.audioFormat);
        } else {
            inAudioBufferSize = bufferSize;
        }
        //防止因意外出现缓冲段为空的情况
        int frameSize = inAudioBufferSize / PAudioConfig.bytesPerFrame;
        // TODO: 2018/7/16 大小待测 下面的可能不正确
        Log.e(TAG, "onCreate:inAudioBufferSize== " + inAudioBufferSize);
        //四舍五入到给定帧的大小,使能被整除，方便下面的周期性通知
        if (frameSize % PAudioConfig.FRAME_COUNT != 0) {
            frameSize += (PAudioConfig.FRAME_COUNT - frameSize % PAudioConfig.FRAME_COUNT);
            inAudioBufferSize = frameSize * PAudioConfig.bytesPerFrame;
        }
        Log.e(TAG, "onCreate:inAudioBufferSize== " + inAudioBufferSize);
    }

    /**
     * 创建PCM音频文件
     */
    private void createPCMFile() {
        //String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = String.valueOf(curAudioInfo.getCreateAudioTime());
        // TODO: 2018/7/9 下方的userName需要动态获取
        pcmFileName = AudioFileUtils.getPcmFileAbsolutePath("userName" +"&"+ fileName);
        File file = new File(pcmFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 保存PCM音频文件
     */
    private void savePCMFile() {

    }

    /**
     * 设置PCM文件保存的目录
     * @param filePath
     */
    public void setSaveFilePath(File filePath) {

    }

    /**
     * 保存数据信息到数据库
     */
    private void saveDBInfo() {

    }

    /**
     * 获取暂停时，保存到数据库的信息
     */
    private void getDBInfo() {

    }

    /**
     * 清除文件
     */
    public void clearFiles() {
        try {
            File pcmfile = new File(pcmFileName);
            if (pcmfile.exists()) pcmfile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将音频写入文件  todo 不太正确待修改
    private void recordToFile(byte[] rawData) {
        byte[] audiodata = new byte[inAudioBufferSize];
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pcmFileName, true);
            if (readSize <= audiodata.length) fos.write(audiodata, 0, readSize);
        } catch (FileNotFoundException e) {
            Log.e("AudioRecorder", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("AudioRecorder", e.getMessage());
            }
        }
    }

    /**
     * 此计算方法来自samsung开发范例:计算音量
     * @param buffer
     * @param readSize
     */
    private int calculateRealVolume(short[] buffer, int readSize) {
        int sum = 0;
        if (readSize > 0) {
            for (int i = 0; i < readSize; i++) {
                sum += buffer[i] * buffer[i];
            }
            double amplitude = sum / readSize;
           return mVolume = (int) Math.sqrt(amplitude);
        }else {
           return mVolume = 0;
        }
    }

    /**
     * 获取当前位置
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }


    /**
     * 获取当前的录音状态
     * @return
     */
    public int getAudioStatus() {
        return audioStatus;
    }

    /**
     * 获取当前的PCM录音文件的位置；可能为空
     * @return
     */
    public String getVoiceFilePath() {
        return pcmFileName;
    }

    /**
     * 获取真实的音量。 [算法来自三星]
     * @return 真实音量
     */
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     * @return 最大音量值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    /**
     * 是否在录音：获取状态
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 录音扩展接口
     */
    public interface PAudioCallBack {
        /**
         * 录音记录的进程
         *
         * @param progress
         */
        //void recordProgress(int progress);

        /**
         * 音量大小
         *
         * @param volumn
         */
        void volumn(int volumn);
    }

}
