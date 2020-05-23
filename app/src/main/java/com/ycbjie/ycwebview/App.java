package com.ycbjie.ycwebview;

import android.app.Application;

import com.ycbjie.webviewlib.X5LogUtils;
import com.ycbjie.webviewlib.X5WebUtils;
import com.ycbjie.ycwebview.cache.WebViewCacheWrapper;
import com.ycbjie.ycwebview.cache.WebViewCacheDelegate;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        X5WebUtils.init(this);
        X5LogUtils.setIsLog(true);

        WebViewCacheDelegate.getInstance().init(new WebViewCacheWrapper.Builder(this));
    }
}
