package com.personal.AudioStream.group;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.speex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：create by YangZ on 2018/7/9 09:13
 * 邮箱：YangZL8023@163.com
 */

public class TallBackContentFragment extends android.support.v4.app.Fragment {

    private static final String STATUS = "status";

    private Integer mStatus = null;//对讲首页状态,默认为null 0 本地，1 其他，2 全部，3 设置
    private RecyclerView recyclerView;

    private TallBackRecyclerViewAdapter adapter;

    private List<IntercomGroupBean> datas;

    public TallBackContentFragment() {
        // Required empty public constructor
    }

    public static TallBackContentFragment newInstance(int status) {
        TallBackContentFragment fragment = new TallBackContentFragment();
        Bundle args = new Bundle();
        args.putInt(STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatus = getArguments().getInt(STATUS, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tall_back_content, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        initView();
        initData();
        return view;
    }

    private void initData() {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        for (int i = 0; i < 20; i++) {
            datas.add(new IntercomGroupBean("分组" + mStatus + "_" + i));
        }
        adapter.setData(datas);
    }

    private void initView() {
        adapter = new TallBackRecyclerViewAdapter(getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(adapter);
        //间距
        recyclerView.addItemDecoration(new SpacesItemDecoration(20));
    }

}
