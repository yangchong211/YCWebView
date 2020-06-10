package com.ycbjie.ycwebview;

import android.app.Application;


import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.utils.X5WebUtils;
import com.ycbjie.ycwebview.cache.WebViewCacheDelegate;
import com.ycbjie.ycwebview.cache.WebViewCacheWrapper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        X5WebUtils.init(this);
        X5LogUtils.setIsLog(true);
//        X5WebUtils.initCache(this);
        WebViewCacheDelegate.getInstance().init(new WebViewCacheWrapper.Builder(this));
    }
}
