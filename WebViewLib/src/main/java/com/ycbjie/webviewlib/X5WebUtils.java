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


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : WebView工具类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public final class X5WebUtils {

    /**
     * 不能直接new，否则抛个异常
     */
    private X5WebUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化腾讯x5浏览器webView，建议在application初始化
     * @param context                       上下文
     */
    public static void init(Context context){
        if(context instanceof Application){
            //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
            QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
                @Override
                public void onViewInitFinished(boolean arg0) {
                    //x5內核初始化完成的回调，为true表示x5内核加载成功
                    //否则表示x5内核加载失败，会自动切换到系统内核。
                    Log.d("app", " onViewInitFinished is " + arg0);
                }

                @Override
                public void onCoreInitFinished() {
                    Log.d("app", " onCoreInitFinished ");
                }
            };
            //x5内核初始化接口
            QbSdk.initX5Environment(context,  cb);
        }else {
            throw new UnsupportedOperationException("context must be application...");
        }
    }


    /**
     * 判断网络是否连接
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isConnected(Context context) {
        if (context==null){
            return false;
        }
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return NetworkInfo
     */
    @SuppressLint("MissingPermission")
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }
        return manager.getActiveNetworkInfo();
    }

    /**
     * 同步cookie
     * 建议调用webView.loadUrl(url)之前一句调用此方法就可以给WebView设置Cookie
     * @param url                   地址
     * @param cookieList            需要添加的Cookie值,以键值对的方式:key=value
     */
    public static void syncCookie(Context context , String url, ArrayList<String> cookieList) {
        //初始化
        CookieSyncManager.createInstance(context);
        //获取对象
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //移除
        cookieManager.removeSessionCookie();
        //添加cookie操作
        if (cookieList != null && cookieList.size() > 0) {
            for (String cookie : cookieList) {
                cookieManager.setCookie(url, cookie);
            }
        }
        String cookies = cookieManager.getCookie(url);
        X5LogUtils.d("cookies-------"+cookies);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }

    /**
     * 清除cookie操作
     * @param context               上下文
     */
    public static void removeCookie(Context context){
        CookieSyncManager.createInstance(context);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().removeSessionCookie();
    }

    /**
     * Return whether the activity is alive.
     *
     * @param context The context.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Context context) {
        return isActivityAlive(getActivityByContext(context));
    }

    /**
     * Return the activity by context.
     *
     * @param context The context.
     * @return the activity by context.
     */
    public static Activity getActivityByContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    /**
     * Return whether the activity is alive.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }



    /**
     * 注解限定符
     */
    @IntDef({ErrorMode.NO_NET,ErrorMode.STATE_404, ErrorMode.RECEIVED_ERROR, ErrorMode.SSL_ERROR,
            ErrorMode.TIME_OUT,ErrorMode.STATE_500})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorType{}

    /**
     * 异常状态模式
     * NO_NET                       没有网络
     * STATE_404                    404，网页无法打开
     * RECEIVED_ERROR               onReceivedError，请求网络出现error
     * SSL_ERROR                    在加载资源时通知主机应用程序发生SSL错误
     * TIME_OUT                     网络连接超时
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorMode {
        int NO_NET = 1001;
        int STATE_404 = 1002;
        int RECEIVED_ERROR = 1003;
        int SSL_ERROR = 1004;
        int TIME_OUT = 1005;
        int STATE_500 = 1006;
    }

    /**
     * 截屏，截取activity的可见区域的视图
     * @param activity              上下文
     * @return                      bitmap图片
     */
    public static Bitmap activityShot(Activity activity) {
        /*获取windows中最顶层的view*/
        View view = activity.getWindow().getDecorView();
        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        WindowManager windowManager = activity.getWindowManager();
        //获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        //获取view的缓存位图
        Bitmap drawingCache = view.getDrawingCache();
        //去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(drawingCache, 0, statusBarHeight,
                width, height - statusBarHeight);
        //销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 计算view的大小，测量宽高大小。万能工具方法，可以截屏长图
     * @param activity              上下文
     * @param view                  view
     * @return                      返回bitmap
     */
    public static Bitmap measureSize(Activity activity, View view) {
        //将布局转化成view对象
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        //然后View和其内部的子View都具有了实际大小，也就是完成了布局，相当与添加到了界面上。
        //接着就可以创建位图并在上面绘制
        return layoutView(view, width, height);
    }

    /**
     * 填充布局内容
     * @param viewBitmap            view
     * @param width                 宽
     * @param height                高
     * @return
     */
    private static  Bitmap layoutView(final View viewBitmap, int width, int height) {
        // 整个View的大小 参数是左上角 和右下角的坐标
        viewBitmap.layout(0, 0, width, height);
        //宽，父容器已经检测出view所需的精确大小，这时候view的最终大小SpecSize所指定的值，相当于match_parent或指定具体数值。
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        //高，表示父容器不对View有任何限制，一般用于系统内部，表示一种测量状态；
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED);
        viewBitmap.measure(measuredWidth, measuredHeight);
        viewBitmap.layout(0, 0, viewBitmap.getMeasuredWidth(), viewBitmap.getMeasuredHeight());
        viewBitmap.setDrawingCacheEnabled(true);
        viewBitmap.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        viewBitmap.setDrawingCacheBackgroundColor(Color.WHITE);
        // 把一个View转换成图片
        Bitmap cachebmp = viewConversionBitmap(viewBitmap);
        viewBitmap.destroyDrawingCache();
        return cachebmp;
    }

    /**
     * view转bitmap，如果是截图控件中有scrollView，那么可以取它的子控件LinearLayout或者RelativeLayout
     * @param v                     view
     * @return                      bitmap对象
     */
    private static Bitmap viewConversionBitmap(View v) {
        int w = v.getWidth();
        int h = 0;
        if (v instanceof LinearLayout){
            LinearLayout linearLayout = (LinearLayout) v;
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                h += linearLayout.getChildAt(i).getHeight();
            }
        } else if (v instanceof RelativeLayout){
            RelativeLayout relativeLayout = (RelativeLayout) v;
            for (int i = 0; i < relativeLayout.getChildCount(); i++) {
                h += relativeLayout.getChildAt(i).getHeight();
            }
        } else if (v instanceof CoordinatorLayout){
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) v;
            for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
                h += coordinatorLayout.getChildAt(i).getHeight();
            }
        } else {
            h = v.getHeight();
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        //如果不设置canvas画布为白色，则生成透明
        c.drawColor(Color.WHITE);
        v.layout(0, 0, w, h);
        v.draw(c);
        return bmp;
    }

}
