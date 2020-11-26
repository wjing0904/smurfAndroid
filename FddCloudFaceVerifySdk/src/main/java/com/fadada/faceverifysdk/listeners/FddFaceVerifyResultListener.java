package com.fadada.faceverifysdk.listeners;

public interface FddFaceVerifyResultListener {
    void onVerifySuccess();

    void onVerifyFailed();

    void onVerifyCancel();
}
