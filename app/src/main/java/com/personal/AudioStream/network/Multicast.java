package com.personal.AudioStream.network;


import com.personal.AudioStream.util.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by yanghao1 on 2017/5/9.
 */

public class Multicast {

    // 组播Socket
    private MulticastSocket multicastSocket;
    // IPV4地址
    private InetAddress inetAddress;

    private static final Multicast multicast = new Multicast();

    private Multicast() {
        try {
            inetAddress = InetAddress.getByName(Constants.MULTI_BROADCAST_IP);
            // 创建组播网络地址，并判断
            /*if (!inetAddress.isMulticastAddress()) {
                //pushMsgToMain(UDP_HANDLER_MESSAGE_TOAST, "IP地址不是组播地址（224.0.0.0~239.255.255.255）");
                return;
            }*/
            multicastSocket = new MulticastSocket(Constants.MULTI_BROADCAST_PORT);
            multicastSocket.setLoopbackMode(true);
            //multicastSocket.setSoTimeout();
            multicastSocket.joinGroup(inetAddress);
            multicastSocket.setTimeToLive(4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Multicast getMulticast() {
        return multicast;
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void free() {
        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(inetAddress);
                multicastSocket.close();
                multicastSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
