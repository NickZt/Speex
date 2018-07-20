package com.personal.AudioStream.group;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.personal.speex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：create by YangZ on 2018/7/9 09:13
 * 邮箱：YangZL8023@163.com
 */

public class TallBackFragment extends android.support.v4.app.Fragment {

    private static final String STATUS = "status";

    private Integer mStatus = null;//对讲首页状态,默认为null 0 本地，1 其他，2 全部，3 设置
    private RecyclerView recyclerView;
    private TextView tvSend;

    private TallBackRecyclerViewAdapter adapter1;

    private List<IntercomGroupBean> datas;

    private LinearLayout llContent;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ArrayList<Fragment> mFragments;

    public TallBackFragment() {
        // Required empty public constructor
    }

    public static TallBackFragment newInstance(int status) {
        TallBackFragment fragment = new TallBackFragment();
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
        View view = inflater.inflate(R.layout.fragment_tall_back, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvSend = view.findViewById(R.id.tv_send);
        llContent = view.findViewById(R.id.ll_content);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.viewPager);
        initView();
        initData();
        return view;
    }

    private void initData() {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        if (mStatus == 0) {
            for (int i = 0; i < 20; i++) {
                datas.add(new IntercomGroupBean("本地" + i));
            }
            adapter1.setData(datas);
        }else if (mStatus == 2){
            for (int i = 0; i < 20; i++) {
                datas.add(new IntercomGroupBean("全部" + i));
            }
            adapter1.setData(datas);
        } else if (mStatus == 3) {
            for (int i = 0; i < 5; i++) {
                datas.add(new IntercomGroupBean("设置" + i));
            }
            adapter1.setData(datas);
        }

    }

    private void initView() {
        if (mStatus == 2) {
            tvSend.setVisibility(View.VISIBLE);
        } else {
            tvSend.setVisibility(View.GONE);
        }
        if (mStatus == 1) {
            recyclerView.setVisibility(View.GONE);
            llContent.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            llContent.setVisibility(View.GONE);
        }
        adapter1 = new TallBackRecyclerViewAdapter(getContext());
        if (mStatus != 1) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            recyclerView.setAdapter(adapter1);
            //间距
            recyclerView.addItemDecoration(new SpacesItemDecoration(20));
        } else {
            mFragments = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                titles.add("其他分组" + i);
                mFragments.add(TallBackContentFragment.newInstance(i));
            }

            TabAdapter mTabAdapter = new TabAdapter(this.getChildFragmentManager(), mFragments, titles);
            viewPager.setAdapter(mTabAdapter);//给ViewPager设置适配器
            tabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);//设置TabLayout可滑动
            viewPager.setOffscreenPageLimit(5);
        }

    }

}
