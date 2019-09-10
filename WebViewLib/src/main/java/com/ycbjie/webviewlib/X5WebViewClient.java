package com.ycbjie.webviewlib;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义x5的WebViewClient
 *     revise:
 * </pre>
 */
public class X5WebViewClient extends WebViewClient {

    private InterWebListener webListener;
    private Activity activity;
    private WebView webView;

    public X5WebViewClient(InterWebListener listener , Activity activity , WebView webView) {
        this.webListener = listener;
        this.activity = activity;
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        /*
         * 防止加载网页时调起系统浏览器
         */
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!X5WebUtils.isConnected(activity) && webListener!=null) {
            //隐藏进度条方法
            webListener.hindProgressBar();
            // html加载完成之后，添加js函数
            webListener.addJsListener();
        }
        super.onPageFinished(view, url);
        //在html标签加载完成之后在加载图片内容
        webView.getSettings().setBlockNetworkImage(false);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            String data = "Page NO FOUND！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }

    // 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
    // 值得注意的是，不同的是过时的版本的回调，新的版本将被称为任何资源（iframe，图像等）
    // 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
    // 6.0 之后
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("服务器异常",error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        if (webListener!=null){
            webListener.showErrorView();
        }
    }


    /**
     * 通知主机应用程序在加载资源时从服务器接收到HTTP错误
     * @param view                              view
     * @param request                           request
     * @param errorResponse                     错误内容
     */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }


    /**
     * 通知主机应用程序已自动处理用户登录请求
     * @param view                              view
     * @param realm                             数据
     * @param account                           account
     * @param args                              args
     */
    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    /**
     * 在加载资源时通知主机应用程序发生SSL错误
     * @param view                              view
     * @param handler                           handler
     * @param error                             error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
    }
}
