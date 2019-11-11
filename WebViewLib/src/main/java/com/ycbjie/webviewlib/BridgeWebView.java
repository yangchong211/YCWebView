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
package com.ycbjie.webviewlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
public class BridgeWebView extends WebView implements WebViewJavascriptBridge{

	public static final String TO_LOAD_JS = "WebViewJavascriptBridge.js";
	private long uniqueId = 0;
	private Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
	private Map<String, BridgeHandler> messageHandlers = new HashMap<>();
	BridgeHandler defaultHandler = new DefaultHandler();
	private List<Message> startupMessage = new ArrayList<>();
	private X5WebViewClient x5WebViewClient;
	private X5WebChromeClient x5WebChromeClient;
	/**
	 * loadUrl方法在19以上超过2097152个字符失效
	 */
	private static final int URL_MAX_CHARACTER_NUM=2097152;


	/**
	 * 获取消息list集合
	 * @return							集合
	 */
	public List<Message> getStartupMessage() {
		return startupMessage;
	}

	/**
	 * 设置消息，注意目前在onProgressChanged方法中调用
	 */
	public void setStartupMessage(List<Message> startupMessage) {
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
		x5WebViewClient = new MyX5WebViewClient(this,getContext());
		this.setWebViewClient(x5WebViewClient);
		x5WebChromeClient = new MyX5WebChromeClient(this,(Activity) getContext());
		this.setWebChromeClient(x5WebChromeClient);
	}

	private class MyX5WebChromeClient extends X5WebChromeClient{

//		private boolean isShowContent = false;

		/**
		 * 构造方法
		 *
		 * @param activity 上下文
		 */
		public MyX5WebChromeClient(BridgeWebView webView, Activity activity) {
			super(webView, activity);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
//			int max = 85;
//			if (newProgress> max && !isShowContent){
//				//在这个时候添加js注入方法，具体可以看readme文档
//                BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
//                if (BridgeWebView.this.getStartupMessage() != null) {
//                    for (Message m : BridgeWebView.this.getStartupMessage()) {
//						BridgeWebView.this.dispatchMessage(m);
//                    }
//					BridgeWebView.this.setStartupMessage(null);
//                }
//				isShowContent = true;
//			}
		}
	}

	private class MyX5WebViewClient extends X5WebViewClient{

		/**
		 * 构造方法
		 *
		 * @param webView 需要传进来webview
		 * @param context 上下文
		 */
		public MyX5WebViewClient(BridgeWebView webView, Context context) {
			super(webView, context);
		}

		/**
		 * 这个方法中可以做拦截
		 * 主要的作用是处理各种通知和请求事件
		 * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
		 * @param view                              view
		 * @param url                               链接
		 * @return                                  是否自己处理，true表示自己处理
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			X5LogUtils.i("-------shouldOverrideUrlLoading----1---"+url);
			//页面关闭后，直接返回，不要执行网络请求和js方法
			boolean activityAlive = X5WebUtils.isActivityAlive(getContext());
			if (!activityAlive){
				return false;
			}
			if (TextUtils.isEmpty(url)) {
				return false;
			}
			try {
				url = URLDecoder.decode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// 如果是返回数据
			if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
				BridgeWebView.this.handlerReturnData(url);
				return true;
			} else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
				BridgeWebView.this.flushMessageQueue();
				return true;
			} else {
				if (this.onCustomShouldOverrideUrlLoading(url)){
					return true;
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		}

		protected boolean onCustomShouldOverrideUrlLoading(String url) {
			return false;
		}


		/**
		 * 增加shouldOverrideUrlLoading在api>=24时
		 * 主要的作用是处理各种通知和请求事件
		 * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
		 * @param view                              view
		 * @param request                           request
		 * @return
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			X5LogUtils.i("-------shouldOverrideUrlLoading----2---"+request.getUrl().toString());
			//页面关闭后，直接返回，不要执行网络请求和js方法
			boolean activityAlive = X5WebUtils.isActivityAlive(getContext());
			if (!activityAlive){
				return false;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				String url = request.getUrl().toString();
				if (TextUtils.isEmpty(url)) {
					return false;
				}
				try {
					url = URLDecoder.decode(url, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
				//如果是返回数据
				if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
					BridgeWebView.this.handlerReturnData(url);
					return true;
				} else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
					BridgeWebView.this.flushMessageQueue();
					return true;
				} else {
					if (this.onCustomShouldOverrideUrlLoading(url)){
						return true;
					} else {
						return super.shouldOverrideUrlLoading(view, request);
					}
				}
			}else {
				return super.shouldOverrideUrlLoading(view, request);
			}
		}


		/**
		 * 当页面加载完成会调用该方法
		 * @param view                              view
		 * @param url                               url链接
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			//这个时候添加js注入方法
			//WebViewJavascriptBridge.js
			BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
			if (BridgeWebView.this.getStartupMessage() != null) {
				for (Message m : BridgeWebView.this.getStartupMessage()) {
					//分发message 必须在主线程才分发成功
					BridgeWebView.this.dispatchMessage(m);
				}
				BridgeWebView.this.setStartupMessage(null);
			}
		}
	}

    protected X5WebViewClient generateBridgeWebViewClient() {
        return x5WebViewClient;
    }

	protected X5WebChromeClient generateBridgeWebChromeClient() {
		return x5WebChromeClient;
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
		Message m = new Message();
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
	private void queueMessage(Message m) {
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
	public void dispatchMessage(Message m) {
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
			String javascriptCommand = String.format(
					BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
			// 必须要找主线程才会将数据传递出去 --- 划重点
			if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				X5LogUtils.d("分发message--------------"+javascriptCommand);
				//this.loadUrl(javascriptCommand);
				//开始执行js中_handleMessageFromNative方法
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
						javascriptCommand.length()>=URL_MAX_CHARACTER_NUM) {
					this.evaluateJavascript(javascriptCommand,null);
				}else {
					this.loadUrl(javascriptCommand);
				}
			}
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
					List<Message> list = null;
					try {
						list = Message.toArrayList(data);
					} catch (Exception e) {
                        e.printStackTrace();
						return;
					}
					if (list == null || list.size() == 0) {
						return;
					}
					for (int i = 0; i < list.size(); i++) {
						Message m = list.get(i);
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
										Message responseMsg = new Message();
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
	 */
	public void callHandler(String handlerName) {
		send(handlerName);
	}

	/**
	 * call javascript registered handler
	 * 调用javascript处理程序注册
	 * @param handlerName handlerName
	 * @param callBack CallBackFunction
	 */
	public void callHandler(String handlerName,CallBackFunction callBack) {
		send(handlerName, callBack);
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
