package com.fadada.lib;

import java.net.MalformedURLException;
import java.net.URL;

public class MyClass {
    public static void main(String[] args) {
        URL url;
        try {
            url = new URL("https://t-test.fadada.com/Si18a1MEd");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL地址格式错误");
        }
        System.out.println(url.getProtocol() + "://" + url.getAuthority());
    }
}