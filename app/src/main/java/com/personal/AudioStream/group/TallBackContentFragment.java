package com.personal.AudioStream.group;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.speex.IntercomUserBean;
import com.personal.speex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：create by YangZ on 2018/7/9 09:13
 * 邮箱：YangZL8023@163.com
 */

public class TallBackContentFragment extends BaseFragment {

    private static final String STATUS = "status";

    private RecyclerView recyclerView;

    private TallBackRecyclerViewAdapter adapter;

    private List<IntercomUserBean> userBeans = new ArrayList<>();

    public TallBackContentFragment() {
        // Required empty public constructor
    }

    public static TallBackContentFragment newInstance(String titles) {
        TallBackContentFragment fragment = new TallBackContentFragment();
        Bundle args = new Bundle();
        args.putString(STATUS, titles);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setmStatus(getArguments().getString(STATUS));
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
    }

    private void initView() {
        adapter = new TallBackRecyclerViewAdapter(getContext(),userBeans);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(adapter);
        //间距
        recyclerView.addItemDecoration(new SpacesItemDecoration(20));
    }

    @Override
    public void addNewUser(IntercomUserBean userBean) {
        if (getmStatus().equals(userBean.getGroupName())
                && !userBeans.contains(userBean)) {
            userBeans.add(userBean);
            adapter.notifyItemInserted(userBeans.size() - 1);
        }
    }

    @Override
    public String removeExistUser(IntercomUserBean userBean) {
        if (userBeans.contains(userBean)) {
            int position = userBeans.indexOf(userBean);
            userBeans.remove(position);
            userBeans.add(userBean);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(0, userBeans.size());
        }
        if (userBeans.size() == 0) {
            return getmStatus();
        }else {
            return "非空";
        }
    }
}
