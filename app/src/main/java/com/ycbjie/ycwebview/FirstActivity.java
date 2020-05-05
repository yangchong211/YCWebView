package com.ycbjie.ycwebview;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;

import com.tencent.smtt.sdk.WebBackForwardList;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebHistoryItem;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.ycbjie.webviewlib.WebProgress;
import com.ycbjie.webviewlib.X5LogUtils;

public class FirstActivity extends AppCompatActivity {

    private BaseWebView webView;
    private WebProgress progress;
    private Toolbar bar;

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
        setContentView(R.layout.activity_web_view1);
        webView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        bar = findViewById(R.id.bar);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorAccent),this.getResources().getColor(R.color.colorPrimaryDark));
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        String url = "http://www.baidu.com";
        webView.loadUrl(url);

    }

//    int running = 0;

    private class MyWebViewClient extends WebViewClient {

//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            String s = request.getUrl().toString();
//            X5LogUtils.i("-------shouldOverrideUrlLoading----->21--"+s);
//            //view.loadUrl(s);
//            //return true;
//            return super.shouldOverrideUrlLoading(view, request);
//        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            X5LogUtils.i("-------shouldOverrideUrlLoading-------"+url);
            //判断重定向的方式一
            WebView.HitTestResult hitTestResult = view.getHitTestResult();
            if(hitTestResult == null) {
                return false;
            }
            if(hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
                X5LogUtils.i("-------重定向-------");
                return false;
            }

            if (webView.isTouchByUser()){
                X5LogUtils.i("-------点击事件-------");
            }
            view.loadUrl(url);
            X5LogUtils.i("-----shouldOverrideUrlLoading------loadUrl-------");
//            running++;
            return true;
            //return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            X5LogUtils.i("-------onPageStarted-------"+url);
//            running = Math.max(running, 1);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            X5LogUtils.i("-------onPageFinished-------"+url);
//            if (--running==0) {
//                //做操作，隐藏加载进度条或者加载loading
//                X5LogUtils.i("-------onPageFinished-----结束--");
//            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            X5LogUtils.i("-------onReceivedTitle-------"+title);
            //bar.setTitle(title));
            getWebTitle(view);
//            if (--running==0) {
//                bar.setTitle(title);
//            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progress.setWebProgress(newProgress);
        }
    }

    private void getWebTitle(WebView view){
        WebBackForwardList forwardList = view.copyBackForwardList();
        WebHistoryItem item = forwardList.getCurrentItem();
        if (item != null) {
            bar.setTitle(item.getTitle());
           // X5LogUtils.i("-------onReceivedTitle----getWebTitle---"+item.getTitle());
        }
    }
}
