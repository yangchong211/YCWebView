package com.ycbjie.ycwebview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ycbjie.webviewlib.BridgeHandler;
import com.ycbjie.webviewlib.CallBackFunction;
import com.ycbjie.webviewlib.DefaultHandler;
import com.ycbjie.webviewlib.InterWebListener;
import com.ycbjie.webviewlib.WebProgress;
import com.ycbjie.webviewlib.X5WebUtils;
import com.ycbjie.webviewlib.X5WebView;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/17
 *     desc  : webView页面
 *     revise: 暂时先用假数据替代
 * </pre>
 */
public class NativeActivity extends AppCompatActivity {

    private X5WebView mWebView;
    private WebProgress progress;
    private Button btn;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView.canGoBack() && event.getKeyCode() ==
                KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.clearHistory();
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
            //mWebView = null;
        }
        super.onDestroy();
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_view);
        initData();
        initView();
    }


    public void initData() {

    }

    public void initView() {
        mWebView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorAccent));

        btn = findViewById(R.id.btn);
        mWebView.loadUrl("file:///android_asset/demo.html");
        mWebView.getX5WebChromeClient().setWebListener(interWebListener);
        mWebView.getX5WebViewClient().setWebListener(interWebListener);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.callHandler("functionInJs", "data from Java", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Toast.makeText(NativeActivity.this,"你个逗比" + data,Toast.LENGTH_SHORT).show();
                        Log.i("java调用web----", "reponse data from js " + data);
                    }

                });
                /*具体看demo.html的这段代码
                bridge.registerHandler("functionInJs", function(data, responseCallback) {
                    document.getElementById("show").innerHTML = ("data from Java: = " + data);
                    if (responseCallback) {
                        var responseData = "Javascript Says Right back aka!";
                        responseCallback(responseData);
                    }
                });*/
            }
        });

        //js交互方法
        initWebViewBridge();
    }




    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            progress.hide();
        }

        @Override
        public void showErrorView(@X5WebUtils.ErrorType int type) {
            switch (type){
                //没有网络
                case X5WebUtils.ErrorMode.NO_NET:
                    break;
                //404，网页无法打开
                case X5WebUtils.ErrorMode.STATE_404:

                    break;
                //onReceivedError，请求网络出现error
                case X5WebUtils.ErrorMode.RECEIVED_ERROR:

                    break;
                //在加载资源时通知主机应用程序发生SSL错误
                case X5WebUtils.ErrorMode.SSL_ERROR:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void startProgress(int newProgress) {
            progress.setWebProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //这个是处理回调逻辑
        mWebView.getX5WebChromeClient().uploadMessageForAndroid5(data,resultCode);
    }

    @JavascriptInterface
    public void initWebViewBridge() {
        mWebView.setDefaultHandler(new DefaultHandler());
        //js调native
        mWebView.registerHandler("toPhone", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

            }
        });
        mWebView.registerHandler("submitFromWeb", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Toast.makeText(NativeActivity.this,data+"yc",Toast.LENGTH_LONG).show();
                Log.i("registerHandler", "handler = submitFromWeb, data from web = " + data);
                //这个是回调给web端，比如
                function.onCallBack("submitFromWeb exe, response data 中文 from Java");
            }
        });

        mWebView.callHandler("functionInJs", "小杨逗比", new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(NativeActivity.this,data+"逗比",Toast.LENGTH_LONG).show();
            }
        });
        mWebView.callHandler("functionInJs");
    }

}
