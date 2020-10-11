package com.smurf.app.upgrade;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smurf.app.R;


public class UpgradeDialog extends Dialog implements View.OnClickListener {
    private TextView contextTxt;
    private TextView versionCodeTxt;
    private UpgradeNormalListener mUpgradeNormalListener;
    private LinearLayout upgradeLayout;
    private CouponBean couponBean;
    private int versionCode;

    public UpgradeDialog(Context context,CouponBean couponBean,int versionCode) {
        super(context, R.style.public_dialog);
        this.couponBean = couponBean;
        this.versionCode = versionCode;
    }

    public UpgradeDialog setUpgradeNormalListener(UpgradeNormalListener upgradeNormalListener) {
        mUpgradeNormalListener = upgradeNormalListener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dialog_upgrade);
        setCanceledOnTouchOutside(false);
        initView();
        initData();
    }

    private void initView() {
        contextTxt = findViewById(R.id.updataversion_msg);
        versionCodeTxt = findViewById(R.id.updataversioncode);
        upgradeLayout = findViewById(R.id.dialog_sure);
        upgradeLayout.setOnClickListener(this);
    }

    private  void initData() {
        if (couponBean != null) {
            CouponBean.DataBean data = couponBean.getData();
            if (data != null && contextTxt != null) {
                contextTxt.setText(data.getExplain());
            }
        }

        if (versionCodeTxt != null) {
            versionCodeTxt.setText(String.valueOf(versionCode));
        }

    }

    @Override
    public void onClick(View view) {
        if(mUpgradeNormalListener!= null) {
            if (couponBean != null) {
                CouponBean.DataBean data = couponBean.getData();
                if (data != null && contextTxt != null) {
                    mUpgradeNormalListener.upgradeForce(data.getUrlX());
                }
            };

        }
    }

    public interface UpgradeNormalListener {
        void upgradeForce(String installUrl);
    }

}
