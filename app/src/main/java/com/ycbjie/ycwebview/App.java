package com.ycbjie.ycwebview;

import android.app.Application;

import com.ycbjie.webviewlib.X5LogUtils;
import com.ycbjie.webviewlib.X5WebUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        X5WebUtils.init(this);
        X5LogUtils.setIsLog(false);
    }
}
