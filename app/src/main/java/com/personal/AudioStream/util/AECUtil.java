package com.personal.AudioStream.util;

import android.media.audiofx.AcousticEchoCanceler;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by personal on 2017/4/18.
 * 回音消除等
 */

public class AECUtil {

    private static AcousticEchoCanceler canceler;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isDeviceSupport() {
        return AcousticEchoCanceler.isAvailable();
    }

    public static boolean setAECEnabled(boolean enable) {
        if (null == canceler) {
            return false;
        }
        canceler.setEnabled(enable);
        return canceler.getEnabled();
    }

    public static boolean release() {
        if (null == canceler) {
            return false;
        }
        canceler.setEnabled(false);
        canceler.release();
        canceler = null;
        return true;
    }
}
