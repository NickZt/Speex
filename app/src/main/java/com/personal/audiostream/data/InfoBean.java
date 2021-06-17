package com.personal.audiostream.data;

/**
 * Created by 山东御银智慧 on 2018/7/10.
 */

public class InfoBean {

    /**
     * IPaddress
     */
    private String IPaddress;

    /**
     * 端口
     */
    private int port;

    /**
     * equipmentname
     */
    private String deviceName;

    /**
     *  send username称
     */
    private String senderName;

    /**
     *  send group name Permission level
     */
    private String senderLevel;

    /**
     *  send group name的group name
     */
    private String groupName;

    /**
     *  Record creation time
     */
    private long createAudioTime;

    /**
     * 录音the end时间
     */
    private long endAudioTime;

    /**
     * 录音时长，单位：秒
     */
    private int audioTimeLength;

    /**
     * 录音 send 时间
     */
    private long sendTime;

    /**
     * 录音 Save 到PCMname
     */
    private String pcmFileName;

    /**
     * news
     */
    private String message;

    @Override
    public int hashCode() {
        return IPaddress.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InfoBean) {
            return this.IPaddress.equals(((InfoBean)o).getIPaddress());
        }
        return super.equals(o);
    }
    @Override
    public String toString() {
        return "InfoBean{" +
                "IPaddress='" + IPaddress + '\'' +
                ", port=" + port +
                ", deviceName='" + deviceName + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderLevel='" + senderLevel + '\'' +
                ", groupName='" + groupName + '\'' +
                ", createAudioTime=" + createAudioTime +
                ", endAudioTime=" + endAudioTime +
                ", audioTimeLength=" + audioTimeLength +
                ", sendTime=" + sendTime +
                ", pcmFileName=" + pcmFileName +
                ", message='" + message + '\'' +
                '}';
    }


    public String getPcmFileName() {
        return pcmFileName;
    }

    public void setPcmFileName(String pcmFileName) {
        this.pcmFileName = pcmFileName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIPaddress() {
        return IPaddress;
    }

    public void setIPaddress(String IPaddress) {
        this.IPaddress = IPaddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderLevel() {
        return senderLevel;
    }

    public void setSenderLevel(String senderLevel) {
        this.senderLevel = senderLevel;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getCreateAudioTime() {
        return createAudioTime;
    }

    public void setCreateAudioTime(long createAudioTime) {
        this.createAudioTime = createAudioTime;
    }

    public long getEndAudioTime() {
        return endAudioTime;
    }

    public void setEndAudioTime(long endAudioTime) {
        this.endAudioTime = endAudioTime;
    }

    public int getAudioTimeLength() {
        return audioTimeLength;
    }

    public void setAudioTimeLength(int audioTimeLength) {
        this.audioTimeLength = audioTimeLength;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
