package com.ycbjie.webviewlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义WebView类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 *             该demo可以作为学习案例，实现js交互的思路和BridgeWebView不一样，对比学习
 *             该案例参考：https://github.com/wendux/WebViewJavascriptBridge
 * </pre>
 */
public class WvWebView extends WebView {

    private static final String BRIDGE_NAME = "WVJBInterface";
    private static final int EXEC_SCRIPT = 1;
    private static final int LOAD_URL = 2;
    private static final int LOAD_URL_WITH_HEADERS = 3;
    private static final int HANDLE_MESSAGE = 4;
    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";
    private MyHandler mainThreadHandler = null;
    private JavascriptCloseWindowListener javascriptCloseWindowListener=null;
    private ArrayList<WvMessage> startupMessageQueue = null;
    private Map<String, WVJBResponseCallback> responseCallbacks = null;
    private Map<String, WVJBHandler> messageHandlers = null;
    private long uniqueId = 0;
    private boolean alertBoxBlock=true;
    
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
                        WvWebView.super.loadUrl((String) msg.obj);
                        break;
                    case LOAD_URL_WITH_HEADERS:
                        RequestInfo info = (RequestInfo) msg.obj;
                        WvWebView.super.loadUrl(info.url, info.headers);
                        break;
                    case HANDLE_MESSAGE:
                        WvWebView.this.handleMessage((String) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class RequestInfo {
        String url;
        Map<String, String> headers;
        RequestInfo(String url, Map<String, String> additionalHttpHeaders) {
            this.url = url;
            this.headers = additionalHttpHeaders;
        }
    }

    private class WvMessage {
        Object data = null;
        String callbackId = null;
        String handlerName = null;
        String responseId = null;
        Object responseData = null;
    }

    public WvWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WvWebView(Context context) {
        super(context);
        init();
    }

    public interface WVJBResponseCallback<T> {
        void onResult(T data);
    }

    public interface WVJBMethodExistCallback {
        void onResult(boolean exist);
    }

    public interface JavascriptCloseWindowListener {
        /**
         * @return  If true, close the current activity, otherwise, do nothing.
         */
        boolean onClose();
    }

    public interface WVJBHandler<T,R> {
        void handler(T data, WVJBResponseCallback<R> callback);
    }

    public void disableJavascriptAlertBoxSafetyTimeout(boolean disable){
        alertBoxBlock = !disable;
    }

    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    public  void callHandler(String handlerName, Object data) {
        callHandler(handlerName, data, null);
    }

    public  <T> void callHandler(String handlerName, Object data, WVJBResponseCallback<T> responseCallback) {
        sendData(data, responseCallback, handlerName);
    }

    /**
     * Test whether the handler exist in javascript
     * @param handlerName
     * @param callback
     */
    public void hasJavascriptMethod(String handlerName, final WVJBMethodExistCallback callback){
        callHandler("_hasJavascriptMethod", handlerName, new WVJBResponseCallback() {
            @Override
            public void onResult(Object data) {
                callback.onResult((boolean)data);
            }
        });
    }

    /**
     * set a listener for javascript closing the current activity.
     */
    public void setJavascriptCloseWindowListener(JavascriptCloseWindowListener listener){
        javascriptCloseWindowListener=listener;
    }

    /**
     * js调native
     * @param handlerName                               名称
     * @param handler                                   消息
     * @param <T>                                       T
     * @param <R>                                       R
     */
    public <T,R> void registerHandler(String handlerName, WVJBHandler<T,R> handler) {
        if (handlerName == null || handlerName.length() == 0 || handler == null) {
            return;
        }
        messageHandlers.put(handlerName, handler);
    }

    /**
     * send the onResult message to javascript
     * 发送消息给js
     * @param data                                      data
     * @param responseCallback                          callback
     * @param handlerName                               handlerName
     */
    private void sendData(Object data, WVJBResponseCallback responseCallback, String handlerName) {
        if (data == null && (handlerName == null || handlerName.length() == 0)) {
            return;
        }
        WvMessage message = new WvMessage();
        if (data != null) {
            message.data = data;
        }
        if (responseCallback != null) {
            String callbackId = "java_cb_" + (++uniqueId);
            responseCallbacks.put(callbackId, responseCallback);
            message.callbackId = callbackId;
        }
        if (handlerName != null) {
            message.handlerName = handlerName;
        }
        queueMessage(message);
    }

    private synchronized void queueMessage(WvMessage message) {
        if (startupMessageQueue != null) {
            startupMessageQueue.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    private void dispatchMessage(WvMessage message) {
        String messageJson = messageToJsonObject(message).toString();
        evaluateJavascript(String.format("WebViewJavascriptBridge._handleMessageFromJava(%s)", messageJson));
    }

    // handle the onResult message from javascript
    private void handleMessage(String info) {
        try {
            JSONObject jo = new JSONObject(info);
            WvMessage message = JsonObjectToMessage(jo);
            if (message.responseId != null) {
                WVJBResponseCallback responseCallback = responseCallbacks.remove(message.responseId);
                if (responseCallback != null) {
                    responseCallback.onResult(message.responseData);
                }
            } else {
                WVJBResponseCallback responseCallback = null;
                if (message.callbackId != null) {
                    final String callbackId = message.callbackId;
                    responseCallback = new WVJBResponseCallback() {
                        @Override
                        public void onResult(Object data) {
                            WvMessage msg = new WvMessage();
                            msg.responseId = callbackId;
                            msg.responseData = data;
                            dispatchMessage(msg);
                        }
                    };
                }

                WVJBHandler handler;
                handler = messageHandlers.get(message.handlerName);
                if (handler != null) {
                    handler.handler(message.data, responseCallback);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject messageToJsonObject(WvMessage message) {
        JSONObject jo = new JSONObject();
        try {
            if (message.callbackId != null) {
                jo.put(CALLBACK_ID_STR, message.callbackId);
            }
            if (message.data != null) {
                jo.put(DATA_STR, message.data);
            }
            if (message.handlerName != null) {
                jo.put(HANDLER_NAME_STR, message.handlerName);
            }
            if (message.responseId != null) {
                jo.put(RESPONSE_ID_STR, message.responseId);
            }
            if (message.responseData != null) {
                jo.put(RESPONSE_DATA_STR, message.responseData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    private WvMessage JsonObjectToMessage(JSONObject jo) {
        WvMessage message = new WvMessage();
        try {
            if (jo.has(CALLBACK_ID_STR)) {
                message.callbackId = jo.getString(CALLBACK_ID_STR);
            }
            if (jo.has(DATA_STR)) {
                message.data = jo.get(DATA_STR);
            }
            if (jo.has(HANDLER_NAME_STR)) {
                message.handlerName = jo.getString(HANDLER_NAME_STR);
            }
            if (jo.has(RESPONSE_ID_STR)) {
                message.responseId = jo.getString(RESPONSE_ID_STR);
            }
            if (jo.has(RESPONSE_DATA_STR)) {
                message.responseData = jo.get(RESPONSE_DATA_STR);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    @Keep
    void init() {
        mainThreadHandler = new MyHandler(getContext());
        this.responseCallbacks = new HashMap<>();
        this.messageHandlers = new HashMap<>();
        this.startupMessageQueue = new ArrayList<>();
        super.setWebChromeClient(mWebChromeClient);
        super.setWebViewClient(mWebViewClient);
        registerHandler("_hasNativeMethod", new WVJBHandler() {
            @Override
            public void handler(Object data, WVJBResponseCallback callback) {
                callback.onResult(messageHandlers.get(data) != null);
            }
        });
        registerHandler("_closePage", new WVJBHandler() {
            @Override
            public void handler(Object data, WVJBResponseCallback callback) {
                if(javascriptCloseWindowListener==null ||javascriptCloseWindowListener.onClose()){
                    ((Activity) getContext()).onBackPressed();
                }
            }
        });
        registerHandler("_disableJavascriptAlertBoxSafetyTimeout", new WVJBHandler() {
            @Override
            public void handler(Object data, WVJBResponseCallback callback) {
                disableJavascriptAlertBoxSafetyTimeout((boolean)data);
            }
        });
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.JELLY_BEAN){
            super.addJavascriptInterface(new Object() {
                @Keep
                @JavascriptInterface
                public void notice(String info) {
                    Message msg = mainThreadHandler.obtainMessage(HANDLE_MESSAGE, info);
                    mainThreadHandler.sendMessage(msg);
                }
            }, BRIDGE_NAME);
        }

    }

    private void evaluateJavascriptUrl(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WvWebView.super.evaluateJavascript(script, null);
        } else {
            super.loadUrl("javascript:" + script);
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     * @param script
     */
    public void evaluateJavascript(final String script) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            evaluateJavascriptUrl(script);
        } else {
            Message msg = mainThreadHandler.obtainMessage(EXEC_SCRIPT, script);
            mainThreadHandler.sendMessage(msg);
        }
    }


    /**
     * 这个方法可以在任何线程中调用，如果在主线程中没有调用它，它将自动分配给主线程。通过handler实现不同线程
     * @param url                                   url
     */
    @Override
    public void loadUrl(String url) {
        Message msg = mainThreadHandler.obtainMessage(LOAD_URL, url);
        mainThreadHandler.sendMessage(msg);
    }


    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     * @param url
     * @param additionalHttpHeaders
     */
    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        Message msg = mainThreadHandler.obtainMessage(LOAD_URL_WITH_HEADERS, new RequestInfo(url, additionalHttpHeaders));
        mainThreadHandler.sendMessage(msg);
    }

    protected X5WebViewClient generateBridgeWebViewClient() {
        return mWebViewClient;
    }

    protected X5WebChromeClient generateBridgeWebChromeClient() {
        return mWebChromeClient;
    }


    private X5WebChromeClient mWebChromeClient = new X5WebChromeClient(this,(Activity) getContext()) {

        private boolean isShowContent = false;
        private int max = 85;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if(newProgress>max && !isShowContent) {
                try {
                    InputStream is = view.getContext().getAssets().open("WvWebViewJavascriptBridge.js");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    String js = new String(buffer);
                    evaluateJavascript(js);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (WvWebView.this) {
                    if (startupMessageQueue != null) {
                        for (int i = 0; i < startupMessageQueue.size(); i++) {
                            dispatchMessage(startupMessageQueue.get(i));
                        }
                        startupMessageQueue = null;
                    }
                }
                isShowContent = true;
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
            if(!alertBoxBlock){
                result.confirm();
            }
            Dialog alertDialog = new AlertDialog.Builder(getContext()).
                    setMessage(message).
                    setCancelable(false).
                    setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(alertBoxBlock) {
                                result.confirm();
                            }
                        }
                    })
                    .create();
            alertDialog.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            if(!alertBoxBlock){
                result.confirm();
            }
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(alertBoxBlock) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            result.confirm();
                        } else {
                            result.cancel();
                        }
                    }
                }
            };
            new AlertDialog.Builder(getContext())
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener).show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, final String message,
                                  String defaultValue, final JsPromptResult result) {
            if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.JELLY_BEAN){
                String prefix="_wvjbxx";
                if(message.equals(prefix)){
                    Message msg = mainThreadHandler.obtainMessage(HANDLE_MESSAGE, defaultValue);
                    mainThreadHandler.sendMessage(msg);
                }
                return true;
            }
            if(!alertBoxBlock){
                result.confirm();
            }
            final EditText editText = new EditText(getContext());
            editText.setText(defaultValue);
            if (defaultValue != null) {
                editText.setSelection(defaultValue.length());
            }
            float dpi = getContext().getResources().getDisplayMetrics().density;
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(alertBoxBlock) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            result.confirm(editText.getText().toString());
                        } else {
                            result.cancel();
                        }
                    }
                }
            };
            new AlertDialog.Builder(getContext())
                    .setTitle(message)
                    .setView(editText)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener)
                    .show();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int t = (int) (dpi * 16);
            layoutParams.setMargins(t, 0, t, 0);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            editText.setLayoutParams(layoutParams);
            int padding = (int) (15 * dpi);
            editText.setPadding(padding - (int) (5 * dpi), padding, padding, padding);
            return true;
        }
    };

    private X5WebViewClient mWebViewClient = new X5WebViewClient(this,getContext()) {

    };

}
