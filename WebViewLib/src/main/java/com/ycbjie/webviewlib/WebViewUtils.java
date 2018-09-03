package com.ycbjie.webviewlib;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2018/5/6
 *     desc  : WebView工具类
 *     revise: 潇湘剑雨，持续更新，欢迎各位同行提出问题和建议
 * </pre>
 */
public class WebViewUtils {


    /**
     * 不能直接new，否则抛个异常
     */
    private WebViewUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    /**
     * 初始化WebView
     * @param context                   注意，必须是全局上下文，否则报错
     */
    public static void initWebView(Context context){
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


}
