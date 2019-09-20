package com.ycbjie.ycwebview;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.ycbjie.webviewlib.VideoWebListener;
import com.ycbjie.webviewlib.X5WebChromeClient;
import com.ycbjie.webviewlib.X5WebView;
import com.ycbjie.webviewlib.X5WebViewClient;

public class FiveActivity extends AppCompatActivity {

    private X5WebView webView;
    private X5WebChromeClient x5WebChromeClient;
    private X5WebViewClient x5WebViewClient;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //全屏播放退出全屏
            if (x5WebChromeClient!=null && x5WebChromeClient.inCustomView()) {
                x5WebChromeClient.hideCustomView();
                return true;
                //返回网页上一页
            } else if (webView.canGoBack()) {
                webView.goBack();
                return true;
                //退出网页
            } else {
                handleFinish();
            }
        }
        return false;
    }

    public void handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        try {
            if (x5WebChromeClient!=null){
                x5WebChromeClient.removeVideoView();
            }
            //有音频播放的web页面的销毁逻辑
            //在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview
            //但是注意：webview调用destory时,webview仍绑定在Activity上
            //这是由于自定义webview构建时传入了该Activity的context对象
            //因此需要先从父容器中移除webview,然后再销毁webview:
            if (webView != null) {
                ViewGroup parent = (ViewGroup) webView.getParent();
                if (parent != null) {
                    parent.removeView(webView);
                }
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            Log.e("X5WebViewActivity", e.getMessage());
        }
        super.onDestroy();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        webView = findViewById(R.id.web_view);

        String movieUrl = "https://sv.baidu.com/videoui/page/videoland?context=%7B%22nid%22%3A%22sv_5861863042579737844%22%7D&pd=feedtab_h5";
        webView.loadUrl(movieUrl);
        x5WebChromeClient = webView.getX5WebChromeClient();
        x5WebViewClient = webView.getX5WebViewClient();
        x5WebChromeClient.setVideoWebListener(new VideoWebListener() {
            @Override
            public void showVideoFullView() {
                //视频全频播放时监听
            }

            @Override
            public void hindVideoFullView() {
                //隐藏全频播放，也就是正常播放视频
            }

            @Override
            public void showWebView() {
                //显示webView
            }

            @Override
            public void hindWebView() {
                //隐藏webView
            }
        });
    }
}
