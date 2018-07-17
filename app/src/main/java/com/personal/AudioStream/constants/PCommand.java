package com.personal.AudioStream.constants;

/**
 * 指令配置类
 * Created by personal on 2017/4/13.
 */

public class PCommand {

    // Service向Activity发送的跨进程指令
    public static final String DISC_REQUEST = "DISC_REQUEST";
    public static final String DISC_RESPONSE = "DISC_RESPONSE";
    public static final String DISC_LEAVE = "DISC_LEAVE";

    // Activity向Service发送的跨进程指令
    public static final String START_FOREGROUND_ACTION = "com.jd.wly.intercom.action.start";
    public static final String STOP_FOREGROUND_ACTION = "com.jd.wly.intercom.action.stop";
    // 前台Service
    public static final String MAIN_ACTION = "com.jd.wly.intercom.action.main";
    public static final int FOREGROUND_SERVICE = 101;


    //默认级别标志
    public static final int DEF_FLAG_LEVEL = 0;
    //单播级别标志
    public static final int UNI_FLAG_PER_LEVEL = 1;
    //组播组内级别标志
    public static final int MULTI_FLAG_GROUP_LEVEL = 2;
    //组播全部级别标志
    public static final int MULTI_FLAG_ALL_LEVEL = 3;

    //搜索：发送信息标志
    public static final int DISCOVERING_SEND = 101;
    //搜索：接收信息标志
    public static final int DISCOVERING_RECEIVE = 102;
    //搜索：离开信息标志
    public static final int DISCOVERING_LEAVE = 103;
}
