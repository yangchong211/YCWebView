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

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义x5的WebViewClient
 *     revise: 如果要自定义WebViewClient必须要集成此类
 *             demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class X5WebViewClient extends WebViewClient {

    private InterWebListener webListener;
    private BridgeWebView webView;

    /**
     * 设置监听时间，包括常见状态页面切换，进度条变化等
     * @param listener                          listener
     */
    public void setWebListener(InterWebListener listener){
        this.webListener = listener;
    }

    /**
     * 构造方法
     * @param webView                           需要传进来webview
     * @param context                           上下文
     */
    public X5WebViewClient(BridgeWebView webView, Context context) {
        this.webView = webView;
        //将js对象与java对象进行映射
        webView.addJavascriptInterface(new ImageJavascriptInterface(context), "imagelistener");
    }

    /**
     * 这个方法中可以做拦截
     * @param view                              view
     * @param url                               链接
     * @return                                  是否自己处理，true表示自己处理
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
     * @param view                              view
     * @param request                           request
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String url = request.getUrl().toString();
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
        if (!X5WebUtils.isConnected(webView.getContext()) && webListener!=null) {
            //隐藏进度条方法
            webListener.hindProgressBar();
        }
        super.onPageFinished(view, url);
        //在html标签加载完成之后在加载图片内容
        webView.getSettings().setBlockNetworkImage(false);
        //这个时候添加js注入方法
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
        //html加载完成之后，添加监听图片的点击js函数
        //addImageClickListener();
        addImageClickListener(webView);
    }

    /**
     * 请求网络出现error
     * @param view                              view
     * @param errorCode                         错误🐎
     * @param description                       description
     * @param failingUrl                        失败链接
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            String data = "Page NO FOUND！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        }
    }

    /**
     * 当缩放改变的时候会调用该方法
     * @param view                              view
     * @param oldScale                          之前的缩放比例
     * @param newScale                          现在缩放比例
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }

    // 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
    // 值得注意的是，不同的是过时的版本的回调，新的版本将被称为任何资源（iframe，图像等）
    // 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
    // 6.0 之后
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            X5WebUtils.log("服务器异常"+error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        if (webListener!=null){
            webListener.showErrorView();
        }
    }


    /**
     * 通知主机应用程序在加载资源时从服务器接收到HTTP错误
     * @param view                              view
     * @param request                           request
     * @param errorResponse                     错误内容
     */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }


    /**
     * 通知主机应用程序已自动处理用户登录请求
     * @param view                              view
     * @param realm                             数据
     * @param account                           account
     * @param args                              args
     */
    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    /**
     * 在加载资源时通知主机应用程序发生SSL错误
     * @param view                              view
     * @param handler                           handler
     * @param error                             error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        if (error!=null){
            String url = error.getUrl();
            X5WebUtils.log("onReceivedSslError----异常url----"+url);
        }
        //https忽略证书问题
        if (handler!=null){
            handler.proceed();
        }
    }

    /**
     * android与js交互：
     * 首先我们拿到html中加载图片的标签img.
     * 然后取出其对应的src属性
     * 循环遍历设置图片的点击事件
     * 将src作为参数传给java代码
     * 在java回调方法中对界面进行跳转处理，用PhotoView加载大图实现，便于手势的操作
     * 这个循环将所图片放入数组，当js调用本地方法时传入。
     * 当然如果采用方式一获取图片的话，本地方法可以不需要传入这个数组
     * //通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     * @param webView                       webview
     */
    private void addImageArrayClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "var array=new Array(); " +
                "for(var j=0;j<objs.length;j++){" +
                "    array[j]=objs[j].src; " +
                "}"+
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src,array);  " +
                "    }  " +
                "}" +
                "})()");
    }

    /**
     * 通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     * @param webView                       webview
     */
    private void addImageClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }
}

