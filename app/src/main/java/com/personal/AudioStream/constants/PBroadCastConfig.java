package com.personal.AudioStream.constants;

import com.personal.AudioStream.util.SPUtil;

/**
 * 单播/组播 配置类
 * 多播：
 * <p>
 * IP多播通信必须依赖于IP多播地址，在IPv4中它是一个D类IP地址，范围从224.0.0.0到239.255.255.255，并被划分为局部链接多播地址、预留多播地址和管理权限多播地址三类。其中，
 * <p>
 * 局部链接多播地址范围在224.0.0.0~224.0.0.255，这是为路由协议和其它用途保留的地址，路由器并不转发属于此范围的IP包；
 * <p>
 * 预留多播地址为224.0.1.0~238.255.255.255，可用于全球范围（如Internet）或网络协议；
 * <p>
 * 管理权限多播地址为239.0.0.0~239.255.255.255，可供组织内部使用，类似于私有IP地址，不能用于Internet，可限制多播范围。
 * Created by personal on 2017/4/14.
 */

public class PBroadCastConfig {

    // 组播端口号
    public static final int MULTI_BROADCAST_PORT = 10001;
    // 组播IP地址（224.0.0.0~239.255.255.255）
    //public static final String MULTI_BROADCAST_IP = "224.9.9.9";
    //public static final String MULTI_BROADCAST_IP = "239.255.255.255";
//    public static final String MULTI_BROADCAST_IP = "239.0.255.255";
    public static final String BROADCAST_IP = "255.255.255.255";
    public static final String MULTI_BROADCAST_IP = "224.0.0.250";
    public static final String MULTI_BROADCAST_IP_A = "224.0.1.101";
    public static final String MULTI_BROADCAST_IP_B = "224.0.1.102";
    public static final String MULTI_BROADCAST_IP_C = "224.0.1.103";
    public static final String MULTI_BROADCAST_IP_D = "224.0.1.104";
    public static final String MULTI_BROADCAST_IP_E = "224.0.1.105";
    public static final String MULTI_BROADCAST_IP_F = "224.0.1.106";
    public static final String MULTI_BROADCAST_IP_G = "224.0.1.107";
    public static final String MULTI_BROADCAST_IP_H = "224.0.1.108";

    public static String getMultiIP() {
        String string = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
        String result = MULTI_BROADCAST_IP;
        switch (string) {
            case "A组":
                result =  MULTI_BROADCAST_IP_A;
                break;
            case "B组":
                result =  MULTI_BROADCAST_IP_B;
                break;
            case "C组":
                result =  MULTI_BROADCAST_IP_C;
                break;
            case "D组":
                result =  MULTI_BROADCAST_IP_D;
                break;
            case "E组":
                result =  MULTI_BROADCAST_IP_E;
                break;
            case "F组":
                result =  MULTI_BROADCAST_IP_F;
                break;
            case "G组":
                result =  MULTI_BROADCAST_IP_G;
                break;
            case "H组":
                result =  MULTI_BROADCAST_IP_H;
                break;
            default:
                result =  MULTI_BROADCAST_IP;
                break;
        }
        return result;
    }

    // 单播端口号
    public static final int UNICAST_PORT = 10000;


    // 接收超时时间，应小于等于主机的超时时间1500
    public static final int RECEIVE_TIME_OUT = 5000;

    // 响应设备的最大个数，防止UDP广播攻击
    public static final int RESPONSE_DEVICE_MAX = 500;

    // 搜索请求
    public static final byte PACKET_TYPE_FIND_DEVICE_REQ_10 = 0x10;
    // 搜索响应
    public static final byte PACKET_TYPE_FIND_DEVICE_RSP_11 = 0x11;
    // 搜索确认
    public static final byte PACKET_TYPE_FIND_DEVICE_CHK_12 = 0x12;

    public static final byte PACKET_DATA_TYPE_DEVICE_NAME_20 = 0x20;
    public static final byte PACKET_DATA_TYPE_DEVICE_ROOM_21 = 0x21;

}
