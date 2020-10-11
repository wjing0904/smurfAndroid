package com.smurf.app.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smurf.app.R;

import org.w3c.dom.Text;


public class Shareboard extends PopupWindow {

    private static final String TAG = "Shareboard";
    private Context mContext;
    private ImageView wechatImg;
    private ImageView wxCircleImg;
    private TextView cancelTxt;
    private ShareWeChatListener shareWeChatListener;

    public Shareboard(Context context) {
        super(context);
        this.mContext = context;
        init();
    }


    private void init() {
        View rl_root = View.inflate(mContext, R.layout.share_wechat_view , null);
        setContentView(rl_root);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        wechatImg = rl_root.findViewById(R.id.share_socialize_wechat);
        wxCircleImg = rl_root.findViewById(R.id.share_socialize_wxcircle);
        cancelTxt = rl_root.findViewById(R.id.tv_cancel);
        cancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        wechatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shareWeChatListener!= null)
                    shareWeChatListener.shareWeChat(0);
            }
        });

        wxCircleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shareWeChatListener!= null)
                    shareWeChatListener.shareWeChat(1);
            }
        });
    }

    public void setShareWeChatListener(ShareWeChatListener shareWeChatListener){
        this.shareWeChatListener = shareWeChatListener;
    }

    public void show() {
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

}
