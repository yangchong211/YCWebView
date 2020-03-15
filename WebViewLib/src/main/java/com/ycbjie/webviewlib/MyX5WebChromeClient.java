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

import android.app.Activity;

import com.tencent.smtt.sdk.WebView;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 主要把js的处理给分离到该类中
 *     revise: 如果要自定义WebChromeClient必须要集成此类
 *             demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class MyX5WebChromeClient extends X5WebChromeClient{

    private boolean isShowContent = false;
    public BridgeWebView webView;

    /**
     * 构造方法
     *
     * @param activity 上下文
     */
    public MyX5WebChromeClient(BridgeWebView webView, Activity activity) {
        super(webView, activity);
        this.webView = webView;
    }

    /**
     * 这个方法是监听加载进度变化的，当加载到百分之八十五的时候，页面一般就出来呢
     * 作用：获得网页的加载进度并显示
     * @param view                              view
     * @param newProgress                       进度值
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        /*int max = 85;
        if (newProgress> max && !isShowContent){
            //在这个时候添加js注入方法，具体可以看readme文档
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
            if (webView.getStartupMessage() != null) {
                for (Message m : webView.getStartupMessage()) {
                    webView.dispatchMessage(m);
                }
                webView.setStartupMessage(null);
            }
            isShowContent = true;
        }*/
    }
}
