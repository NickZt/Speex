package com.personal.AudioStream.discover;

import android.os.Handler;
import android.os.Message;


import com.personal.AudioStream.constants.PCommand;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.network.Multicast;
import com.personal.AudioStream.constants.PBroadCastConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class SignInAndOutReq extends JobHandler {

    private String command;

    public SignInAndOutReq(Handler handler) {
        super(handler);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        if (command != null) {
            byte[] data = command.getBytes(Charset.forName("UTF-8"));
            DatagramPacket datagramPacket = new DatagramPacket(
                    data, data.length, Multicast.getMulticast().getInetAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
            try {
                Multicast.getMulticast().getSendMulticastSocket().send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (PCommand.DISC_REQUEST.equals(command)) {
                sendMsg2MainThread();
            } else if (PCommand.DISC_LEAVE.equals(command)) {
                setCommand(PCommand.DISC_REQUEST);
            }
        }
    }

    /**
     * 发送消息到主线程
     */
    private void sendMsg2MainThread() {
        Message message = new Message();
        message.what = PCommand.DISCOVERING_SEND;
        handler.sendMessage(message);
    }
}
