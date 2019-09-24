#### 目录介绍
- 01.前沿说明
    - 1.1 案例展示效果
    - 1.2 该库功能和优势
    - 1.3 相关类介绍说明
- 02.如何使用
    - 2.1 如何引入
    - 2.2 最简单使用
    - 2.3 常用api
    - 2.4 使用建议
- 03.js调用
    - 3.1 如何使用项目js调用
    - 3.2 js的调用时机分析
- 04.问题反馈
    - 4.0.1 视频播放宽度超过屏幕
    - 4.0.2 x5加载office资源
    - 4.0.3 WebView播放视频问题
    - 4.0.4 无法获取webView的正确高度
    - 4.0.5 使用scheme协议打开链接风险
    - 4.0.6 如何处理加载错误
- 05.webView优化
    - 5.0.1 视频全屏播放按返回页面被放大
    - 5.0.2 加快加载webView中的图片资源
    - 5.0.3 自定义加载异常error的状态页面
    - 5.0.4 WebView硬件加速导致页面渲染闪烁
    - 5.0.5 WebView加载证书错误
    - 5.0.6 web音频播放销毁后还有声音
    - 5.0.7 DNS采用和客户端API相同的域名
    - 5.0.8 如何设置白名单操作
    - 5.0.9 后台无法释放js导致发热耗电
- 06.关于参考
- 07.其他说明介绍


### 01.前沿说明
- 基于腾讯x5封源库，提高webView开发效率，大概要节约你百分之六十的时间成本。该案例支持处理js的交互逻辑且无耦合、同时暴露进度条加载进度、可以监听异常error状态、支持视频播放并且可以全频、支持加载word，xls，ppt，pdf，txt等文件文档、发短信、打电话、发邮件、打开文件操作上传图片、唤起原生App、x5库为最新版本，功能强大。


#### 1.1 案例展示效果
- WebView启动过程大概分为以下几个阶段，这里借鉴美团的一张图片
    - ![image](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2017/9a2f8beb.png)
- 项目案例效果展示图
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-0.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-1.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-2.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-3.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-4.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-5.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-6.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-7.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-8.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-9.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-10.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-11.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-12.jpg" width="500" hegiht="313" />



#### 1.2 该库功能和优势
- 提高webView开发效率，大概要节约你百分之六十的时间成本，一键初始化操作；
- 支持处理js的交互逻辑，方便快捷，并且无耦合；
- 暴露进度条加载进度，结束，以及异常状态listener给开发者；
- 支持视频播放，可以切换成全频播放视频，可旋转屏幕；
- 集成了腾讯x5的WebView，最新版本，功能强大；
- 支持打开文件的操作，比如打开相册，然后选中图片上传，兼容版本(5.0)
- 支持加载word，xls，ppt，pdf，txt等文件文档，使用方法十分简单



#### 1.3 相关类介绍说明
- BridgeHandler         接口，主要处理消息回调逻辑
- BridgeUtil            工具类，静态常量，以及获取js消息的一些方法，final修饰
- BridgeWebView         自定义WebView类，主要处理与js之间的消息
- CallBackFunction      js回调
- DefaultHandler        默认的BridgeHandler
- InterWebListener      接口，web的接口回调，包括常见状态页面切换【状态页面切换】，进度条变化【显示和进度监听】等
- Message               自定义消息Message实体类
- ProgressWebView       自定义带进度条的webView
- WebViewJavascriptBridge       js桥接接口
- X5WebChromeClient     自定义x5的WebChromeClient，处理进度监听，title变化，以及上传图片，后期添加视频处理逻辑
- X5WebUtils            工具类，初始化腾讯x5浏览器webView，及调用该类init方法
- X5WebView             可以使用这个类，方便统一初始化WebSettings的一些属性，如果不用这里的，想单独初始化setting属性，也可以直接使用BridgeWebView
- X5WebViewClient       自定义x5的WebViewClient，如果要自定义WebViewClient必须要集成此类，一定要继承该类，因为注入js监听是在该类中操作的



### 02.如何使用
#### 2.1 如何引入
- **如何引用，该x5的库已经更新到最新版本**
    ```
    implementation 'cn.yc:WebViewLib:1.1.2'
    ```

#### 2.2 最简单使用
- **项目初始化**
    ```
    X5WebUtils.init(this);
    ```
- **最普通使用，需要自己做手动设置setting相关属性**
    ```
    <BridgeWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- **也可以使用X5WebView，已经做了常见的setting属性设置**
    ```
    <X5WebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- **如果想有带进度的，可以使用ProgressWebView**
    ```
    <可以使用ProgressWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```

#### 2.3 常用api
- **关于web的接口回调，包括常见状态页面切换，进度条变化等监听处理**
    ```
    mWebView.getX5WebChromeClient().setWebListener(interWebListener);
    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            pb.setVisibility(View.GONE);
        }
    
        @Override
        public void showErrorView() {
            //设置自定义异常错误页面
        }
    
        @Override
        public void startProgress(int newProgress) {
            pb.setProgress(newProgress);
        }
    };
    ```
- **关于视频播放的时候，web的接口回调，主要是视频相关回调，比如全频，取消全频，隐藏和现实webView**
    ```
    x5WebChromeClient = x5WebView.getX5WebChromeClient();
    x5WebChromeClient.setVideoWebListener(new VideoWebListener() {
        @Override
        public void showVideoFullView() {
            //视频全频播放时监听
        }
    
        @Override
        public void hindVideoFullView() {
            //隐藏全频播放，也就是正常播放视频
        }
    
        @Override
        public void showWebView() {
            //显示webView
        }
    
        @Override
        public void hindWebView() {
            //隐藏webView
        }
    });
    ```

#### 2.4 使用建议
- **优化一下相关的操作**
    - 关于设置js支持的属性
    ```
    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }
    ```
    - 关于destroy销毁逻辑
    ```
    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                webView.stopLoading();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            Log.e("X5WebViewActivity", e.getMessage());
        }
        super.onDestroy();
    }
    ```


### 03.js调用
#### 3.1 如何使用项目js调用
- **代码如下所示，下面中的jsname代表的是js这边提供给客户端的方法名称**
    ```
    mWebView.registerHandler("jsname", new BridgeHandler() {
        @Override
        public void handler(String data, CallBackFunction function) {
            
        }
    });
    ```
- **如何回调数据给web那边**
    ```
    function.onCallBack("回调数据");
    ```


#### 3.2 js的调用时机分析
- **onPageFinished()或者onPageStarted()方法中注入js代码**
    - 做过WebView开发，并且需要和js交互，大部分都会认为js在WebViewClient.onPageFinished()方法中注入最合适，此时dom树已经构建完成，页面已经完全展现出来。但如果做过页面加载速度的测试，会发现WebViewClient.onPageFinished()方法通常需要等待很久才会回调（首次加载通常超过3s），这是因为WebView需要加载完一个网页里主文档和所有的资源才会回调这个方法。
    - 能不能在WebViewClient.onPageStarted()中注入呢？答案是不确定。经过测试，有些机型可以，有些机型不行。在WebViewClient.onPageStarted()中注入还有一个致命的问题——这个方法可能会回调多次，会造成js代码的多次注入。
    - 从7.0开始，WebView加载js方式发生了一些小改变，**官方建议把js注入的时机放在页面开始加载之后**。
- **WebViewClient.onProgressChanged()方法中注入js代码**
    - WebViewClient.onProgressChanged()这个方法在dom树渲染的过程中会回调多次，每次都会告诉我们当前加载的进度。
        - 在这个方法中，可以给WebView自定义进度条，类似微信加载网页时的那种进度条
        - 如果在此方法中注入js代码，则需要避免重复注入，需要增强逻辑。可以定义一个boolean值变量控制注入时机
    - 那么有人会问，加载到多少才需要处理js注入逻辑呢？
        - 正是因为这个原因，页面的进度加载到80%的时候，实际上dom树已经渲染得差不多了，表明WebView已经解析了<html>标签，这时候注入一定是成功的。在WebViewClient.onProgressChanged()实现js注入有几个需要注意的地方：
        - 1 上文提到的多次注入控制，使用了boolean值变量控制
        - 2 重新加载一个URL之前，需要重置boolean值变量，让重新加载后的页面再次注入js
        - 3 如果做过本地js，css等缓存，则先判断本地是否存在，若存在则加载本地，否则加载网络js
        - 4 注入的进度阈值可以自由定制，理论上10%-100%都是合理的，不过建议使用了75%到90%之间可以。



### 04.问题反馈
#### 4.0.1 视频播放宽度超过屏幕
- 视频播放宽度比webView设置的宽度大，超过屏幕：这个时候可以设置ws.setLoadWithOverviewMode(false);


#### 4.0.2 x5加载office资源
- 关于加载word，pdf，xls等文档文件注意事项：Tbs不支持加载网络的文件，需要先把文件下载到本地，然后再加载出来
- 还有一点要注意，在onDestroy方法中调用此方法mTbsReaderView.onStop()，否则第二次打开无法浏览。更多可以看FileReaderView类代码！



#### 4.0.3 WebView播放视频问题
- 1、此次的方案用到WebView，而且其中会有视频嵌套，在默认的WebView中直接播放视频会有问题， 而且不同的SDK版本情况还不一样，网上搜索了下解决方案，在此记录下. webView.getSettings.setPluginState(PluginState.ON);webView.setWebChromeClient(new WebChromeClient());
- 2、然后在webView的Activity配置里面加上： android:hardwareAccelerated="true"
- 3、以上可以正常播放视频了，但是webview的页面都finish了居然还能听 到视频播放的声音， 于是又查了下发现webview的onResume方法可以继续播放，onPause可以暂停播放， 但是这两个方法都是在Added in API level 11添加的，所以需要用反射来完成。
- 4、停止播放：在页面的onPause方法中使用：webView.getClass().getMethod("onPause").invoke(webView, (Object[])null);
- 5、继续播放：在页面的onResume方法中使用：webView.getClass().getMethod("onResume").invoke(webView,(Object[])null);这样就可以控制视频的暂停和继续播放了。


#### 4.0.4 无法获取webView的正确高度
- 偶发情况，获取不到webView的内容高度
    - 其中htmlString是一个HTML格式的字符串。
    ```
    WebView view = new WebView(context);
    view.loadData(htmlString, "text/html", "utf-8");

    view.setWebViewClient(new WebViewClient() {
        public void onPageFinished(WebView view, String url) {
         super.onPageFinished(view, url);
         Log.d("2", view.getContentheight() + "");
        }
    });
    ```
    - 这是因为onPageFinished回调指的WebView已经完成从网络读取的字节数，这一点。在点onPageFinished被激发的页面可能还没有被解析。
- 第一种解决办法：提供onPageFinished（）一些延迟
    ```
    webView.setWebViewClient(new WebViewClient() {
     @Override
     public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      new Handler().postDelayed(new Runnable() {
       @Override
       public void run() {
        int contentHeight = webView.getContentHeight();
        int viewHeight = webView.getHeight();
       }
      }, 500);
     }
    });
    ```
- 第二种解决办法：使用js获取内容高度，具体可以看这篇文章：https://www.jianshu.com/p/ad22b2649fba


#### 4.0.5 使用scheme协议打开链接风险
- 常见的用法是在APP获取到来自网页的数据后，重新生成一个intent，然后发送给别的组件使用这些数据。比如使用Webview相关的Activity来加载一个来自网页的url，如果此url来自url scheme中的参数，如：yc://ycbjie:8888/from?load_url=http://www.taobao.com。
    - 如果在APP中，没有检查获取到的load_url的值，攻击者可以构造钓鱼网站，诱导用户点击加载，就可以盗取用户信息。
    - 这个时候，别人非法篡改参数，于是将scheme协议改成yc://ycbjie:8888/from?load_url=http://www.doubi.com。这个时候点击进去即可进入钓鱼链接地址。
- 使用建议
    - APP中任何接收外部输入数据的地方都是潜在的攻击点，过滤检查来自网页的参数。
    - 不要通过网页传输敏感信息，有的网站为了引导已经登录的用户到APP上使用，会使用脚本动态的生成URL Scheme的参数，其中包括了用户名、密码或者登录态token等敏感信息，让用户打开APP直接就登录了。恶意应用也可以注册相同的URL Sechme来截取这些敏感信息。Android系统会让用户选择使用哪个应用打开链接，但是如果用户不注意，就会使用恶意应用打开，导致敏感信息泄露或者其他风险。


#### 4.0.6 如何处理加载错误(Http、SSL、Resource)
- 对于WebView加载一个网页过程中所产生的错误回调，大致有三种
    ```
    /**
     * 只有在主页面加载出现错误时，才会回调这个方法。这正是展示加载错误页面最合适的方法。
     * 然而，如果不管三七二十一直接展示错误页面的话，那很有可能会误判，给用户造成经常加载页面失败的错觉。
     * 由于不同的WebView实现可能不一样，所以我们首先需要排除几种误判的例子：
     *      1.加载失败的url跟WebView里的url不是同一个url，排除；
     *      2.errorCode=-1，表明是ERROR_UNKNOWN的错误，为了保证不误判，排除
     *      3failingUrl=null&errorCode=-12，由于错误的url是空而不是ERROR_BAD_URL，排除
     * @param webView                                           webView
     * @param errorCode                                         errorCode
     * @param description                                       description
     * @param failingUrl                                        failingUrl
     */
    @Override
    public void onReceivedError(WebView webView, int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError(webView, errorCode, description, failingUrl);
        // -12 == EventHandle.ERROR_BAD_URL, a hide return code inside android.net.http package
        if ((failingUrl != null && !failingUrl.equals(webView.getUrl())
                && !failingUrl.equals(webView.getOriginalUrl())) /* not subresource error*/
                || (failingUrl == null && errorCode != -12) /*not bad url*/
                || errorCode == -1) { //当 errorCode = -1 且错误信息为 net::ERR_CACHE_MISS
            return;
        }
        if (!TextUtils.isEmpty(failingUrl)) {
            if (failingUrl.equals(webView.getUrl())) {
                //做自己的错误操作，比如自定义错误页面
            }
        }
    }

    /**
     * 只有在主页面加载出现错误时，才会回调这个方法。这正是展示加载错误页面最合适的方法。
     * 然而，如果不管三七二十一直接展示错误页面的话，那很有可能会误判，给用户造成经常加载页面失败的错觉。
     * 由于不同的WebView实现可能不一样，所以我们首先需要排除几种误判的例子：
     *      1.加载失败的url跟WebView里的url不是同一个url，排除；
     *      2.errorCode=-1，表明是ERROR_UNKNOWN的错误，为了保证不误判，排除
     *      3failingUrl=null&errorCode=-12，由于错误的url是空而不是ERROR_BAD_URL，排除
     * @param webView                                           webView
     * @param webResourceRequest                                webResourceRequest
     * @param webResourceError                                  webResourceError
     */
    @Override
    public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest,
                                WebResourceError webResourceError) {
        super.onReceivedError(webView, webResourceRequest, webResourceError);
    }

    /**
     * 任何HTTP请求产生的错误都会回调这个方法，包括主页面的html文档请求，iframe、图片等资源请求。
     * 在这个回调中，由于混杂了很多请求，不适合用来展示加载错误的页面，而适合做监控报警。
     * 当某个URL，或者某个资源收到大量报警时，说明页面或资源可能存在问题，这时候可以让相关运营及时响应修改。
     * @param webView                                           webView
     * @param webResourceRequest                                webResourceRequest
     * @param webResourceResponse                               webResourceResponse
     */
    @Override
    public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest,
                                    WebResourceResponse webResourceResponse) {
        super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
    }

    /**
     * 任何HTTPS请求，遇到SSL错误时都会回调这个方法。
     * 比较正确的做法是让用户选择是否信任这个网站，这时候可以弹出信任选择框供用户选择（大部分正规浏览器是这么做的）。
     * 有时候，针对自己的网站，可以让一些特定的网站，不管其证书是否存在问题，都让用户信任它。
     * 坑：有时候部分手机打开页面报错，绝招：让自己网站的所有二级域都是可信任的。
     * @param webView                                           webView
     * @param sslErrorHandler                                   sslErrorHandler
     * @param sslError                                          sslError
     */
    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        super.onReceivedSslError(webView, sslErrorHandler, sslError);
        //判断网站是否是可信任的，与自己网站host作比较
        if (WebViewUtils.isYCHost(webView.getUrl())) {
            //如果是自己的网站，则继续使用SSL证书
            sslErrorHandler.proceed();
        } else {
            super.onReceivedSslError(webView, sslErrorHandler, sslError);
        }
    }
    ```


### 05.webView优化
#### 5.0.1 视频全屏播放按返回页面被放大（部分手机出现)
- 至于原因暂时没有找到，解决方案如下所示
    ```
    /**
     * 当缩放改变的时候会调用该方法
     * @param view                              view
     * @param oldScale                          之前的缩放比例
     * @param newScale                          现在缩放比例
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }
    ```


#### 5.0.2 加载webView中的资源时，加快加载的速度优化，主要是针对图片
- html代码下载到WebView后，webkit开始解析网页各个节点，发现有外部样式文件或者外部脚本文件时，会异步发起网络请求下载文件，但如果在这之前也有解析到image节点，那势必也会发起网络请求下载相应的图片。在网络情况较差的情况下，过多的网络请求就会造成带宽紧张，影响到css或js文件加载完成的时间，造成页面空白loading过久。解决的方法就是告诉WebView先不要自动加载图片，等页面finish后再发起图片加载。
    ```
    //初始化的时候设置，具体代码在X5WebView类中
    if(Build.VERSION.SDK_INT >= KITKAT) {
        //设置网页在加载的时候暂时不加载图片
        ws.setLoadsImagesAutomatically(true);
    } else {
        ws.setLoadsImagesAutomatically(false);
    }
    
    /**
     * 当页面加载完成会调用该方法
     * @param view                              view
     * @param url                               url链接
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //页面finish后再发起图片加载
        if(!webView.getSettings().getLoadsImagesAutomatically()) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        }
    }
    ```


#### 5.0.3 自定义加载异常error的状态页面，比如下面这些方法中可能会出现error
- 当WebView加载页面出错时（一般为404 NOT FOUND），安卓WebView会默认显示一个出错界面。当WebView加载出错时，会在WebViewClient实例中的onReceivedError()，还有onReceivedTitle方法接收到错误
    ```
    /**
     * 请求网络出现error
     * @param view                              view
     * @param errorCode                         错误🐎
     * @param description                       description
     * @param failingUrl                        失败链接
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            String data = "Page NO FOUND！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        } else {
            if (webListener!=null){
                webListener.showErrorView();
            }
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
            X5WebUtils.log("服务器异常"+error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        if (webListener!=null){
            webListener.showErrorView();
        }
    }
    
    /**
     * 这个方法主要是监听标题变化操作的
     * @param view                              view
     * @param title                             标题
     */
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (title.contains("404") || title.contains("网页无法打开")){
            if (webListener!=null){
                webListener.showErrorView();
            }
        } else {
            // 设置title
        }
    }
    ```


#### 5.0.4 WebView硬件加速导致页面渲染闪烁
- 4.0以上的系统我们开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑。但有个副作用就是，当WebView视图被整体遮住一块，然后突然恢复时（比如使用SlideMenu将WebView从侧边滑出来时），这个过渡期会出现白块同时界面闪烁。解决这个问题的方法是在过渡期前将WebView的硬件加速临时关闭，过渡期后再开启
    ```
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    ```
- **5.0.5 WebView加载证书错误**
    - webView加载一些别人的url时候，有时候会发生证书认证错误的情况，这时候我们希望能够正常的呈现页面给用户，我们需要忽略证书错误，需要调用WebViewClient类的onReceivedSslError方法，调用handler.proceed()来忽略该证书错误。
    ```
    /**
     * 在加载资源时通知主机应用程序发生SSL错误
     * 作用：处理https请求
     * @param view                              view
     * @param handler                           handler
     * @param error                             error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        if (error!=null){
            String url = error.getUrl();
            X5WebUtils.log("onReceivedSslError----异常url----"+url);
        }
        //https忽略证书问题
        if (handler!=null){
            //表示等待证书响应
            handler.proceed();
            // handler.cancel();      //表示挂起连接，为默认方式
            // handler.handleMessage(null);    //可做其他处理
        }
    }
    ```


#### 5.0.6 web音频播放销毁后还有声音
- WebView页面中播放了音频,退出Activity后音频仍然在播放，需要在Activity的onDestory()中调用
    ```
    @Override
    protected void onDestroy() {
        try {
            //有音频播放的web页面的销毁逻辑
            //在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview
            //但是注意：webview调用destory时,webview仍绑定在Activity上
            //这是由于自定义webview构建时传入了该Activity的context对象
            //因此需要先从父容器中移除webview,然后再销毁webview:
            if (webView != null) {
                ViewGroup parent = (ViewGroup) webView.getParent();
                if (parent != null) {
                    parent.removeView(webView);
                }
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            Log.e("X5WebViewActivity", e.getMessage());
        }
        super.onDestroy();
    }
    ```


#### 5.0.7 DNS采用和客户端API相同的域名
- 建立连接/服务器处理；在页面请求的数据返回之前，主要有以下过程耗费时间。
    ```
    DNS
    connection
    服务器处理
    ```
- DNS采用和客户端API相同的域名
    - DNS会在系统级别进行缓存，对于WebView的地址，如果使用的域名与native的API相同，则可以直接使用缓存的DNS而不用再发起请求图片。
    - 举个简单例子，客户端请求域名主要位于api.yc.com，然而内嵌的WebView主要位于 i.yc.com。
    - 当我们初次打开App时：客户端首次打开都会请求api.yc.com，其DNS将会被系统缓存。然而当打开WebView的时候，由于请求了不同的域名，需要重新获取i.yc.com的IP。静态资源同理，最好与客户端的资源域名保持一致。



#### 5.0.8 如何设置白名单操作
- 客户端内的WebView都是可以通过客户端的某个schema打开的，而要打开页面的URL很多都并不写在客户端内，而是可以由URL中的参数传递过去的。上面4.0.5 使用scheme协议打开链接风险已经说明了scheme使用的危险性，那么如何避免这个问题了，设置运行访问的白名单。或者当用户打开外部链接前给用户强烈而明显的提示。具体操作如下所示：
    - 在onPageStarted开始加载资源的方法中，获取加载url的host值，然后和本地保存的合法host做比较，这里domainList是一个数组
    ```
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        String host = Uri.parse(url).getHost();
        LoggerUtils.i("host:" + host);
        if (!BuildConfig.IS_DEBUG) {
            if (Arrays.binarySearch(domainList, host) < 0) {
                //不在白名单内，非法网址，这个时候给用户强烈而明显的提示
            } else {
                //合法网址
            }
        }
    }
    ```


#### 5.0.9 后台无法释放js导致发热耗电
- 在有些手机你如果webview加载的html里，有一些js一直在执行比如动画之类的东西，如果此刻webview 挂在了后台这些资源是不会被释放用户也无法感知。
- 导致一直占有cpu 耗电特别快，所以如果遇到这种情况，处理方式如下所示。大概意思就是在后台的时候，会调用onStop方法，即此时关闭js交互，回到前台调用onResume再开启js交互。
    ```
    //在onStop里面设置setJavaScriptEnabled(false);
    //在onResume里面设置setJavaScriptEnabled(true)。
    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }
    ```



### 06.关于参考
- 感谢开源库
    - [x5开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)
- 参考博客
    - [WebView性能、体验分析与优化](https://tech.meituan.com/2017/06/09/webviewperf.html)



### 07.其他说明介绍
#### 关于其他内容介绍
![image](https://upload-images.jianshu.io/upload_images/4432347-7100c8e5a455c3ee.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 关于博客汇总链接
- 1.[技术博客汇总](https://www.jianshu.com/p/614cb839182c)
- 2.[开源项目汇总](https://blog.csdn.net/m0_37700275/article/details/80863574)
- 3.[生活博客汇总](https://blog.csdn.net/m0_37700275/article/details/79832978)
- 4.[喜马拉雅音频汇总](https://www.jianshu.com/p/f665de16d1eb)
- 5.[其他汇总](https://www.jianshu.com/p/53017c3fc75d)


#### 其他推荐
- 博客笔记大汇总【15年10月到至今】，包括Java基础及深入知识点，Android技术博客，Python学习笔记等等，还包括平时开发中遇到的bug汇总，当然也在工作之余收集了大量的面试题，长期更新维护并且修正，持续完善……开源的文件是markdown格式的！同时也开源了生活博客，从12年起，积累共计47篇[近20万字]，转载请注明出处，谢谢！
- 链接地址：https://github.com/yangchong211/YCBlogs
- 如果觉得好，可以star一下，谢谢！当然也欢迎提出建议，万事起于忽微，量变引起质变！



#### 关于LICENSE
```
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
```




