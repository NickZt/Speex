package com.personal.speex;


import android.os.Parcel;
import android.os.Parcelable;

public class IntercomUserBean implements Parcelable {

    //IP地址,需要接收者的ip
    private String ipAddress;

    //用户名
    private String userName;

    //组名
    private String groupName;

    /**
     * 状态：原本是用户是否在线的状态，现在改为命令
     * 10：请求    命令
     * 11：相应    命令
     * 12：离开    命令
     * 13：开始语音      命令
     * 14：结束语音     命令
     */
    private int statusOnline;

    //级别:4 全部 ；3组内；2个人；1搜索
    private int audioLevel;



    //必须提供一个名为CREATOR的static final属性 该属性需要实现android.os.Parcelable.Creator<T>接口
    public static final Parcelable.Creator<IntercomUserBean> CREATOR = new Parcelable.Creator<IntercomUserBean>() {

        @Override
        public IntercomUserBean createFromParcel(Parcel source) {
            return new IntercomUserBean(source);
        }

        @Override
        public IntercomUserBean[] newArray(int size) {
            return new IntercomUserBean[size];
        }
    };

    protected IntercomUserBean(Parcel source) {
        readFromParcel(source);
    }

    public IntercomUserBean() {
    }

    public IntercomUserBean(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public IntercomUserBean(String ipAddress, String userName, String groupName) {
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.groupName = groupName;
    }


    public IntercomUserBean(String ipAddress, String userName, String groupName, int statusOnline,int audioLevel) {
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.groupName = groupName;
        this.statusOnline = statusOnline;
        this.audioLevel = audioLevel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getStatusOnline() {
        return statusOnline;
    }

    public void setStatusOnline(int statusOnline) {
        this.statusOnline = statusOnline;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getAudioLevel() {
        return audioLevel;
    }

    public void setAudioLevel(int audioLevel) {
        this.audioLevel = audioLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntercomUserBean userBean = (IntercomUserBean) o;

        return ipAddress.equals(userBean.ipAddress);

    }

    @Override
    public int hashCode() {
        return ipAddress.hashCode();
    }




    @Override
    public int describeContents() {
        return 0;
    }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(ipAddress);
            dest.writeString(userName);
            dest.writeString(groupName);
            dest.writeInt(statusOnline);
            dest.writeInt(audioLevel);
        }


    public void readFromParcel(Parcel source) {
        ipAddress = source.readString();
        userName = source.readString();
        groupName = source.readString();
        statusOnline = source.readInt();
        audioLevel = source.readInt();
    }

    @Override
    public String toString() {
        return "{" +
                "ipAddress='" + ipAddress + '\'' +
                ", userName='" + userName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", audioLevel=" + audioLevel +
                ", statusOnline=" + statusOnline +
                '}';
    }
}
