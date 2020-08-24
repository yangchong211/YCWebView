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
package com.ycbjie.webviewlib.client;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.tls.WebTlsHelper;
import com.ycbjie.webviewlib.utils.EncodeUtils;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.utils.X5WebUtils;
import com.ycbjie.webviewlib.view.X5WebView;
import com.ycbjie.webviewlib.base.X5WebViewClient;
import com.ycbjie.webviewlib.bridge.WebJsMessage;
import com.ycbjie.webviewlib.bridge.BridgeUtil;
import com.ycbjie.webviewlib.view.BridgeWebView;

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
public class JsX5WebViewClient extends X5WebViewClient {

    private Context context;
    private X5WebView mWebView;
    private HttpDnsService httpDns ;
    private WebTlsHelper tlsHelper;

    /**
     * 构造方法
     *
     * @param webView 需要传进来webview
     * @param context 上下文
     */
    public JsX5WebViewClient(X5WebView webView, Context context) {
        super(webView, context);
        this.mWebView = webView;
        this.context = context;
        if (X5WebUtils.isHttpDns && httpDns==null){
            httpDns = webView.getHttpDns();
            tlsHelper = new WebTlsHelper(httpDns);
        }
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
        url = EncodeUtils.urlDecode(url);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        // 如果是返回数据
        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
            mWebView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
            mWebView.flushMessageQueue();
            return true;
        } else {
            if (this.onCustomShouldOverrideUrlLoading(url)){
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    /**
     * 子类可以自己去实现
     * @param url                               url链接
     * @return
     */
    public boolean onCustomShouldOverrideUrlLoading(String url) {
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
            url = EncodeUtils.urlDecode(url);
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            //如果是返回数据
            if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                mWebView.handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                mWebView.flushMessageQueue();
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

        //对于Android调用js。有两种业务场景：
        //第一种是在onPageFinished方法之后，调用callHandler方法，则在BridgeWebView类中，会直接dispatchMessage处理
        //第二种是在onPageFinished方法之前，比如你在创建webView时候，就调用了n个callHandler方法。则会在下面代码中for循环dispatchMessage处理
        //针对第二种情况，如何m集合中数量多，比如几十个，为了避免Android调用js方法evaluateJavascript每一个都调用，这里使用handler发消息来加入队列来达到需求
        if (mWebView.getStartupMessage() != null) {
            for (WebJsMessage m : mWebView.getStartupMessage()) {
                //分发message 必须在主线程才分发成功
                mWebView.dispatchMessage(m);
            }
            mWebView.setStartupMessage(null);
        }
    }

    /**
     * 此方法添加于API21，调用于非UI线程，拦截资源请求并返回数据，返回null时WebView将继续加载资源
     *
     * 其中 WebResourceRequest 封装了请求，WebResourceResponse 封装了响应
     * 封装了一个Web资源的响应信息，包含：响应数据流，编码，MIME类型，API21后添加了响应头，状态码与状态描述
     * @param webView                           view
     * @param webResourceRequest                request，添加于API21，封装了一个Web资源的请求信息，
     *                                          包含：请求地址，请求方法，请求头，是否主框架，是否用户点击，是否重定向
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        if (tlsHelper!=null){
            return tlsHelper.shouldInterceptRequest(mWebView,webResourceRequest);
        }
        return super.shouldInterceptRequest(webView, webResourceRequest);
    }

}