package com.personal.audiostream.constants;

/**
 * 录音状态 配置类
 * Created by 山东御银智慧 on 2018/7/9.
 */

/**
 * 录音的状态
 */
public class PAudioStatus {
    public static final int STATUS_DEFAULT_READY            = 0x61000;        //默认准备
    public static final int STATUS_USER_DEFINED_READY1      = 0x61001;       //group name定义准备1
    public static final int STATUS_USER_DEFINED_READY2      = 0x61002;       //group name定义准备2


    public static final int STATUS_READY                      = 0x61003;       //准备录音状态
    public static final int STATUS_START                      = 0x61004;       //开始录音状态
    public static final int STATUS_PAUSE                      = 0x61005;       //暂停录音状态
    public static final int STATUS_STOP                       = 0x61006;      //Stop recording状态
    public static final int STATUS_FREE                       = 0x61007;      //释放录音对象
    public static final int STATUS_ERROR                       = 0x61008;      //状态异常
}
