/*
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.ycbjie.webviewlib;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 特别重要：主要把js的处理给分离到该类中
 *     revise: 如果要自定义WebViewClient必须要集成此类
 *             demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class MyX5WebViewClient extends X5WebViewClient{

    private Context context;
    public BridgeWebView webView;

    /**
     * 构造方法
     *
     * @param webView 需要传进来webview
     * @param context 上下文
     */
    public MyX5WebViewClient(BridgeWebView webView, Context context) {
        super(webView, context);
        this.webView = webView;
        this.context = context;
    }

    /**
     * 这个方法中可以做拦截
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     * @param view                              view
     * @param url                               链接
     * @return                                  是否自己处理，true表示自己处理
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        X5LogUtils.i("-------shouldOverrideUrlLoading----1---"+url);
        //页面关闭后，直接返回，不要执行网络请求和js方法
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
        if (!activityAlive){
            return false;
        }
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 如果是返回数据
        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
            webView.flushMessageQueue();
            return true;
        } else {
            if (this.onCustomShouldOverrideUrlLoading(url)){
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    protected boolean onCustomShouldOverrideUrlLoading(String url) {
        return false;
    }


    /**
     * 增加shouldOverrideUrlLoading在api>=24时
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     * @param view                              view
     * @param request                           request
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        X5LogUtils.i("-------shouldOverrideUrlLoading----2---"+request.getUrl().toString());
        //页面关闭后，直接返回，不要执行网络请求和js方法
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
        if (!activityAlive){
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String url = request.getUrl().toString();
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            //如果是返回数据
            if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                webView.handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                webView.flushMessageQueue();
                return true;
            } else {
                if (this.onCustomShouldOverrideUrlLoading(url)){
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }
        }else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }


    /**
     * 当页面加载完成会调用该方法
     * @param view                              view
     * @param url                               url链接
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //这个时候添加js注入方法
        //WebViewJavascriptBridge.js
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                //分发message 必须在主线程才分发成功
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }
}