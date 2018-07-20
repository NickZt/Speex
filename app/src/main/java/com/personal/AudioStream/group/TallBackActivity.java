package com.personal.AudioStream.group;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.personal.speex.R;

import java.util.ArrayList;
import java.util.List;

public class TallBackActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivShare;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ArrayList<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tall_back);
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.iv_back);
        ivShare = findViewById(R.id.iv_share);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        initView();
    }

    private void initView() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvTitle.setText("对讲");
        ivShare.setVisibility(View.GONE);

        mFragments = new ArrayList<>();
        mFragments.add(TallBackFragment.newInstance(0));
        mFragments.add(TallBackFragment.newInstance(1));
        mFragments.add(TallBackFragment.newInstance(2));
        mFragments.add(TallBackFragment.newInstance(3));

        List<String> titles = new ArrayList<>();
        titles.add("本地");
        titles.add("其他");
        titles.add("全部");
        titles.add("设置");

        TabAdapter mTabAdapter = new TabAdapter(getSupportFragmentManager(), mFragments, titles);
        viewPager.setAdapter(mTabAdapter);//给ViewPager设置适配器
        tabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
        tabLayout.setTabMode(TabLayout.MODE_FIXED);//设置TabLayout可滑动
        viewPager.setOffscreenPageLimit(3);

    }

}
