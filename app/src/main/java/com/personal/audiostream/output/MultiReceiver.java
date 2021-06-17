package com.personal.audiostream.output;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.personal.audiostream.constants.PBroadCastConfig;
import com.personal.audiostream.constants.PCommand;
import com.personal.audiostream.constants.SPConsts;
import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.network.Multicast;
import com.personal.audiostream.util.IPUtil;
import com.personal.audiostream.util.SPUtil;
import com.personal.speex.IntercomUserBean;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class MultiReceiver extends JobHandler {
    /**
     * 0:空闲 level
     * 1: search for  level
     * 2：personal level
     * 3：组内 level
     * 4： All  level
     */
    public static volatile int messageLevel = 0;

    public static volatile boolean isAudio = true;
    private static volatile boolean isLocked = false;

    public MultiReceiver(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        while (true) {
            // 设置接收缓冲段
            byte[] receivedData = new byte[512 * 5];
            DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                // 接收数据报文
                Multicast.getMulticast().getReceiveMulticastSocket().receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 判断数据报文类型，并做相应处理，

//            Log.e("audio", "receData: " + "getLength" + datagramPacket.getLength()+Arrays.toString(receData) );
            synchronized (MultiReceiver.class){
                if (!datagramPacket.getAddress().getHostName().equals(IPUtil.getLocalIPAddress())) {
                    try {
                        byte[] receData = datagramPacket.getData();
                        int length = datagramPacket.getLength();
                        //Log.e("audio", "receData2: " + "getLength" + Arrays.toString(receData));
                        byte[] start = new byte[5];
                        byte[] end = new byte[3];
                        System.arraycopy(receData, 0, start, 0, start.length);
                        System.arraycopy(receData, length - end.length, end, 0, end.length);
                        String startStr = new String(start);
                        String endStr = new String(end);
                        Log.e("audio", "recedata3: " + startStr);

                        if ("7E01&".equals(startStr)) {//personal音频
                            if (messageLevel > 2) {
                            } else {
                                if (isAudio) {
                                    byte[] ipaddress = new byte[16];
                                    System.arraycopy(receData, start.length, ipaddress, 0, ipaddress.length);
                                    String address = new String(ipaddress).replaceAll("&", "").trim();
                                    Log.e("audio", "address: " + address);
                                    if (IPUtil.getLocalIPAddress().equals(address)) {
                                        byte[] audioData = new byte[length - 5 - 3 - 16];
                                        System.arraycopy(receData, start.length + 16, audioData, 0, audioData.length);
                                        handleAudioData(audioData);
                                    }
                                }
                            }
                        } else if ("7E02&".equals(startStr)) {//组内音频
                            if (messageLevel > 3) {
                            } else {
                                if (isAudio) {
                                   /* byte[] name = new byte[6];
                                    System.arraycopy(receData, start.length, name, 0, name.length);
                                    String nameStr = new String(name).replaceAll("&", "").trim();
                                    Log.e("audio", "address: " + nameStr);
                                    if (SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "").equals(nameStr)) {
                                        byte[] audioData = new byte[length - 5 - 3 - 6];
                                        System.arraycopy(receData, start.length + 6, audioData, 0, audioData.length);
                                        handleAudioData(audioData);
                                    }*/
                                    messageLevel = 3;
                                    byte[] audioData = new byte[length - 5 - 3];
                                    System.arraycopy(receData, start.length, audioData, 0, audioData.length);
                                    handleAudioData(audioData);
                                }
                            }
                        } else if ("7E03&".equals(startStr)) {//Group audio
                            messageLevel = 4;
                            byte[] audioData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, audioData, 0, audioData.length);
                            handleAudioData(audioData);
                            //sendMsg2MainThread("",PCommand.DISCOVERING_START);
                        } else if ("7E11&".equals(startStr)) {// search for 请求
                            handleSendResponseData(datagramPacket, 1,null);
                        } else if ("7E12&".equals(startStr)) {// search for 响应请求
                            byte[] messageData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, messageData, 0, messageData.length);
                            String message = new String(messageData).trim();
                            IntercomUserBean userBean = new Gson().fromJson(message, IntercomUserBean.class);
                            sendMsg2MainThread(userBean, PCommand.DISCOVERING_RECEIVE);
                        } else if ("7E13&".equals(startStr)) {//离开请求
                            byte[] messageData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, messageData, 0, messageData.length);
                            String message = new String(messageData).trim();
                            IntercomUserBean userBean = new Gson().fromJson(message, IntercomUserBean.class);
                            sendMsg2MainThread(userBean, PCommand.DISCOVERING_LEAVE);
                        } else if ("7E14&".equals(startStr)) {//Call start request
                            byte[] messageData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, messageData, 0, messageData.length);
                            String message = new String(messageData).trim();
                            IntercomUserBean userBean = new Gson().fromJson(message, IntercomUserBean.class);
                            if (userBean.getAudioLevel() == 2) {//personal
                                if (userBean.getIpAddress().equals(IPUtil.getLocalIPAddress())) {
                                    if (!isLocked) {
                                        isLocked = true;
                                        messageLevel = 2;
                                        handleSendResponseData(datagramPacket, 2,userBean);
                                        sendMsg2MainThread(userBean,PCommand.DISCOVERING_START_SINGLE);
                                    }else {
                                        handleSendResponseData(datagramPacket, 3,userBean);
                                    }
                                }
                            } else if (userBean.getAudioLevel() == 3) {//组内
                                if (SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "").equals(userBean.getGroupName())) {
                                    isLocked = true;
                                    messageLevel = 3;
                                    //sendMsg2MainThread("",PCommand.DISCOVERING_START);
                                }
                            } else if (userBean.getAudioLevel() == 4) {// All
                            }
                        } else if ("7E15&".equals(startStr)) {//通话the end请求
                            byte[] messageData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, messageData, 0, messageData.length);
                            String message = new String(messageData).trim();
                            IntercomUserBean userBean = new Gson().fromJson(message, IntercomUserBean.class);
                            if (userBean.getAudioLevel() == 2) {//personal
                                if (userBean.getIpAddress().equals(IPUtil.getLocalIPAddress())) {
                                    messageLevel = 0;
                                    isLocked = false;
                                    sendMsg2MainThread("",PCommand.DISCOVERING_STOP_SINGLE);
                                }
                            } else if (userBean.getAudioLevel() == 3) {//组内
                                if (SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "").equals(userBean.getGroupName())) {
                                    messageLevel = 0;
                                    isLocked = false;
                                }
                            } else if (userBean.getAudioLevel() == 4) {// All
                                messageLevel = 0;
                            }
                        } else if ("7E16&".equals(startStr)) {//通话开始响应(personal)成功
                            byte[] messageData = new byte[length - 5 - 3];
                            System.arraycopy(receData, start.length, messageData, 0, messageData.length);
                            String message = new String(messageData).trim();
                            IntercomUserBean userBean = new Gson().fromJson(message, IntercomUserBean.class);
                            userBean.setIpAddress(datagramPacket.getAddress().getHostName());
                            //启动personal通话
                            sendMsg2MainThread(userBean, PCommand.DISCOVERING_START_SINGLE_SUCCESS);
                        } else if ("7E18&".equals(startStr)) {//通话开始响应(personal)失败
                            //启动personal通话
                            sendMsg2MainThread("", PCommand.DISCOVERING_START_SINGLE_REFUSE);
                        } else if ("7E17&".equals(startStr) && "&7F".equals(startStr)) {//通话the end响应
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("zhuanhuan", "recedata: " + e.getMessage());
                    }
                }
            }

        }
    }

    /**
     * 处理命令数据
     *
     * @param packet 命令数据包
     * @param level
     */
    private void handleSendResponseData(DatagramPacket packet, int level,IntercomUserBean userBean) {
        DatagramPacket sendPacket = null;
        switch (level) {
            case 1:// search for 请求进行响应
                byte[] feedback = PCommand.getDiscoverResponsCommand().getBytes(Charset.forName("UTF-8"));
                //  send 数据
                sendPacket = new DatagramPacket(feedback, feedback.length,
                        packet.getAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
                break;
            case 2://personal通话请求进行响应成功
                userBean.setStatusOnline(1);//成功
                userBean.setIpAddress(packet.getAddress().getHostName());
                byte[] feedback2 = PCommand.getCallResponsStartSuccCommand(userBean).getBytes(Charset.forName("UTF-8"));
                //  send 数据
                sendPacket = new DatagramPacket(feedback2, feedback2.length,
                        packet.getAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
                break;
            case 3://personal通话请求进行响应失败
                userBean.setStatusOnline(0);//失败
                userBean.setIpAddress(packet.getAddress().getHostName());
                byte[] feedback3 = PCommand.getCallResponsStartRefuCommand(userBean).getBytes(Charset.forName("UTF-8"));
                //  send 数据
                sendPacket = new DatagramPacket(feedback3, feedback3.length,
                        packet.getAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
                break;
        }
        try {
            Multicast.getMulticast().getSendMulticastSocket().send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理命令数据
     *
     * @param packet 命令数据包
     */
  /*  private void handleCommandData(DatagramPacket packet) {
        String content = new String(packet.getData()).trim();
        String[] split = content.split("&");
        Log.e("audio", "handleCommandData: " + "收到消息" + content);
        Log.e("audio", "handleCommandData: " + IPUtil.getLocalIPAddress() + "=====" + packet.getAddress().toString());
        if (PCommand.DISC_REQUEST.equals(split[0]) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            byte[] feedback = PCommand.getReponseCommand(1).getBytes(Charset.forName("UTF-8"));
            //  send 数据
            DatagramPacket sendPacket = new DatagramPacket(feedback, feedback.length,
                    packet.getAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
            try {
                Multicast.getMulticast().getSendMulticastSocket().send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  send Handler消息
            sendMsg2MainThread(new IntercomUserBean(packet.getAddress().toString(), split[2], split[1]), PCommand.DISCOVERING_RECEIVE);
        } else if (PCommand.DISC_RESPONSE.equals(split[0]) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            //  send Handler消息
            sendMsg2MainThread(new IntercomUserBean(packet.getAddress().toString(), split[2], split[1]), PCommand.DISCOVERING_RECEIVE);
        } else if (PCommand.DISC_LEAVE.equals(split[0]) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            sendMsg2MainThread(new IntercomUserBean(packet.getAddress().toString(), split[2], split[1]), PCommand.DISCOVERING_LEAVE);
        }
    }*/

    /**
     * 处理音频数据
     *
     * @param encodedData 音频数据包
     */
    private void handleAudioData(byte[] encodedData) {
        AudioData audioData = new AudioData(encodedData);
        MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).put(audioData);
    }

    /**
     *  send Handler消息
     *
     * @param content 内容
     */
    private void sendMsg2MainThread(String content, int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    /**
     *  send Handler消息
     *
     * @param content 内容
     */
    private void sendMsg2MainThread(IntercomUserBean content, int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    public static void setMessageLevel(int messageLevel) {
        MultiReceiver.messageLevel = messageLevel;
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
