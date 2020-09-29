package com.smurf.app.view;

/**
 * 原生通知 js 方法
 */

public interface IWebViewInterface {

    void notifyZxingValueToJs(String value);

    void notifyImageSelectedValueToJs(String value);
}
