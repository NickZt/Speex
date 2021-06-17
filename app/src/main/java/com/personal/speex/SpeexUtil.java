package com.personal.speex;

/**
 * Created by 山东御银智慧 on 2018/6/28.
 */

public class SpeexUtil {
    // Used to load the 'native-lib' library on application startup.
    static {
        try {
            System.loadLibrary("speex_jni");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public final static String TAG = "SPEEXUTIL";
     /* quality
     * 1 : 4kbps (very noticeable artifacts, usually intelligible)
     * 2 : 6kbps (very noticeable artifacts, good intelligibility)
     * 4 : 8kbps (noticeable artifacts sometimes)
     * 6 : 11kpbs (artifacts usually only noticeable with headphones)
     * 8 : 15kbps (artifacts not usually noticeable)
     */
//    todo change this magic number and calc to length buffer
    public static final int DEFAULT_COMPRESSION = 5;
    private static volatile SpeexUtil INSTANCE = null;

    private SpeexUtil() {
        open(DEFAULT_COMPRESSION);
    }

    /**
     *单例 Related
     * @return  返回SpeexUtil对象
     */
    public static SpeexUtil init() {
        if (INSTANCE == null) {
            synchronized (SpeexUtil.class){
                if (INSTANCE == null) {
                    INSTANCE = new SpeexUtil();
                }
            }
        }
        return INSTANCE;
    }

    /**
     *Turn off encoding and release SpeexUtil
     * @return -1 Indicates that the default value INSTANCE is empty, 0 means that it has not been opened, 1 means that it is closed and released
     */
    public static int free(){
        int flag = -1;
        if (INSTANCE != null) {
            flag = INSTANCE.close();
            INSTANCE = null;
        }
        return flag;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native int open(int compression);
    public native int getFrameSize();
    public native int decode(byte encoded[], short lin[], int size);
    public native int encode(short lin[], int offset, byte encoded[], int size);
    public native int close();
}
