package com.ycbjie.ycwebview.cache;

import android.annotation.TargetApi;
import android.os.Build;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

//委派类
public class WebViewCacheDelegate implements WebViewRequestClient {

    private static volatile WebViewCacheDelegate webViewCacheInterceptorInst;

    private WebViewRequestClient mInterceptor;

    public void init(WebViewCacheWrapper.Builder builder){
        if (builder!=null){
            mInterceptor =  builder.build();
        }
    }

    public static WebViewCacheDelegate getInstance(){
        if (webViewCacheInterceptorInst==null){
            synchronized (WebViewCacheDelegate.class){
                if (webViewCacheInterceptorInst == null){
                    webViewCacheInterceptorInst = new WebViewCacheDelegate();
                }
            }
        }
        return webViewCacheInterceptorInst;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse interceptRequest(WebResourceRequest request) {
        if (mInterceptor==null){
            return null;
        }
        return mInterceptor.interceptRequest(request);
    }

    @Override
    public WebResourceResponse interceptRequest(String url) {
        if (mInterceptor==null){
            return null;
        }
        return mInterceptor.interceptRequest(url);
    }

    @Override
    public void loadUrl(WebView webView, String url) {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.loadUrl(webView,url);
    }

    @Override
    public void loadUrl(String url, String userAgent) {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.loadUrl(url,userAgent);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders, String userAgent) {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.loadUrl(url,additionalHttpHeaders,userAgent);
    }

    @Override
    public void loadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.loadUrl(webView,url,additionalHttpHeaders);
    }

    @Override
    public void clearCache() {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.clearCache();
    }

    @Override
    public void enableForce(boolean force) {
        if (mInterceptor==null){
            return ;
        }
        mInterceptor.enableForce(force);
    }

    @Override
    public InputStream getCacheFile(String url) {
        if (mInterceptor==null){
            return null;
        }
        return mInterceptor.getCacheFile(url);
    }

    @Override
    public void initAssetsData() {
        if (mInterceptor!=null){
            mInterceptor.initAssetsData();
        }
    }

    @Override
    public File getCachePath() {
        if (mInterceptor==null){
            return null;
        }
        return mInterceptor.getCachePath();
    }
}
