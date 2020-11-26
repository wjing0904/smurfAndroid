package com.fadada.faceverifysdk;


import java.net.MalformedURLException;
import java.net.URL;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    public static void main(String[] args) {
        URL url;
        try {
            url = new URL("http://d.android.com/tools/testing");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL地址格式错误");
        }
        System.out.println(url.getHost());
    }
}