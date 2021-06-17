package com.personal.audiostream.network;



import com.personal.audiostream.constants.PBroadCastConfig;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by yanghao1 on 2017/5/15.
 */

public class Unicast {

    byte[] receiveMsg = new byte[512];
    private DatagramPacket receivePacket;
    private DatagramSocket receiveSocket;

    private DatagramPacket sendPacket;
    private DatagramSocket sendSocket;

    private static final Unicast unicast = new Unicast();

    private Unicast() {
        try {
            //  Related 接收Socket
            receivePacket = new DatagramPacket(receiveMsg, receiveMsg.length);
            receiveSocket = new DatagramSocket(PBroadCastConfig.UNICAST_PORT);
            //  Related  send Socket
            sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static Unicast getUnicast() {
        return unicast;
    }
    public DatagramSocket getUnicastSendSocket() {
        return sendSocket;
    }

    public DatagramSocket getUnicastReceiveSocket() {
        return receiveSocket;
    }

    public void free() {
        if (sendSocket != null) {
            sendSocket.close();
            sendSocket = null;
        }
        if (receiveSocket != null) {
            receiveSocket.close();
            receiveSocket = null;
        }
    }
}
