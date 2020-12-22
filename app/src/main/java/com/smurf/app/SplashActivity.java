package com.smurf.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.smurf.app.splash.ImgFragment;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ImageView img1_normal,img1_select,img2_normal,img2_select,img3_normal,img3_select;

    private List<Fragment> fragments;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        initFragment();
        initView();
    }

    private void initFragment() {
        fragments = new ArrayList<>();
        fragments.add(ImgFragment.getInstance("",1));
        fragments.add(ImgFragment.getInstance("",2));
        fragments.add(ImgFragment.getInstance("",3));
    }

    private void initView() {
        viewPager = findViewById(R.id.viewpager);
        img1_normal = findViewById(R.id.img_1_normal);
        img1_select = findViewById(R.id.img_1_select);
        img2_normal = findViewById(R.id.img_2_normal);
        img2_select = findViewById(R.id.img_2_select);
        img3_normal = findViewById(R.id.img_3_normal);
        img3_select = findViewById(R.id.img_3_select);

        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                resetDot();
                if(position == 0){
                    img1_normal.setVisibility(View.GONE);
                    img1_select.setVisibility(View.VISIBLE);
                }else if(position == 1){
                    img2_normal.setVisibility(View.GONE);
                    img2_select.setVisibility(View.VISIBLE);
                }else if(position == 2){
                    img3_normal.setVisibility(View.GONE);
                    img3_select.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        resetDot();
        img1_normal.setVisibility(View.GONE);
        img1_select.setVisibility(View.VISIBLE);

    }

    /**
     * 充值dot
     */
    private void resetDot(){
        img1_normal.setVisibility(View.VISIBLE);
        img1_select.setVisibility(View.GONE);
        img2_normal.setVisibility(View.VISIBLE);
        img2_select.setVisibility(View.GONE);
        img3_normal.setVisibility(View.VISIBLE);
        img3_select.setVisibility(View.GONE);
    }

    /**
     * viewpager adapter
     */
    class MyAdapter extends FragmentPagerAdapter{

        public MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
