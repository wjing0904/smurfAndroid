package com.smurf.app.splash;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smurf.app.R;
import com.smurf.app.WebViewActivity;

public class ImgFragment extends Fragment {


    private ImageView img;
    private TextView txtView;
    private int index;

    private int time = 3;
    private boolean isRunning = true;
    private TextView start_tv;

    public static ImgFragment getInstance(String url,int index){
        ImgFragment fragment = new ImgFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url",url);
        bundle.putInt("index",index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash,null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img = view.findViewById(R.id.image);
        txtView = view.findViewById(R.id.txt_time);
        index = getArguments().getInt("index");
        start_tv = view.findViewById(R.id.start_tv);
        txtView.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        start_tv.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        if(index == 1){
            img.setImageResource(R.drawable.start_one);
        }else if(index == 2){
            img.setImageResource(R.drawable.start_two);
        }else if(index == 3){
            img.setImageResource(R.drawable.start_three);
        }
        isRunning = true;
        if(txtView.getVisibility() == View.VISIBLE){
            txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWebActivity();
                }
            });
            startTime();
        }
        if(start_tv.getVisibility() == View.VISIBLE){
            start_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWebActivity();
                }
            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        time = 3;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    private void startTime(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(time > 0 && isRunning){
                    time --;
                    txtView.post(new Runnable() {
                        @Override
                        public void run() {
                            txtView.setText("跳过"+String.valueOf(time));
                        }
                    });
                    handler.postDelayed(this,1000);
                }else{
                    //自动跳过
                    openWebActivity();
                }
            }
        };
        handler.postDelayed(runnable,1000);
    }

    private Handler handler = new Handler();

    /**
     * 打开主页
     */
    private void openWebActivity(){
//        Intent intent = new Intent(getContext(), WebViewActivity.class);
//        startActivity(intent);
        ((WebViewActivity)getActivity()).splashOver();
    }

}
