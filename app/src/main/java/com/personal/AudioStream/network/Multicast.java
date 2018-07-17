package com.personal.AudioStream.network;


import com.personal.AudioStream.constants.PBroadCastConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * 多播：

 IP多播通信必须依赖于IP多播地址，在IPv4中它是一个D类IP地址，范围从224.0.0.0到239.255.255.255，并被划分为局部链接多播地址、预留多播地址和管理权限多播地址三类。其中，

 局部链接多播地址范围在224.0.0.0~224.0.0.255，这是为路由协议和其它用途保留的地址，路由器并不转发属于此范围的IP包；

 预留多播地址为224.0.1.0~238.255.255.255，可用于全球范围（如Internet）或网络协议；

 管理权限多播地址为239.0.0.0~239.255.255.255，可供组织内部使用，类似于私有IP地址，不能用于Internet，可限制多播范围。
 * Created by personal on 2017/5/9.
 */

public class Multicast {

    // 组播发送Socket
    private MulticastSocket multicastSendSocket;
    // 组播接收Socket
    private MulticastSocket multicastReceiveSocket;
    // IPV4地址
    private InetAddress inetAddress;

    private static final Multicast multicast = new Multicast();

    private Multicast() {
        try {
            inetAddress = InetAddress.getByName(PBroadCastConfig.MULTI_BROADCAST_IP);
            // 创建组播网络地址，并判断·
//            if (!inetAddress.isMulticastAddress()) {
//                //pushMsgToMain(UDP_HANDLER_MESSAGE_TOAST, "IP地址不是组播地址（224.0.0.0~239.255.255.255）");
//                return;
//            }
            multicastSendSocket = new MulticastSocket(PBroadCastConfig.MULTI_BROADCAST_PORT);
            multicastSendSocket.setLoopbackMode(true);
            //multicastSendSocket.setSoTimeout();
            multicastSendSocket.joinGroup(inetAddress);//加入多播组，发送方和接受方处于同一组时，接收方可抓取多播报文信息
            multicastSendSocket.setTimeToLive(4);//设定TTL


            multicastReceiveSocket = new MulticastSocket(PBroadCastConfig.MULTI_BROADCAST_PORT);
            multicastReceiveSocket.setLoopbackMode(true);
            //multicastSendSocket.setSoTimeout();
            multicastReceiveSocket.joinGroup(inetAddress);//加入多播组，发送方和接受方处于同一组时，接收方可抓取多播报文信息
            multicastReceiveSocket.setTimeToLive(4);//设定TTL
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Multicast getMulticast() {
        return multicast;
    }

    public MulticastSocket getSendMulticastSocket() {
        return multicastSendSocket;
    }
    public MulticastSocket getReceiveMulticastSocket() {
        return multicastReceiveSocket;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void free() {
        if (multicastSendSocket != null) {
            try {
                multicastSendSocket.leaveGroup(inetAddress);
                multicastSendSocket.close();
                multicastSendSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (multicastReceiveSocket != null) {
            try {
                multicastReceiveSocket.leaveGroup(inetAddress);
                multicastReceiveSocket.close();
                multicastReceiveSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
