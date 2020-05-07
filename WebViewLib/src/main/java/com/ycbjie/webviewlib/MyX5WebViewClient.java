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

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 特别重要：主要把js的处理给分离到该类中
 *     revise: 如果要自定义WebViewClient必须要集成此类
 *             demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class MyX5WebViewClient extends X5WebViewClient{

    private Context context;
    private X5WebView mWebView;
    private HttpDnsService httpDns ;
    /**
     * 构造方法
     *
     * @param webView 需要传进来webview
     * @param context 上下文
     */
    public MyX5WebViewClient(X5WebView webView, Context context) {
        super(webView, context);
        this.mWebView = webView;
        this.context = context;
        if (X5WebUtils.isHttpDns && httpDns==null){
            httpDns = webView.getHttpDns();
        }
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
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
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
            mWebView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
            mWebView.flushMessageQueue();
            return true;
        } else {
            if (this.onCustomShouldOverrideUrlLoading(url)){
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    /**
     * 子类可以自己去实现
     * @param url                               url链接
     * @return
     */
    public boolean onCustomShouldOverrideUrlLoading(String url) {
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
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
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
                mWebView.handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                mWebView.flushMessageQueue();
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
        if (mWebView.getStartupMessage() != null) {
            for (Message m : mWebView.getStartupMessage()) {
                //分发message 必须在主线程才分发成功
                mWebView.dispatchMessage(m);
            }
            mWebView.setStartupMessage(null);
        }
    }

    /**
     * 此方法添加于API21，调用于非UI线程，拦截资源请求并返回数据，返回null时WebView将继续加载资源
     *
     * 其中 WebResourceRequest 封装了请求，WebResourceResponse 封装了响应
     * 封装了一个Web资源的响应信息，包含：响应数据流，编码，MIME类型，API21后添加了响应头，状态码与状态描述
     * @param webView                           view
     * @param webResourceRequest                request，添加于API21，封装了一个Web资源的请求信息，
     *                                          包含：请求地址，请求方法，请求头，是否主框架，是否用户点击，是否重定向
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        if (X5WebUtils.isHttpDns){
            String scheme = Objects.requireNonNull(webResourceRequest.getUrl().getScheme()).trim();
            String method = webResourceRequest.getMethod();
            Map<String, String> headerFields = webResourceRequest.getRequestHeaders();
            String url = webResourceRequest.getUrl().toString();
            // 无法拦截body，拦截方案只能正常处理不带body的请求；
            if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && method.equalsIgnoreCase("get")) {
                try {
                    URLConnection connection = recursiveRequest(url, headerFields, null);
                    if (connection == null) {
                        return super.shouldInterceptRequest(webView, webResourceRequest);
                    }
                    // 注*：对于POST请求的Body数据，WebResourceRequest接口中并没有提供，这里无法处理
                    String contentType = connection.getContentType();
                    String mime = getMime(contentType);
                    String charset = getCharset(contentType);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
                    int statusCode = httpURLConnection.getResponseCode();
                    String response = httpURLConnection.getResponseMessage();
                    Map<String, List<String>> headers = httpURLConnection.getHeaderFields();
                    Set<String> headerKeySet = headers.keySet();
                    X5LogUtils.d("code:" + httpURLConnection.getResponseCode());
                    X5LogUtils.d("mime:" + mime + "; charset:" + charset);

                    // 无mime类型的请求不拦截
                    if (TextUtils.isEmpty(mime)) {
                        return super.shouldInterceptRequest(webView, webResourceRequest);
                    } else {
                        // 二进制资源无需编码信息
                        if (!TextUtils.isEmpty(charset) || (isBinaryRes(mime))) {
                            WebResourceResponse resourceResponse = new WebResourceResponse(mime, charset, httpURLConnection.getInputStream());
                            resourceResponse.setStatusCodeAndReasonPhrase(statusCode, response);
                            Map<String, String> responseHeader = new HashMap<String, String>();
                            for (String key: headerKeySet) {
                                // HttpUrlConnection可能包含key为null的报头，指向该http请求状态码
                                responseHeader.put(key, httpURLConnection.getHeaderField(key));
                            }
                            resourceResponse.setResponseHeaders(responseHeader);
                            return resourceResponse;
                        } else {
                            X5LogUtils.d("non binary resource for " + mime);
                            return super.shouldInterceptRequest(webView, webResourceRequest);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.shouldInterceptRequest(webView, webResourceRequest);
    }


    /**
     * 从contentType中获取MIME类型
     * @param contentType
     * @return
     */
    private String getMime(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.split(";")[0];
    }

    /**
     * 从contentType中获取编码信息
     * @param contentType
     * @return
     */
    private String getCharset(String contentType) {
        if (contentType == null) {
            return null;
        }

        String[] fields = contentType.split(";");
        if (fields.length <= 1) {
            return null;
        }

        String charset = fields[1];
        if (!charset.contains("=")) {
            return null;
        }
        charset = charset.substring(charset.indexOf("=") + 1);
        return charset;
    }


    public URLConnection recursiveRequest(String path, Map<String, String> headers, String reffer) {
        HttpURLConnection conn;
        URL url = null;
        try {
            url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            // 异步接口获取IP
            String ip = httpDns.getIpByHostAsync(url.getHost());
            if (ip != null) {
                // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
                X5LogUtils.d("Get IP: " + ip + " for host: " + url.getHost() + " from HTTPDNS successfully!");
                String newUrl = path.replaceFirst(url.getHost(), ip);
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                if (headers != null) {
                    for (Map.Entry<String, String> field : headers.entrySet()) {
                        conn.setRequestProperty(field.getKey(), field.getValue());
                    }
                }
                // 设置HTTP请求头Host域
                conn.setRequestProperty("Host", url.getHost());
            } else {
                return null;
            }
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);
            if (conn instanceof HttpsURLConnection) {
                final HttpsURLConnection httpsURLConnection = (HttpsURLConnection)conn;
                WebTlsSniSocketFactory sslSocketFactory = new WebTlsSniSocketFactory((HttpsURLConnection) conn);
                // sni场景，创建SSLScocket
                httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
                // https场景，证书校验
                httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        String host = httpsURLConnection.getRequestProperty("Host");
                        if (null == host) {
                            host = httpsURLConnection.getURL().getHost();
                        }
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(host, session);
                    }
                });
            }
            int code = conn.getResponseCode();// Network block
            if (needRedirect(code)) {
                // 原有报头中含有cookie，放弃拦截
                if (headers != null && containCookie(headers)) {
                    return null;
                }
                String location = conn.getHeaderField("Location");
                if (location == null) {
                    location = conn.getHeaderField("location");
                }
                if (location != null) {
                    if (!(location.startsWith("http://") || location.startsWith("https://"))) {
                        //某些时候会省略host，只返回后面的path，所以需要补全url
                        URL originalUrl = new URL(path);
                        location = originalUrl.getProtocol() + "://" + originalUrl.getHost() + location;
                    }
                    X5LogUtils.d("code:" + code + "; location:" + location + "; path" + path);
                    return recursiveRequest(location, headers, path);
                } else {
                    // 无法获取location信息，让浏览器获取
                    return null;
                }
            } else {
                // redirect finish.
                X5LogUtils.d("redirect finish");
                return conn;
            }
        } catch (MalformedURLException e) {
            X5LogUtils.d("recursiveRequest MalformedURLException");
        } catch (IOException e) {
            X5LogUtils.d("recursiveRequest IOException");
        } catch (Exception e) {
            X5LogUtils.d("unknow exception");
        }
        return null;
    }

    /**
     * 是否是二进制资源，二进制资源可以不需要编码信息
     * @param mime
     * @return
     */
    private boolean isBinaryRes(String mime) {
        if (mime.startsWith("image") || mime.startsWith("audio") || mime.startsWith("video")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * header中是否含有cookie
     * @param headers
     */
    private boolean containCookie(Map<String, String> headers) {
        for (Map.Entry<String, String> headerField : headers.entrySet()) {
            if (headerField.getKey().contains("Cookie")) {
                return true;
            }
        }
        return false;
    }

    private boolean needRedirect(int code) {
        return code >= 300 && code < 400;
    }

}