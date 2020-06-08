package com.ycbjie.webviewlib.cache;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.cache.CacheInterceptor;


/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2020/5/17
 *     desc  : http缓存拦截起，主要是设置Cache-Control的这个属性
 *     revise:
 * </pre>
 */
public class HttpCacheInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String cache = request.header(WebViewCacheWrapper.KEY_CACHE);
        Response originResponse = chain.proceed(request);
        if (!TextUtils.isEmpty(cache)&&cache.equals(WebCacheType.NORMAL.ordinal()+"")){
            return originResponse;
        }
        //Cache-Control 是最重要的规则。常见的取值有private、public、no-cache、max-age、no-store、默认是private
        //Cache-Control仅指定了max-age所以默认是private。
        //缓存时间是31536000，也就是说这个时间段的再次请求这条数据，都会直接获取缓存数据库中的数据，直接使用。
        //关于缓存原理，可以重点看一下这个类的源码：CacheInterceptor
        return originResponse.newBuilder()
                .removeHeader("pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control","max-age=3153600000")
                .build();
    }

}
