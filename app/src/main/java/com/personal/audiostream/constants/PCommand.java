package com.personal.audiostream.constants;

import com.personal.audiostream.util.IPUtil;
import com.personal.audiostream.util.SPUtil;
import com.personal.speex.IntercomUserBean;

/**
 * 指令配置类
 * Created by personal on 2017/4/13.
 */

public class PCommand {

    // Service向Activity send 的跨进程指令
    public static final String DISC_REQUEST = "DISC_REQUEST";
    public static final String DISC_REQUEST_TWO = "DISC_REQUEST_TWO";
    public static final String DISC_RESPONSE = "DISC_RESPONSE";
    public static final String DISC_LEAVE = "DISC_LEAVE";

    // Activity向Service send 的跨进程指令
    public static final String START_FOREGROUND_ACTION = "com.jd.wly.intercom.action.start";
    public static final String STOP_FOREGROUND_ACTION = "com.jd.wly.intercom.action.stop";
    // 前台Service
    public static final String MAIN_ACTION = "com.jd.wly.intercom.action.main";
    public static final int FOREGROUND_SERVICE = 100;


    //默认 level 标志
    public static final int DEF_FLAG_LEVEL = 0;
    //单播 level 标志
    public static final int UNI_FLAG_PER_LEVEL = 1;
    //组播组内 level 标志
    public static final int MULTI_FLAG_GROUP_LEVEL = 2;
    //组播 All  level 标志
    public static final int MULTI_FLAG_ALL_LEVEL = 3;

    // search for ： send 信息标志
    public static final int DISCOVERING_SEND = 101;
    // search for ：接收信息标志
    public static final int DISCOVERING_RECEIVE = 102;
    // search for ：离开信息标志
    public static final int DISCOVERING_LEAVE = 103;

    //开始通话标志
    public static final int DISCOVERING_START_SINGLE = 111;//One-to-one
    public static final int DISCOVERING_START_SINGLE_SUCCESS = 104;//One-to-one Successful response
    public static final int DISCOVERING_START_SINGLE_REFUSE = 105;//One-to-one The other party is talking again
    public static final int DISCOVERING_START_GROUP = 106;//组内
    public static final int DISCOVERING_START_ALL= 107;// All
    //the end通话标志
    public static final int DISCOVERING_STOP_SINGLE = 108;//One-to-one
    public static final int DISCOVERING_STOP_GROUP = 109;//组内
    public static final int DISCOVERING_STOP_ALL = 110;// All

    private static IntercomUserBean requUserBean;
    private static IntercomUserBean leaveUserBean;

    private static IntercomUserBean reposeUserBean;
    private static IntercomUserBean startUserBean;
    private static IntercomUserBean endUserBean;

    /**
     * &7F    :the end标志
     * 7E01&  :personal音频
     * 7E02&  :组内音频
     * 7E03&  :Group audio
     *
     * 7E11&  : search for 请求
     * 7E12&  : search for 相应
     * 7E13&  :离开请求
     * 7E14&  :Call start request
     * 7E15&  :通话the end请求
     * <p>
     * 7E16&  :通话开始响应(personal)
     * 7E17&  :通话the end响应
     */


    public static String getCallResponsStartRefuCommand(IntercomUserBean userBean) {
        return "7E18&" + userBean.toString() + "&7F";
    }
    public static String getCallResponsStartSuccCommand(IntercomUserBean userBean) {
        return "7E16&" + userBean.toString() + "&7F";
    }

    public static String getCallResponsStopCommand(IntercomUserBean userBean) {
        return "7E15&" + userBean.toString() + "&7F";
    }

    public static String getCallRequStartCommand(IntercomUserBean userBean) {
        return "7E14&" + userBean.toString() + "&7F";
    }

    public static String getCallRequtStopCommand(IntercomUserBean userBean) {
        return "7E15&" + userBean.toString() + "&7F";
    }

    public static String getDiscoverCommand() {
        if (requUserBean == null) {
            String groupName = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
            String userName = SPUtil.getInstance().getString(SPConsts.USER_NAME, "");
            requUserBean = new IntercomUserBean();
            requUserBean.setGroupName(groupName);
            requUserBean.setUserName(userName);
            requUserBean.setIpAddress(IPUtil.getLocalIPAddress());
            requUserBean.setAudioLevel(1);
        }
        return "7E11&" + requUserBean.toString() + "&7F";
    }

    public static String getLeaveCommand() {
        if (leaveUserBean == null) {
            String groupName = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
            String userName = SPUtil.getInstance().getString(SPConsts.USER_NAME, "");
            leaveUserBean = new IntercomUserBean();
            leaveUserBean.setGroupName(groupName);
            leaveUserBean.setUserName(userName);
            leaveUserBean.setIpAddress(IPUtil.getLocalIPAddress());
        }
        return "7E13&" + leaveUserBean.toString() + "&7F";
    }

    public static String getDiscoverResponsCommand() {
        if (requUserBean == null) {
            String groupName = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
            String userName = SPUtil.getInstance().getString(SPConsts.USER_NAME, "");
            requUserBean = new IntercomUserBean();
            requUserBean.setGroupName(groupName);
            requUserBean.setUserName(userName);
            requUserBean.setIpAddress(IPUtil.getLocalIPAddress());
            requUserBean.setAudioLevel(1);
        }
        return "7E12&" + requUserBean.toString() + "&7F";
    }
}
