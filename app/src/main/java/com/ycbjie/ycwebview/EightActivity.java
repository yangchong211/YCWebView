package com.ycbjie.ycwebview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.BridgeUtil;
import com.ycbjie.webviewlib.BridgeWebView;
import com.ycbjie.webviewlib.X5WebChromeClient;
import com.ycbjie.webviewlib.X5WebView;
import com.ycbjie.webviewlib.X5WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class EightActivity extends AppCompatActivity {

    private X5WebView webView;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
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
            if (webView != null) {
                webView.stopLoading();
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
        String url = "https://juejin.im/post/5d401cabf265da03a53a12fe";
        webView.loadUrl(url);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return handleLongImage();
            }
        });
        MyX5WebViewClient webViewClient = new MyX5WebViewClient(webView, this);
        webView.setWebViewClient(webViewClient);
        MyX5WebChromeClient webChromeClient = new MyX5WebChromeClient(this);
        webView.setWebChromeClient(webChromeClient);
    }


    private class MyX5WebViewClient extends X5WebViewClient {
        public MyX5WebViewClient(BridgeWebView webView, Context context) {
            super(webView, context);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("网络拦截--------1------",url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("网络拦截--------2------",request.getUrl().toString());
            return super.shouldOverrideUrlLoading(view, request);
        }
    }


    private class MyX5WebChromeClient extends X5WebChromeClient{

        /**
         * 构造方法
         *
         * @param activity 上下文
         */
        public MyX5WebChromeClient(Activity activity) {
            super(activity);
        }
    }

    /**
     * 长按图片事件处理
     */
    private boolean handleLongImage() {
        final com.tencent.smtt.sdk.WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        // 如果是图片类型或者是带有图片链接的类型
        if (hitTestResult.getType() == com.tencent.smtt.sdk.WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == com.tencent.smtt.sdk.WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // 弹出保存图片的对话框
            new AlertDialog.Builder(EightActivity.this)
                    .setItems(new String[]{"查看大图", "保存图片到相册"},
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String picUrl = hitTestResult.getExtra();
                            //获取图片
                            Log.e("picUrl", picUrl);
                            switch (which) {
                                case 0:

                                    break;
                                case 1:
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }


}
