package com.personal.AudioStream.constants;

import com.personal.AudioStream.util.IPUtil;
import com.personal.AudioStream.util.SPUtil;
import com.personal.speex.IntercomUserBean;

/**
 * 指令配置类
 * Created by personal on 2017/4/13.
 */

public class PCommand {

    // Service向Activity发送的跨进程指令
    public static final String DISC_REQUEST = "DISC_REQUEST";
    public static final String DISC_REQUEST_TWO = "DISC_REQUEST_TWO";
    public static final String DISC_RESPONSE = "DISC_RESPONSE";
    public static final String DISC_LEAVE = "DISC_LEAVE";

    // Activity向Service发送的跨进程指令
    public static final String START_FOREGROUND_ACTION = "com.jd.wly.intercom.action.start";
    public static final String STOP_FOREGROUND_ACTION = "com.jd.wly.intercom.action.stop";
    // 前台Service
    public static final String MAIN_ACTION = "com.jd.wly.intercom.action.main";
    public static final int FOREGROUND_SERVICE = 100;


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

    //开始通话标志
    public static final int DISCOVERING_START_SINGLE = 111;//单对单
    public static final int DISCOVERING_START_SINGLE_SUCCESS = 104;//单对单成功相应
    public static final int DISCOVERING_START_SINGLE_REFUSE = 105;//单对单对方再通话中
    public static final int DISCOVERING_START_GROUP = 106;//组内
    public static final int DISCOVERING_START_ALL= 107;//全部
    //结束通话标志
    public static final int DISCOVERING_STOP_SINGLE = 108;//单对单
    public static final int DISCOVERING_STOP_GROUP = 109;//组内
    public static final int DISCOVERING_STOP_ALL = 110;//全部

    private static IntercomUserBean requUserBean;
    private static IntercomUserBean leaveUserBean;

    private static IntercomUserBean reposeUserBean;
    private static IntercomUserBean startUserBean;
    private static IntercomUserBean endUserBean;

    /**
     * &7F    :结束标志
     * 7E01&  :个人音频
     * 7E02&  :组内音频
     * 7E03&  :群发音频
     *
     * 7E11&  :搜索请求
     * 7E12&  :搜索相应
     * 7E13&  :离开请求
     * 7E14&  :通话开始请求
     * 7E15&  :通话结束请求
     * <p>
     * 7E16&  :通话开始响应(个人)
     * 7E17&  :通话结束响应
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
