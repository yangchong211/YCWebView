package com.ycbjie.webviewlib;

import android.support.annotation.IntRange;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : web的接口回调，包括常见状态页面切换，进度条变化等
 *     revise:
 * </pre>
 */
public interface InterWebListener {

    /**
     * 隐藏进度条
     */
    void hindProgressBar();

    /**
     * 显示webview
     */
    void showWebView();

    /**
     * 隐藏webview
     */
    void hindWebView();

    /**
     * 展示异常页面
     */
    void showErrorView();

    /**
     * 进度条变化时调用，这里添加注解限定符，必须是在0到100之间
     * @param newProgress 进度0-100
     */
    void startProgress(@IntRange(from = 0,to = 100) int newProgress);

}
