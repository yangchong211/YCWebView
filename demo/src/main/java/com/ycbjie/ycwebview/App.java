package com.ycbjie.ycwebview;

import android.app.Application;

import com.ycbjie.webviewlib.cache.WebViewCacheDelegate;
import com.ycbjie.webviewlib.cache.WebViewCacheWrapper;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.utils.X5WebUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        X5WebUtils.init(this);
        X5LogUtils.setIsLog(true);
    }
}
