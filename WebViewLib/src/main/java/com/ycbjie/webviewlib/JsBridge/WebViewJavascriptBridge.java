package com.ycbjie.webviewlib.JsBridge;


public interface WebViewJavascriptBridge {
	
	void send(String data);
	void send(String data, CallBackFunction responseCallback);

}
