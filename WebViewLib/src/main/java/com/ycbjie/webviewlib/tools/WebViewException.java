package com.ycbjie.webviewlib.tools;

/**
 * <pre>
 *     @author  yangchong
 *     email  : yangchong211@163.com
 *     blog   : https://github.com/yangchong211
 *     time   : 2020/04/27
 *     desc   : 自定义异常
 *     revise:
 * </pre>
 */
public class WebViewException extends Exception {

    private int mCode = 0;

    /**
     * code 1  X5WebUtils不能直接初始化异常
     * code 2  初始化腾讯x5浏览器webView，不是在application初始化异常
     * code 3  跳转微信支付，支付宝支付，系统未安装相应应用异常
     */

    public WebViewException(int code, String msg) {
        super(msg);
        mCode = code;
    }

    public WebViewException(String msg) {
        super(msg);
    }

    public WebViewException(Throwable throwable) {
        super(throwable);
        if (throwable instanceof WebViewException) {
            mCode = ((WebViewException) throwable).getCode();
        }
    }

    public int getCode() {
        return mCode;
    }

    public String getMsg() {
        return getMessage();
    }
}
