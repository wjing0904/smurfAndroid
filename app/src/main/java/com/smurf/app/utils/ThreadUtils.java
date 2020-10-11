package com.smurf.app.utils;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ThreadUtils {
    public static final String TAG = "ThreadUtils";

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    /**
     * 主线程中执行
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        sHandler.post(runnable);
    }

    /**
     * 在子线程中执行任务
     * 如，可将DB的一些操作置入action中
     *
     * tips: 本方法，正常执行后，内部返回true，
     * @param action
     * @return
     */
    public static void doOnIOThread(Action action) {
        Observable.create(e -> {
            action.run();
//            e.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.io()).subscribe(l->{

        }, e -> {
            e.printStackTrace();
        });
    }

    /**
     * 在子线程中执行任务
     * 如，可将DB的一些操作置入action中
     *
     * tips: 本方法，正常执行后，内部返回true，
     * @param action
     * @param onError
     * @return
     */
    public static void doOnIOThread(Action action, Consumer<? super Throwable> onError) {
        Observable.create(e -> {
            action.run();
//            e.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.io()).subscribe(l -> {

        }, onError);
    }

    /**
     * 执行异步任务
     * @param action
     * @param consumer
     */
    public static void doAsyncTask(Action action, Consumer consumer) {
        Observable.create(e -> {
            action.run();
            e.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(consumer, e -> {
            e.printStackTrace();
        });
    }

    /**
     * 执行异步任务
     * @param action
     * @param consumer
     * @param onError
     */
    public static void doAsyncTask(Action action, Consumer consumer, Consumer<? super Throwable> onError) {
        Observable.create(e -> {
            action.run();
            e.onNext(Boolean.TRUE);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(consumer, onError);
    }

    /**
     * 与上例类似， 区别在于， 可以将 T 变换 为 R
     *
     * (Function类似的 类还有 Function3、Function4...  等， 需要多个T型 形参)
     * @param function
     * @param t
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Observable<R> opDb(Function<T, R> function, T t) {
        return Observable.create(e -> e.onNext(function.apply(t)));
    }

}
