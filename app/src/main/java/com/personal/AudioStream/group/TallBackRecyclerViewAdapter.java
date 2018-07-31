package com.personal.AudioStream.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.AudioStream.constants.PCommand;
import com.personal.speex.IntercomUserBean;
import com.personal.speex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：create by YangZ on 2018/7/9 09:46
 * 邮箱：YangZL8023@163.com
 */

public class TallBackRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;//
    private LayoutInflater mInflater;

    private List<IntercomUserBean> data = new ArrayList<>();

    private DefaultViewHolder defaultViewHolder;

    private OnMyClickListener onMyClickListener;

    public void setData(List<IntercomUserBean> data) {
        this.data.clear();
        this.data.addAll(data);
        /*if (data == null) {
            this.data = new ArrayList<>();
        }*/
        notifyDataSetChanged();
    }


    public TallBackRecyclerViewAdapter(Context mContext) {
        this.mContext = mContext;
        if (mInflater == null) {
            mInflater = LayoutInflater.from(mContext);
        }
    }

    public TallBackRecyclerViewAdapter(Context mContext,List<IntercomUserBean> userBeanList) {
        this(mContext);
        data = userBeanList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_name, parent, false);
        return new DefaultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DefaultViewHolder viewHolder = (DefaultViewHolder) holder;
        final String userName = data.get(position).getUserName();
        final String ipAddress = data.get(position).getIpAddress();
        viewHolder.tv_user_name.setText(userName);
        viewHolder.tv_ip.setText(ipAddress);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMyClickListener != null) {
                    onMyClickListener.clickListener(v, PCommand.UNI_FLAG_PER_LEVEL,data.get(position));
                }
                Toast.makeText(mContext, userName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class DefaultViewHolder extends RecyclerView.ViewHolder {
        TextView tv_user_name;
        TextView tv_ip;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_ip = itemView.findViewById(R.id.tv_ip);
        }
    }

    public void setOnMyClickListener(OnMyClickListener onMyClickListener) {
        this.onMyClickListener = onMyClickListener;
    }

    public interface OnMyClickListener {
        void clickListener(View v,int status,IntercomUserBean userBean);
    }
}
