package com.ycbjie.webviewlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.Map;

public class BaseWebView extends WebView {

    private boolean mTouchByUser;

    public BaseWebView(Context context) {
        super(context);
        init();
    }

    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mTouchByUser = true;
                    X5LogUtils.i("------------OnTouchListener = true--");
                }
                return false;
            }
        });
    }

    @Override
    public final void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        resetAllStateInternal(url);
    }
    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        resetAllStateInternal(url);
    }
    @Override
    public final void postUrl(String url, byte[] postData) {
        super.postUrl(url, postData);
        resetAllStateInternal(url);
    }
    @Override
    public final void loadData(String data, String mimeType, String encoding) {
        super.loadData(data, mimeType, encoding);
        resetAllStateInternal(getUrl());
    }
    @Override
    public final void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding,
                                          String historyUrl) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
        resetAllStateInternal(getUrl());
    }
    @Override
    public void reload() {
        super.reload();
        resetAllStateInternal(getUrl());
    }
    public boolean isTouchByUser() {
        return mTouchByUser;
    }
    private void resetAllStateInternal(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("javascript:")) {
            return;
        }
        resetAllState();
    }
    // 加载url时重置touch状态
    protected void resetAllState() {
        mTouchByUser = false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //用户按下到下一个链接加载之前，置为true
                mTouchByUser = true;
                X5LogUtils.i("------------mTouchByUser = true--");
                break;
        }
        return super.onTouchEvent(event);
    }


    /*@Override
    public void setWebViewClient(final WebViewClient client) {
        super.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean handleByChild = null != client && client.shouldOverrideUrlLoading(view, url);
                if (handleByChild) {
                    // 开放client接口给上层业务调用，如果返回true，表示业务已处理。
                    return true;
                } else if (!isTouchByUser()) {
                    // 如果业务没有处理，并且在加载过程中用户没有再次触摸屏幕，认为是301/302事件，直接交由系统处理。
                    return super.shouldOverrideUrlLoading(view, url);
                } else {
                    //否则，属于二次加载某个链接的情况，为了解决拼接参数丢失问题，重新调用loadUrl方法添加固有参数。
                    loadUrl(url);
                    return true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                boolean handleByChild = null != client && client.shouldOverrideUrlLoading(view, request);
                if (handleByChild) {
                    return true;
                } else if (!isTouchByUser()) {
                    return super.shouldOverrideUrlLoading(view, request);
                } else {
                    loadUrl(request.getUrl().toString());
                    return true;
                }
            }
        });
    }*/


    private long m_DownTime ;
    private class CheckForClickTouchLister implements View.OnTouchListener {
        private final static long MAX_TOUCH_DURATION = 100;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    m_DownTime = event.getEventTime();
                    break;
                case MotionEvent.ACTION_UP:
                    if(event.getEventTime() - m_DownTime <= MAX_TOUCH_DURATION) {
                        //处理点击事件
                    }
                    break;
                default:
                    break;
             }
             return false;
        }
    }



}
