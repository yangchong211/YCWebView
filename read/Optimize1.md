#### 优化汇总目录介绍
- 5.0.1 视频全屏播放按返回页面被放大
- 5.0.2 加快加载webView中的图片资源
- 5.0.3 自定义加载异常error的状态页面
- 5.0.4 WebView硬件加速导致页面渲染闪烁
- 5.0.5 WebView加载证书错误
- 5.0.6 web音频播放销毁后还有声音
- 5.0.7 DNS采用和客户端API相同的域名
- 5.0.8 如何设置白名单操作
- 5.0.9 后台无法释放js导致发热耗电
- 5.1.0 可以提前显示加载进度条
- 5.1.1 WebView密码明文存储漏洞优化
- 5.1.2 页面关闭后不要执行web中js
- 5.1.3 WebView + HttpDns优化
- 5.1.4 如何禁止WebView返回时刷新
- 5.1.5 WebView处理404、500逻辑



### 5.0.1 视频全屏播放按返回页面被放大（部分手机出现)
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


### 5.0.2 加载webView中的资源时，加快加载的速度优化，主要是针对图片
- html代码下载到WebView后，webkit开始解析网页各个节点，发现有外部样式文件或者外部脚本文件时，会异步发起网络请求下载文件，但如果在这之前也有解析到image节点，那势必也会发起网络请求下载相应的图片。
- 在网络情况较差的情况下，过多的网络请求就会造成带宽紧张，影响到css或js文件加载完成的时间，造成页面空白loading过久。解决的方法就是告诉WebView先不要自动加载图片，等页面finish后再发起图片加载。
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



### 5.0.3 自定义加载异常error的状态页面，比如下面这些方法中可能会出现error
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



### 5.0.4 WebView硬件加速导致页面渲染闪烁
- 4.0以上的系统我们开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑。但有个副作用就是，当WebView视图被整体遮住一块，然后突然恢复时（比如使用SlideMenu将WebView从侧边滑出来时），这个过渡期会出现白块同时界面闪烁。解决这个问题的方法是在过渡期前将WebView的硬件加速临时关闭，过渡期后再开启
    ```
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    ```
- 关于硬件加速的原理是什么？



    
### 5.0.5 WebView加载证书错误
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


### 5.0.7 DNS采用和客户端API相同的域名
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



### 5.0.8 如何设置白名单操作
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
- 设置白名单操作其实和过滤广告是一个意思，这里你可以放一些合法的网址允许访问。



### 5.0.9 后台无法释放js导致发热耗电
- 在有些手机你如果webView加载的html里，有一些js一直在执行比如动画之类的东西，如果此刻webView 挂在了后台这些资源是不会被释放用户也无法感知。
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


### 5.1.0 可以提前显示加载进度条
- 提前显示进度条不是提升性能，但是对用户体验来说也是很重要的一点 ， WebView.loadUrl("url") 不会立马就回调 onPageStarted 或者 onProgressChanged 因为在这一时间段，WebView 有可能在初始化内核，也有可能在与服务器建立连接，这个时间段容易出现白屏，白屏用户体验是很糟糕的 ，所以建议
    ```
    //正确
    pb.setVisibility(View.VISIBLE);
    mWebView.loadUrl("https://github.com/yangchong211/LifeHelper");
    
    //不太好
    @Override
    public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
        super.onPageStarted(webView, s, bitmap);
        //设定加载开始的操作
        pb.setVisibility(View.VISIBLE);
    }
    
    //下面这个是监听进度条进度变化的逻辑
    mWebView.getX5WebChromeClient().setWebListener(interWebListener);
    mWebView.getX5WebViewClient().setWebListener(interWebListener);
    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            pb.setVisibility(View.GONE);
        }

        @Override
        public void showErrorView() {

        }

        @Override
        public void startProgress(int newProgress) {
            pb.setProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }
    };
    ```


### 5.1.1 WebView密码明文存储漏洞优化
- WebView 默认开启密码保存功能 mWebView.setSavePassword(true)，如果该功能未关闭，在用户输入密码时，会弹出提示框，询问用户是否保存密码，如果选择”是”，密码会被明文保到 /data/data/com.package.name/databases/webview.db 中，这样就有被盗取密码的危险，所以需要通过 WebSettings.setSavePassword(false) 关闭密码保存提醒功能。
    - 具体代码操作如下所示
    ```
    /设置是否开启密码保存功能，不建议开启，默认已经做了处理，存在盗取密码的危险
    mX5WebView.setSavePassword(false);
    ```


### 5.1.2 页面关闭后不要执行web中js
- 页面关闭后，直接返回，不要执行网络请求和js方法。代码如下所示：
    ```
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        X5LogUtils.i("-------shouldOverrideUrlLoading----2---"+request.getUrl().toString());
        //页面关闭后，直接返回，不要执行网络请求和js方法
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
        if (!activityAlive){
            return false;
        }
    }
    ```


### 5.1.3 WebView + HttpDns优化
- HttpDns，使用http协议向特定的DNS服务器进行域名解析请求，代替基于DNS协议向运营商的Local DNS发起解析请求，可以降低运营商DNS劫持带来的访问失败。
- 阿里云HTTP-DNS是避免dns劫持的一种有效手段，在许多特殊场景如HTTPS/SNI、okhttp等都有最佳实践，事实上很多场景依然可以通过HTTP-DNS进行IP直连，这个方案具体可以看阿里的官方demo和文档，我自己本身也没有实践过，这里只是提一下。
    - 参考链接：[Android Webview + HttpDns最佳实践](https://help.aliyun.com/document_detail/60181.html?spm=5176.11065259.1996646101.searchclickresult.431f492dDakb73)


### 5.1.4 如何禁止WebView返回时刷新
- webView在内部跳转的新的链接的时候，发现总会在返回的时候reload()一遍，但有时候我们希望保持上个状态。两种解决办法
- 第一种方法
    - 如果仅仅是简单的不更新数据，可以设置： mX5WebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
    - 如果你在网上搜索，你会发现很多都是这个回答，我也不知道别人究竟有没有试过这种方法，这个方案是不可行的。
- 第二种方法
    - new一个WebView，有人说部分浏览器都是从新new的webView，借鉴这种方法亲测可用： 
    - 布局里添加一个容器：
        ```
        <FrameLayout
            android:id="@+id/webContentLayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
        ```
    - 然后动态生成WebView，并且添加进去就可以
        ```
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_web);
            webContentLayout = (FrameLayout)findViewBy(R.id.webContentLayout);
            addWeb(url);
        }
        
        private void addWeb(String url) {
            WebView mWeb = new WebView(MainActivity.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            mWeb.setLayoutParams(params);
            mWeb.setWebChromeClient(new WebChromeClient());
            mWeb.setWebViewClient(new MyWebViewClient());
            mWeb.getSettings().setJavaScriptEnabled(true);
            mWeb.loadUrl(url);
            webContentLayout.addView(mWeb);
        }
        
        //截获跳转
        private class MyWebViewClient extends WebViewClient{
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading: " + url);
                if (!urlList.contains(url)) {
                    addWeb(url);
                    urlList.add(url);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        }
        
        //返回处理，和传统的mWeb.canGoBack()不一样了，而是直接remove
        @Override
        public void onBackPressed() {
            int childCount = webContentLayout.getChildCount();
            if (childCount > 1) {
                webContentLayout.removeViewAt(childCount - 1);
            } else {
                super.onBackPressed();
            }
        }
        ```


### 5.1.5 WebView处理404、500逻辑
- 在Android6.0以下的系统我们如何处理404这样的问题呢？
    - 通过获取网页的title，判断是否为系统默认的404页面。在WebChromeClient()子类中可以重写他的onReceivedTitle()方法
    ```
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        // android 6.0 以下通过title获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (title.contains("404") || title.contains("500") || title.contains("Error")) {
                // 避免出现默认的错误界面
                view.loadUrl("about:blank");
                //view.loadUrl(mErrorUrl);
            }
        }
    }
    ```
- Android6.0以上判断404或者500
    ```
    @TargetApi(android.os.Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        // 这个方法在6.0才出现
        int statusCode = errorResponse.getStatusCode();
        System.out.println("onReceivedHttpError code = " + statusCode);
        if (404 == statusCode || 500 == statusCode) {
            //避免出现默认的错误界面
            view.loadUrl("about:blank");
            //view.loadUrl(mErrorUrl);
        }
    }
    ```



