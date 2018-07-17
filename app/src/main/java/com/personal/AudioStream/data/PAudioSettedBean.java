package com.personal.AudioStream.data;

/**
 * Created by 山东御银智慧 on 2018/7/16.
 */

public class PAudioSettedBean {
    /**
     * 是否开启保存
     */
    private boolean isSaved;

    public PAudioSettedBean() {
        this.isSaved = false;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
