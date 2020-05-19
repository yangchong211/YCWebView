package com.ycbjie.webviewlib.tls;

import android.text.TextUtils;

import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.view.X5WebView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
 *     time  : 2020/5/10
 *     desc  : 特别重要：将https+dns解析处理逻辑分离到该类WebTlsHelper中，保证类的单一性
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class WebTlsHelper {

    private HttpDnsService httpDns ;

    public WebTlsHelper(HttpDnsService httpDns) {
        this.httpDns = httpDns;
    }

    public WebResourceResponse shouldInterceptRequest(X5WebView webView, WebResourceRequest webResourceRequest) {
        String scheme = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            scheme = Objects.requireNonNull(webResourceRequest.getUrl().getScheme()).trim();
        }
        String method = webResourceRequest.getMethod();
        Map<String, String> headerFields = webResourceRequest.getRequestHeaders();
        String url = webResourceRequest.getUrl().toString();
        // 无法拦截body，拦截方案只能正常处理不带body的请求；
        if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                && method.equalsIgnoreCase("get")) {
            try {
                URLConnection connection = recursiveRequest(url, headerFields, null);
                if (connection == null){
                    return webView.getX5WebViewClient().shouldInterceptRequest(webView, webResourceRequest);
                }
                // 注*：对于POST请求的Body数据，WebResourceRequest接口中并没有提供，这里无法处理
                String contentType = connection.getContentType();
                String mime = getMime(contentType);
                String charset = getCharset(contentType);
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                int statusCode = httpURLConnection.getResponseCode();
                String response = httpURLConnection.getResponseMessage();
                Map<String, List<String>> headers = httpURLConnection.getHeaderFields();
                Set<String> headerKeySet = headers.keySet();
                X5LogUtils.d("code:" + httpURLConnection.getResponseCode());
                X5LogUtils.d("mime:" + mime + "; charset:" + charset);

                // 无mime类型的请求不拦截
                if (TextUtils.isEmpty(mime)) {
                    return webView.getX5WebViewClient().shouldInterceptRequest(webView, webResourceRequest);
                } else {
                    // 二进制资源无需编码信息
                    if (!TextUtils.isEmpty(charset) || (isBinaryRes(mime))) {
                        WebResourceResponse resourceResponse = new WebResourceResponse(mime, charset, httpURLConnection.getInputStream());
                        resourceResponse.setStatusCodeAndReasonPhrase(statusCode, response);
                        Map<String, String> responseHeader = new HashMap<String, String>();
                        for (String key : headerKeySet) {
                            // HttpUrlConnection可能包含key为null的报头，指向该http请求状态码
                            responseHeader.put(key, httpURLConnection.getHeaderField(key));
                        }
                        resourceResponse.setResponseHeaders(responseHeader);
                        return resourceResponse;
                    } else {
                        X5LogUtils.d("non binary resource for " + mime);
                        return webView.getX5WebViewClient().shouldInterceptRequest(webView, webResourceRequest);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return webView.getX5WebViewClient().shouldInterceptRequest(webView, webResourceRequest);
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
     * @param mime                      mine
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
     * @param headers                   headers
     */
    private boolean containCookie(Map<String, String> headers) {
        for (Map.Entry<String, String> headerField : headers.entrySet()) {
            if (headerField.getKey().contains("Cookie")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否重定向
     * @param code                      code码
     * @return
     */
    private boolean needRedirect(int code) {
        return code >= 300 && code < 400;
    }


}
