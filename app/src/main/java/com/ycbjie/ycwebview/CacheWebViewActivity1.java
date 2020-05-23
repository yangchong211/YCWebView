package com.ycbjie.ycwebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.InterWebListener;
import com.ycbjie.webviewlib.MyX5WebViewClient;
import com.ycbjie.webviewlib.WebProgress;
import com.ycbjie.webviewlib.X5WebUtils;
import com.ycbjie.webviewlib.X5WebView;
import com.ycbjie.ycwebview.cache.WebResponseAdapter;
import com.ycbjie.ycwebview.cache.WebViewCacheDelegate;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/17
 *     desc  : webView页面
 *     revise: 暂时先用假数据替代
 * </pre>
 */
public class CacheWebViewActivity1 extends AppCompatActivity {

    private X5WebView mWebView;
    private WebProgress progress;
    private String url;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() ==
                KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mWebView.pageCanGoBack()) {
                //退出网页
                return mWebView.pageGoBack();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            //mWebView = null;
        }
        super.onDestroy();
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initData();
        initView();
    }


    public void initData() {
        Intent intent = getIntent();
        if (intent!=null){
            url = intent.getStringExtra("url");
        }
    }

    public void initView() {
        mWebView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorAccent),this.getResources().getColor(R.color.colorPrimaryDark));

        YcX5WebViewClient webViewClient = new YcX5WebViewClient(mWebView, this);
        mWebView.setWebViewClient(webViewClient);
        mWebView.loadUrl(CacheWebViewActivity2.url);
        mWebView.getX5WebChromeClient().setWebListener(interWebListener);
        mWebView.getX5WebViewClient().setWebListener(interWebListener);
    }


    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            progress.hide();
        }

        @Override
        public void showErrorView(@X5WebUtils.ErrorType int type) {
            switch (type){
                //没有网络
                case X5WebUtils.ErrorMode.NO_NET:
                    break;
                //404，网页无法打开
                case X5WebUtils.ErrorMode.STATE_404:

                    break;
                //onReceivedError，请求网络出现error
                case X5WebUtils.ErrorMode.RECEIVED_ERROR:

                    break;
                //在加载资源时通知主机应用程序发生SSL错误
                case X5WebUtils.ErrorMode.SSL_ERROR:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void startProgress(int newProgress) {
            progress.setWebProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }
    };

    private class YcX5WebViewClient extends MyX5WebViewClient {
        public YcX5WebViewClient(X5WebView webView, Context context) {
            super(webView, context);
        }


        /**
         * 此方法废弃于API21，调用于非UI线程，拦截资源请求并返回响应数据，返回null时WebView将继续加载资源
         * 注意：API21以下的AJAX请求会走onLoadResource，无法通过此方法拦截
         *
         * 其中 WebResourceRequest 封装了请求，WebResourceResponse 封装了响应
         * 封装了一个Web资源的响应信息，包含：响应数据流，编码，MIME类型，API21后添加了响应头，状态码与状态描述
         * @param webView                           view
         * @param s                                 s
         */
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
//            X5LogUtils.i("WebView-------shouldInterceptRequest------1--"+s);
//            WebResourceResponse request = WebViewCacheInterceptorInst.getInstance().interceptRequest(s);
//            return request;
//            //return super.shouldInterceptRequest(webView, s);
//        }

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
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
//        /*Map<String, String> requestHeaders = webResourceRequest.getRequestHeaders();
//        Set<Map.Entry<String, String>> entries = requestHeaders.entrySet();
//        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
//        while (iterator.hasNext()){
//            Map.Entry<String, String> next = iterator.next();
//            String key = next.getKey();
//            String value = next.getValue();
//            WebViewUtils.i("-------shouldInterceptRequest---查看请求头信息----"+key+"----"+value);
//        }*/
//            WebResourceResponse request = WebViewCacheInterceptorInst.getInstance().interceptRequest(webResourceRequest);
//            if (webResourceRequest!=null && webResourceRequest.getUrl()!=null){
//                X5LogUtils.i("WebView-------shouldInterceptRequest------2--"+webResourceRequest.getUrl());
//            }
//            return request;
//            //return super.shouldInterceptRequest(webView, webResourceRequest);
//        }


        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
            WebResourceResponse request = WebViewCacheDelegate.getInstance().interceptRequest(s);
            return WebResponseAdapter.adapter(request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            WebResourceResponse request = WebViewCacheDelegate.getInstance().
                    interceptRequest(webResourceRequest);
            return WebResponseAdapter.adapter(request);
        }

    }
}
