package com.personal.AudioStream.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    private List<IntercomGroupBean> data;

    private DefaultViewHolder defaultViewHolder;

    public void setData(List<IntercomGroupBean> data) {
        this.data = data;
        if (data == null) {
            this.data = new ArrayList<>();
        }
        notifyDataSetChanged();
    }


    public TallBackRecyclerViewAdapter(Context mContext) {
        this.mContext = mContext;
        if (mInflater == null) {
            mInflater = LayoutInflater.from(mContext);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_name, parent, false);
        return new DefaultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DefaultViewHolder viewHolder = (DefaultViewHolder) holder;
        final String name = data.get(position).getName();
        viewHolder.tv.setText(name);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class DefaultViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_name_left);
        }
    }

}
