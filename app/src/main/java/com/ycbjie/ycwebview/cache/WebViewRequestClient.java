package com.ycbjie.ycwebview.cache;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface WebViewRequestClient {

    WebResourceResponse interceptRequest(WebResourceRequest request);
    WebResourceResponse interceptRequest(String url);
    File getCachePath();

    void clearCache();
    void enableForce(boolean force);
    InputStream getCacheFile(String url);
    void initAssetsData();
    void loadUrl(WebView webView, String url);
    void loadUrl(String url, String userAgent);
    void loadUrl(String url, Map<String, String> additionalHttpHeaders, String userAgent);
    void loadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders);

}
