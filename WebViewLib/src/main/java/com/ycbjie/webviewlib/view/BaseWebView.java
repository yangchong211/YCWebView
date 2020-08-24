package com.ycbjie.webviewlib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.base.RequestInfo;
import com.ycbjie.webviewlib.utils.FastClickUtils;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.utils.X5WebUtils;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义WebView的base类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class BaseWebView extends WebView {

    private static final int EXEC_SCRIPT = 1;
    private static final int LOAD_URL = 2;
    private static final int LOAD_URL_WITH_HEADERS = 3;
    /**
     * loadUrl方法在19以上超过2097152个字符失效
     */
    private static final int URL_MAX_CHARACTER_NUM = 2097152;
    private MyHandler mainThreadHandler = null;

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {

        private WeakReference<Context> mContextReference;

        MyHandler(Context context) {
            super(Looper.getMainLooper());
            mContextReference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final Context context = mContextReference.get();
            if (context != null) {
                switch (msg.what) {
                    case EXEC_SCRIPT:
                        evaluateJavascriptUrl((String) msg.obj);
                        break;
                    case LOAD_URL:
                        BaseWebView.super.loadUrl((String) msg.obj);
                        break;
                    case LOAD_URL_WITH_HEADERS:
                        RequestInfo info = (RequestInfo) msg.obj;
                        BaseWebView.super.loadUrl(info.url, info.headers);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public BaseWebView(Context context) {
        super(context);
        init();
    }

    public BaseWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public BaseWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        mainThreadHandler = new MyHandler(getContext());
    }

    /**
     * 暂停所有布局，解析，和所有webView的JavaScript计时器。
     * 这是一个全局请求，不局限于这个WebView。如果应用程序已暂停，这将非常有用。
     */
    @Override
    public void pauseTimers() {
        super.pauseTimers();
    }

    /**
     * 停止加载
     */
    @Override
    public void stopLoading() {
        super.stopLoading();
    }

    /**
     * 尽最大努力暂停任何可以安全暂停的处理，比如动画和地理定位。
     * 注意，这个调用不会暂停JavaScript。使用{@link # pausetimer}全局暂停JavaScript。
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 在先前调用{@link #onPause}后恢复WebView。
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 获取当前页的进度
     * @return                      当前页的进度在0到100之间
     */
    @Override
    public int getProgress() {
        return super.getProgress();
    }

    /**
     * 获取HTML内容的高度
     * @return                      HTML内容的高度
     */
    @Override
    public int getContentHeight() {
        return super.getContentHeight();
    }

    /**
     * 获取当前页面的原始URL。这并不总是与传递给WebViewClient的URL相同。
     * onPageStarted，因为虽然加载URL已经开始，当前页面可能没有改变。另外，可能有重定向导致到最初请求的不同URL。
     * @return                      链接url
     */
    @Override
    public String getOriginalUrl() {
        return super.getOriginalUrl();
    }

    /**
     * 获取当前页面的URL。这并不总是与传递给WebViewClient的URL相同。
     * onPageStarted，因为虽然加载URL已经开始，当前页面可能没有改变。
     * @return                      链接ul
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    /**
     * 重新加载
     */
    @Override
    public void reload() {
        //防止短时间多次触发load方法
        if (FastClickUtils.isFastDoubleClick()){
            return;
        }
        if (X5WebUtils.isMainThread()){
            super.reload();
        } else {
            Handler handler = getHandler();
            if (handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.reload();
                    }
                });
            }
            //throw new WebViewException("Please load in the main thread");
        }
    }

    /**
     * 页面可见开启js交互
     */
    public void resume(){
        this.getSettings().setJavaScriptEnabled(true);
    }

    /**
     * 页面不可见关闭js交互
     */
    public void stop() {
        this.getSettings().setJavaScriptEnabled(false);
    }

    private void evaluateJavascriptUrl(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //BaseWebView.super.evaluateJavascript(script, null);
            BaseWebView.super.evaluateJavascript(script, new ValueCallback<String>(){
                @Override
                public void onReceiveValue(String s) {
                    X5LogUtils.i("---evaluateJavascript-1--"+s);
                }
            });
        } else {
            //super.loadUrl("javascript:" + script);
            if (script.length()>=URL_MAX_CHARACTER_NUM){
                BaseWebView.super.evaluateJavascript(script, new ValueCallback<String>(){
                    @Override
                    public void onReceiveValue(String s) {
                        X5LogUtils.i("---evaluateJavascript-2--"+s);
                    }
                });
            } else {
                BaseWebView.super.loadUrl(script);
            }
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     * @param script
     */
    public void evaluateJavascript(final String script) {
        if (script==null || script.length()==0){
            return;
        }
        Message msg = mainThreadHandler.obtainMessage(EXEC_SCRIPT, script);
        mainThreadHandler.sendMessage(msg);
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     * @param script
     */
    public void evaluateJavascript(final String script, final ValueCallback<String> callback) {
        if (script==null || script.length()==0){
            return;
        }
        if (X5WebUtils.isMainThread()){
            BaseWebView.super.evaluateJavascript(script,callback);
        } else {
            Handler handler = getHandler();
            if (handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.evaluateJavascript(script,callback);
                    }
                });
            }
        }
    }

    /**
     * 这个方法可以在任何线程中调用，如果在主线程中没有调用它，它将自动分配给主线程。通过handler实现不同线程
     * @param url                                   url
     */
    @Override
    public void loadUrl(String url) {
        if (url==null || url.length()==0){
            return;
        }
        Message msg = mainThreadHandler.obtainMessage(LOAD_URL, url);
        mainThreadHandler.sendMessage(msg);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (url==null || url.length()==0){
            return;
        }
        Message msg = mainThreadHandler.obtainMessage(LOAD_URL_WITH_HEADERS, new RequestInfo(url, additionalHttpHeaders));
        mainThreadHandler.sendMessage(msg);
    }

    /**
     * 开发者调用
     * 子线程发送消息
     * @param url						url
     */
    public void postUrl(String url){
        if (url==null || url.length()==0){
            return;
        }
        if (X5WebUtils.isMainThread()){
            loadUrl(url);
        } else {
            Message message = mainThreadHandler.obtainMessage();
            message.what = LOAD_URL;
            message.obj = url;
            mainThreadHandler.sendMessage(message);
        }
    }


}
