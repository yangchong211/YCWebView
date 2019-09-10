package com.ycbjie.webviewlib;

import android.view.View;


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
     * 进度条变化时调用
     *
     * @param newProgress 进度0-100
     */
    void startProgress(int newProgress);
    /**
     * 添加js监听
     */
    void addJsListener();
}
