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
package com.ycbjie.webviewlib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.ycbjie.webviewlib.base.RequestInfo;
import com.ycbjie.webviewlib.inter.CallBackFunction;
import com.ycbjie.webviewlib.bridge.DefaultHandler;
import com.ycbjie.webviewlib.bridge.WebJsMessage;
import com.ycbjie.webviewlib.inter.WebViewJavascriptBridge;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.inter.BridgeHandler;
import com.ycbjie.webviewlib.bridge.BridgeUtil;
import com.ycbjie.webviewlib.wv.WvWebView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义WebView类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
@SuppressLint("SetJavaScriptEnabled")
public class  BridgeWebView extends WebView implements WebViewJavascriptBridge {

	public static final String TO_LOAD_JS = "WebViewJavascriptBridge.js";
	private long uniqueId = 0;
	private Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
	private Map<String, BridgeHandler> messageHandlers = new HashMap<>();
	BridgeHandler defaultHandler = new DefaultHandler();
	private List<WebJsMessage> startupMessage = new ArrayList<>();
	/**
	 * loadUrl方法在19以上超过2097152个字符失效
	 */
	private static final int URL_MAX_CHARACTER_NUM = 2097152;
	private static final int EXEC_SCRIPT = 1;
	private static final int LOAD_URL = 2;
	private static final int LOAD_URL_WITH_HEADERS = 3;
	private static final int HANDLE_MESSAGE = 4;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case EXEC_SCRIPT:
					String javascriptCommand = (String) msg.obj;
					evaluateJavascriptUrl(javascriptCommand);
					break;
				case LOAD_URL:
					String url = (String) msg.obj;
					loadUrl(url);
					break;
				case LOAD_URL_WITH_HEADERS:
					RequestInfo info = (RequestInfo) msg.obj;
					BridgeWebView.super.loadUrl(info.url, info.headers);
					break;
				default:
					break;
			}
		}
	};
	
	/**
	 * 获取消息list集合
	 * @return							集合
	 */
	public List<WebJsMessage> getStartupMessage() {
		return startupMessage;
	}

	/**
	 * 设置消息，注意目前在onProgressChanged方法中调用
	 */
	public void setStartupMessage(List<WebJsMessage> startupMessage) {
		this.startupMessage = startupMessage;
	}

	public BridgeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BridgeWebView(Context context) {
		super(context);
		init();
	}

	/**
	 * 开发者调用
	 * 主线程发送消息
	 * @param url						url
	 */
	@Override
	public void loadUrl(String url) {
		try {
			super.loadUrl(url);
			//Message msg = handler.obtainMessage(LOAD_URL, url);
			//handler.sendMessage(msg);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 开发者调用
	 * 主线程发送消息
	 * @param url						url
	 */
	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		Message msg = handler.obtainMessage(LOAD_URL_WITH_HEADERS, new RequestInfo(url, additionalHttpHeaders));
		handler.sendMessage(msg);
	}

	/**
	 * 开发者调用
	 * 子线程发送消息
	 * @param url						url
	 */
	public void postUrl(String url){
		if (Thread.currentThread() == Looper.getMainLooper().getThread()){
			loadUrl(url);
		} else {
			Message message = handler.obtainMessage();
			message.what = LOAD_URL;
			message.obj = url;
			handler.sendMessage(message);
		}
	}

	/**
	 * 开发者调用
	 * Android调用js方法
	 * @param script					方法
	 */
	public void evaluateJavascript(final String script) {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			evaluateJavascriptUrl(script);
		} else {
			Message msg = handler.obtainMessage(EXEC_SCRIPT, script);
			handler.sendMessage(msg);
		}
	}

	/**
	 * Android调用js方法
	 * @param script					方法
	 */
	private void evaluateJavascriptUrl(String script) {
		X5LogUtils.d("分发message--------------"+script);
		//this.loadUrl(javascriptCommand);
		//开始执行js中_handleMessageFromNative方法
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			BridgeWebView.super.evaluateJavascript(script, new ValueCallback<String>(){
				@Override
				public void onReceiveValue(String s) {
					X5LogUtils.i("---evaluateJavascript-1--"+s);
				}
			});
		} else {
			if (script.length()>=URL_MAX_CHARACTER_NUM){
				BridgeWebView.super.evaluateJavascript(script, new ValueCallback<String>(){
					@Override
					public void onReceiveValue(String s) {
						X5LogUtils.i("---evaluateJavascript-2--"+s);
					}
				});
			} else {
				loadUrl(script);
			}
		}
	}

	/**
	 * 默认处理程序，处理js发送的没有指定处理程序名称的消息，
	 * 如果js消息有处理程序名，它将由本机注册的命名处理程序处理
	 * @param handler							handler
	 */
	public void setDefaultHandler(BridgeHandler handler) {
       this.defaultHandler = handler;
	}

    private void init() {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
	}

    /**
     * 获取到CallBackFunction data执行调用并且从数据集移除
     * @param url						url链接
     */
	public void handlerReturnData(String url) {
		String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
		if (functionName!=null){
			CallBackFunction f = responseCallbacks.get(functionName);
			String data = BridgeUtil.getDataFromReturnUrl(url);
			if (f != null) {
				f.onCallBack(data);
				responseCallbacks.remove(functionName);
			}
		}
	}

	@Override
	public void send(String data) {
		send(data, null);
	}

	@Override
	public void send(String data, CallBackFunction responseCallback) {
		doSend(null, data, responseCallback);
	}

    /**
     * 保存message到消息队列
     * @param handlerName handlerName
     * @param data data
     * @param responseCallback CallBackFunction
     */
	private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
		//创建message对象，主要是将js的方法名称，传递的数据，封装到对象中
		WebJsMessage m = new WebJsMessage();
		//判断是否有data数据
		if (!TextUtils.isEmpty(data)) {
			m.setData(data);
		}
		//判断responseCallback是否为null
		if (responseCallback != null) {
			String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT,
					++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
			responseCallbacks.put(callbackStr, responseCallback);
			//m.setCallbackId(callbackStr)方法的作用？
			//该方法设置的callbackId生成后不仅仅会被传到Js，
			//而且会以key-value对的形式和responseCallback配对保存到responseCallbacks这个Map里面。
			//它的目的，就是为了等Js把处理结果回调给Java层后，
			//Java层能根据callbackId找到对应的responseCallback，做后续的回调处理。
			m.setCallbackId(callbackStr);
		}
		//判断是否有handlerName数据
		if (!TextUtils.isEmpty(handlerName)) {
			m.setHandlerName(handlerName);
		}
		//开始分发数据
		queueMessage(m);
	}

    /**
     * list<message> != null 添加到消息集合否则分发消息
     * @param m Message
     */
	private void queueMessage(WebJsMessage m) {
		if (startupMessage != null) {
			startupMessage.add(m);
		} else {
			dispatchMessage(m);
		}
	}

    /**
     * 分发message 必须在主线程才分发成功
     * @param m Message
     */
	public void dispatchMessage(WebJsMessage m) {
        String messageJson = m.toJson();
        //增加非空判断的逻辑
        if (messageJson!=null){
			//escape special characters for json string  为json字符串转义特殊字符
			messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
			messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
			messageJson = messageJson.replaceAll("(?<=[^\\\\])(\')", "\\\\\'");
			messageJson = messageJson.replaceAll("%7B", URLEncoder.encode("%7B"));
			messageJson = messageJson.replaceAll("%7D", URLEncoder.encode("%7D"));
			messageJson = messageJson.replaceAll("%22", URLEncoder.encode("%22"));
			//转化格式为javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');
			String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
			Message message = handler.obtainMessage();
			message.what = EXEC_SCRIPT;
			message.obj = javascriptCommand;
			handler.sendMessage(message);
			// 必须要找主线程才会将数据传递出去 --- 划重点
			/*if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				X5LogUtils.d("分发message--------------"+javascriptCommand);
				//this.loadUrl(javascriptCommand);
				//开始执行js中_handleMessageFromNative方法
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
						javascriptCommand.length()>=URL_MAX_CHARACTER_NUM) {
					this.evaluateJavascript(javascriptCommand,null);
				}else {
					this.loadUrl(javascriptCommand);
				}
			}*/
		}
    }

    /**
     * 刷新消息队列
     */
	public void flushMessageQueue() {
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {
				@Override
				public void onCallBack(String data) {
					// deserializeMessage 反序列化消息
					List<WebJsMessage> list = null;
					try {
						list = WebJsMessage.toArrayList(data);
					} catch (Exception e) {
                        e.printStackTrace();
						return;
					}
					if (list == null || list.size() == 0) {
						return;
					}
					for (int i = 0; i < list.size(); i++) {
						WebJsMessage m = list.get(i);
						String responseId = m.getResponseId();
						// 是否是response  CallBackFunction
						if (!TextUtils.isEmpty(responseId)) {
							CallBackFunction function = responseCallbacks.get(responseId);
							String responseData = m.getResponseData();
							function.onCallBack(responseData);
							responseCallbacks.remove(responseId);
						} else {
							CallBackFunction responseFunction = null;
							// if had callbackId 如果有回调Id
							final String callbackId = m.getCallbackId();
							if (!TextUtils.isEmpty(callbackId)) {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										WebJsMessage responseMsg = new WebJsMessage();
										responseMsg.setResponseId(callbackId);
										responseMsg.setResponseData(data);
										queueMessage(responseMsg);
									}
								};
							} else {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										// do nothing
									}
								};
							}
							// BridgeHandler执行
							BridgeHandler handler;
							if (!TextUtils.isEmpty(m.getHandlerName())) {
								handler = messageHandlers.get(m.getHandlerName());
							} else {
								handler = defaultHandler;
							}
							if (handler != null){
								handler.handler(m.getData(), responseFunction);
							}
						}
					}
				}
			});
		}
	}


	public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
		this.loadUrl(jsUrl);
        // 添加至 Map<String, CallBackFunction>
		responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
	}

	/**
	 * register handler,so that javascript can call it
	 * 注册处理程序,以便javascript调用它
	 * @param handlerName 					handlerName
	 * @param handler 						BridgeHandler
	 */
	public void registerHandler(String handlerName, BridgeHandler handler) {
		if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
			messageHandlers.put(handlerName, handler);
		}
	}

	/**
	 * 解绑注册js操作，一般可以在onDestory中处理
	 * @param handlerName					方法name
	 */
	public void unregisterHandler(String handlerName) {
		if (handlerName != null) {
			messageHandlers.remove(handlerName);
		}
	}

	/**
	 * call javascript registered handler
	 * 调用javascript处理程序注册
     * @param handlerName handlerName
	 * @param data data
	 * @param callBack CallBackFunction
	 */
	public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
	}

}
