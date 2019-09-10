package com.ycbjie.webviewlib;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义接口，处理消息回调逻辑
 *     revise:
 * </pre>
 */
public interface BridgeHandler {
	
	void handler(String data, CallBackFunction function);

}
