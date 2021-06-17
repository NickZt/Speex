package com.personal.audiostream.group;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.personal.speex.IntercomUserBean;

import org.simple.eventbus.EventBus;

import androidx.fragment.app.Fragment;

/**
 * Created by 山东御银智慧 on 2018/7/24.
 */

public abstract class BaseFragment extends Fragment {

    private String mStatus = "";

    protected OnMyItemClickListener  onItemClickListener;

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setOnItemClickListener(OnMyItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    public abstract void addNewUser(IntercomUserBean userBean);

    /**
     *
     * @param userBean
     * @return  剩余的group name数量为0时则返回 STATUS (即：group name)
     */
    public abstract String removeExistUser(IntercomUserBean userBean);

    public interface OnMyItemClickListener {
        void onItemClick(View view,int status,IntercomUserBean userBean);
    }
}
