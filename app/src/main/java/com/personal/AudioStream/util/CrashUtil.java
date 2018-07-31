package com.personal.AudioStream.util;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.personal.App;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *     desc  : 崩溃相关工具类
 */
public final class CrashUtil {

    private static String defaultDir;
    private static String dir;
    private static String versionName;
    private static int    versionCode;

    private static ExecutorService sExecutor;

    private static final String FILE_SEP = System.getProperty("file.separator");
    // 用于格式化日期,作为日志文件名的一部分
    @SuppressLint("SimpleDateFormat")
    private static final Format FORMAT   = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    private static final String CRASH_HEAD;

    /** CrashUtil实例 */
    private static CrashUtil INSTANCE;

    // 系统默认的UncaughtException处理类
    private static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER;
    private static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER;

    private static OnCrashListener sOnCrashListener;

    static {
        try {
            PackageInfo pi = App.getInstance()
                    .getPackageManager()
                    .getPackageInfo(App.getInstance().getPackageName(), 0);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        CRASH_HEAD = "************* Crash Log Head ****************" +
                "\nDevice Manufacturer: " + Build.MANUFACTURER +// 设备厂商
                "\nDevice Model       : " + Build.MODEL +// 设备型号
                "\nAndroid Version    : " + Build.VERSION.RELEASE +// 系统版本
                "\nAndroid SDK        : " + Build.VERSION.SDK_INT +// SDK 版本
                "\n******************************************************"+
                "\nApp VersionName    : " + versionName +
                "\nApp VersionCode    : " + versionCode +
                "\n************* Crash Log Head ****************\n\n";

        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

        UNCAUGHT_EXCEPTION_HANDLER = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                if (e == null) {
                    if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, null);
                    } else {
                        //SystemClock.sleep(3000);
                        // 退出程序
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                    return;
                }
                if (sOnCrashListener != null) {
                    sOnCrashListener.onCrash(e);
                }
                //if (saveCrashInfoFile(e)) return;
                if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                    // 如果用户没有处理则让系统默认的异常处理器来处理
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, e);
                }
            }
        };
    }

    /**
     *
     * @param e
     * @return true 保存成功   false 保存失败
     */
    public static boolean saveCrashInfoFile(final Throwable e) {
        Date now = new Date(System.currentTimeMillis());
        String fileName =  FORMAT.format(now) + ".txt";
        final String fullPath = (dir == null ? defaultDir : dir) + fileName;
        if (!createOrExistsFile(fullPath)) {
            TUtil.showShort("Crash文件保存目录创建失败！");
            return false;
        }
        LogUtil.e(fullPath);//查看crash存放的路径
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new FileWriter(fullPath, false));
                    pw.write(CRASH_HEAD);
                    e.printStackTrace(pw);
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        cause.printStackTrace(pw);
                        cause = cause.getCause();
                    }
                    SystemClock.sleep(5000);//模拟测试用睡眠
                } catch (IOException e) {
                    TUtil.showShort("Crash文件读写失败！");
                    LogUtil.e("Crash文件读写失败！\n"+e);
                    e.printStackTrace();
                } finally {
                    if (pw != null) {
                        pw.close();
                    }
                }
            }
        });
        return true;
    }

    private CrashUtil() {
        throw new UnsupportedOperationException("CrashUtil实例化失败！");
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     */
    public static void init() {
        init("");
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     *
     * @param crashDir 崩溃文件存储目录
     */
    public static void init(@NonNull final File crashDir) {
        init(crashDir.getAbsolutePath(), null);
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     *
     * @param crashDir 崩溃文件存储目录
     */
    public static void init(final String crashDir) {
        init(crashDir, null);
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     *
     * @param onCrashListener 崩溃监听事件
     */
    public static void init(final OnCrashListener onCrashListener) {
        init("", onCrashListener);
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     *
     * @param crashDir        崩溃文件存储目录
     * @param onCrashListener 崩溃监听事件
     */
    public static void init(@NonNull final File crashDir, final OnCrashListener onCrashListener) {
        init(crashDir.getAbsolutePath(), onCrashListener);
    }

    /**
     * 初始化
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}</p>
     *
     * @param crashDir        崩溃文件存储目录
     * @param onCrashListener 崩溃监听事件
     */
    public static void init(final String crashDir, final OnCrashListener onCrashListener) {
        if (isSpace(crashDir)) {
            dir = null;
        } else {
            dir = crashDir.endsWith(FILE_SEP) ? crashDir : crashDir + FILE_SEP;
        }
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && App.getInstance().getExternalCacheDir() != null)
            defaultDir = App.getInstance().getExternalCacheDir() + FILE_SEP + "crash" + FILE_SEP;
        else {
            defaultDir = App.getInstance().getCacheDir() + FILE_SEP + "crash" + FILE_SEP;
        }
        sOnCrashListener = onCrashListener;
        Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
    }

    private static boolean createOrExistsFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

/*    *//** 获取CrashUtil实例 *//*
    public static CrashUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (CrashUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE =  new CrashUtil();
                }
            }
        }
        return INSTANCE;
    }*/

    /**
     * 文件删除
//     * @param autoClearDay 文件保存天数
     */
  /*  public void autoClear(final int autoClearDay) {
        FileUtil.delete(getGlobalpath(), new FilenameFilter() {

            @Override
            public boolean accept(File file, String filename) {
                String s = FileUtil.getFileNameWithoutExtension(filename);
                int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                String date = "crash-" + DateUtil.getOtherDay(day);
                return date.compareTo(s) >= 0;
            }
        });
    }*/

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public interface OnCrashListener {
        void onCrash(Throwable e);
    }
}
