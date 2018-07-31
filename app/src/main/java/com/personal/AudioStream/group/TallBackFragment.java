package com.personal.AudioStream.group;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.personal.AudioStream.constants.PCommand;
import com.personal.AudioStream.constants.SPConsts;
import com.personal.AudioStream.util.IPUtil;
import com.personal.AudioStream.util.SPUtil;
import com.personal.speex.IntercomUserBean;
import com.personal.speex.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：create by YangZ on 2018/7/9 09:13
 * 邮箱：YangZL8023@163.com
 */

public class TallBackFragment extends BaseFragment {

    private static final String STATUS = "status";

    private Integer mStatus = null;//对讲首页状态,默认为null 0 本地，1 其他，2 全部，3 设置
    private RecyclerView recyclerView;
    private TextView tvSend;

    private TallBackRecyclerViewAdapter adapter1;

    private List<IntercomUserBean> userDatas = new ArrayList<>();
    private Map<String,List<IntercomUserBean>> childMap = new HashMap<String,List<IntercomUserBean>>();

    private LinearLayout llContent;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ArrayList<BaseFragment> mFragments = new ArrayList<>();
    private List<String> titles =  new ArrayList<>();;
    private TabAdapter mTabAdapter;

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
    }

    private void initView() {
        if (mStatus == 0) {
            tvSend.setVisibility(View.VISIBLE);
            tvSend.setText("组内发送");
            tvSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvSend.setPadding(10,10,10,10);
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, PCommand.MULTI_FLAG_GROUP_LEVEL,
                                new IntercomUserBean(
                                        IPUtil.getLocalIPAddress(),
                                        SPUtil.getInstance().getString(SPConsts.USER_NAME),
                                        SPUtil.getInstance().getString(SPConsts.GROUP_NAME)
                                        ));
                    }
                }
            });
        } else if (mStatus == 2) {
            tvSend.setVisibility(View.VISIBLE);
            tvSend.setText("全部发送");
            tvSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, PCommand.MULTI_FLAG_ALL_LEVEL, new IntercomUserBean());
                    }
                }
            });
        } else if (mStatus == 1) {
            recyclerView.setVisibility(View.GONE);
            tvSend.setVisibility(View.GONE);
            llContent.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            llContent.setVisibility(View.GONE);
        }

        if (mStatus != 1) {
            adapter1 = new TallBackRecyclerViewAdapter(getContext(),userDatas);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            recyclerView.setAdapter(adapter1);
            //间距
            recyclerView.addItemDecoration(new SpacesItemDecoration(20));
            adapter1.setOnMyClickListener(new TallBackRecyclerViewAdapter.OnMyClickListener() {
                @Override
                public void clickListener(View v,int status,IntercomUserBean userBean) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v,status,userBean);
                    }
                }
            });
        } else {
            mTabAdapter = new TabAdapter(this.getChildFragmentManager(), mFragments, titles);
            viewPager.setAdapter(mTabAdapter);//给ViewPager设置适配器
            tabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);//设置TabLayout可滑动
            viewPager.setOffscreenPageLimit(5);
        }
    }

    @Override
    public void addNewUser(IntercomUserBean userBean){
        if (adapter1 != null) {
            if (mStatus == 0) {
                if (SPUtil.getInstance().getString(SPConsts.GROUP_NAME,"").equals(userBean.getGroupName())
                        && !userDatas.contains(userBean)) {
                    userDatas.add(userBean);
                    adapter1.notifyItemInserted(userDatas.size() - 1);
                }
            }else if (mStatus == 2){
                if (!userDatas.contains(userBean)) {
                    userDatas.add(userBean);
                    adapter1.notifyItemInserted(userDatas.size() - 1);
                }
            }else if (mStatus == 1){
                if (!titles.contains(userBean.getGroupName())) {
                    titles.add(userBean.getGroupName());
                    mFragments.add(TallBackContentFragment.newInstance(userBean.getGroupName()));
                    //Collections.sort(titles);
                    mTabAdapter.notifyDataSetChanged();
                }
                for (BaseFragment mFragment : mFragments) {
                    mFragment.addNewUser(userBean);
                }
            }
        }
    }

    @Override
    public String removeExistUser(IntercomUserBean userBean){
        if (adapter1 != null) {
            if (mStatus == 0) {
                if (userDatas.contains(userBean)) {
                    int position = userDatas.indexOf(userBean);
                    userDatas.remove(position);
                    userDatas.add(userBean);
                    adapter1.notifyItemRemoved(position);
                    adapter1.notifyItemRangeChanged(0, userDatas.size());
                }
            }else if (mStatus == 2){
                if (userDatas.contains(userBean)) {
                    int position = userDatas.indexOf(userBean);
                    userDatas.remove(position);
                    userDatas.add(userBean);
                    adapter1.notifyItemRemoved(position);
                    adapter1.notifyItemRangeChanged(0, userDatas.size());
                }
            }else if (mStatus == 1){
                if (!titles.contains(userBean.getGroupName())) {
                    titles.add(userBean.getGroupName());
                    mFragments.add(TallBackContentFragment.newInstance(userBean.getGroupName()));
                    //Collections.sort(titles);
                    mTabAdapter.notifyDataSetChanged();
                }
                for (BaseFragment mFragment : mFragments) {
                    String result = mFragment.removeExistUser(userBean);
                    if (titles.contains(result)) {
                        titles.remove(result);
                    }
                }
            }
        }
        return ""+mStatus;
    }

}
