package com.ycbjie.webviewlib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义带进度条的webView
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class ProgressWebView extends FrameLayout {

    public ProgressWebView(@NonNull Context context) {
        this(context,null);
    }

    public ProgressWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ProgressWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Context context = getContext();
        if (context!=null){
            View view = LayoutInflater.from(context).inflate(
                    R.layout.view_progress_web_view, this, false);
            X5WebView webView = view.findViewById(R.id.web_view);
            final ProgressBar pbProgress = view.findViewById(R.id.pb_progress);
            X5WebChromeClient x5WebChromeClient = new X5WebChromeClient((Activity) context);
            webView.setWebChromeClient(x5WebChromeClient);
            x5WebChromeClient.setWebListener(new InterWebListener() {
                @Override
                public void hindProgressBar() {
                    pbProgress.setVisibility(GONE);
                }

                @Override
                public void showWebView() {

                }

                @Override
                public void hindWebView() {

                }

                @Override
                public void showErrorView() {

                }

                @Override
                public void startProgress(int newProgress) {
                    pbProgress.setProgress(newProgress);
                }
            });
        }
    }


}
