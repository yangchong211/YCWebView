#### 问题汇总目录介绍
- 01.关于js注入时机修改
- 02.WebView进化史介绍
- 03.提前初始化WebView必要性
- 04.如何保证js安全性
- 07.如何代码开启硬件加速
- 08.WebView设置Cookie
- 09.webView加载网页不显示图片
- 11.提前显示加载进度条
- 12.绕过证书校验漏洞
- 13.allowFileAccess漏洞
- 14.WebView嵌套ScrollView问题
- 15.WebView中图片点击放大


### 参考博客
- https://juejin.im/post/58a037df86b599006b3fade4


### 01.关于js注入时机修改
- 反馈的问题：js注入时间有bug，OnPageFinished中注入，虽然最后都会全局注入成功，但是完成时间有可能太晚，当页面在初始化调用接口函数时会等待时间过长，可能不成功
- 修改代码如下所示
    ```
    /**
     * 当页面加载完成会调用该方法
     * @param view                              view
     * @param url                               url链接
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (!X5WebUtils.isConnected(webView.getContext()) && webListener!=null) {
            //隐藏进度条方法
            webListener.hindProgressBar();
        }
        super.onPageFinished(view, url);
        //设置网页在加载的时候暂时不加载图片
        //webView.getSettings().setBlockNetworkImage(false);
        //页面finish后再发起图片加载
        if(!webView.getSettings().getLoadsImagesAutomatically()) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        }
        //这个时候添加js注入方法
        /*BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                //分发message 必须在主线程才分发成功
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }*/
        //html加载完成之后，添加监听图片的点击js函数
        //addImageClickListener();
        //addImageClickListener(webView);
    }
    
    
    //修改后代码
     * 这个方法是监听加载进度变化的，当加载到百分之八十五的时候，页面一般就出来呢
     * 作用：获得网页的加载进度并显示
     * @param view                              view
     * @param newProgress                       进度值
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (webListener!=null){
            webListener.startProgress(newProgress);
            int max = 85;
            if (newProgress> max && !isShowContent){
                webListener.hindProgressBar();
                isShowContent = true;
    
                //在这个时候添加js注入方法，具体可以看readme文档
                BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
                if (webView.getStartupMessage() != null) {
                    for (Message m : webView.getStartupMessage()) {
                        webView.dispatchMessage(m);
                    }
                    webView.setStartupMessage(null);
                }
            }
        }
    }
    ```


### 02.WebView进化史介绍
- 进化史如下所示
    - 从Android4.4系统开始，Chromium内核取代了Webkit内核。
    - 从Android5.0系统开始，WebView移植成了一个独立的apk，可以不依赖系统而独立存在和更新。
    - 从Android7.0 系统开始，如果用户手机里安装了 Chrome ， 系统优先选择 Chrome 为应用提供 WebView 渲染。
    - 从Android8.0系统开始，默认开启WebView多进程模式，即WebView运行在独立的沙盒进程中。



### 03.提前初始化WebView必要性
- https://www.jianshu.com/p/fc7909e24178
- 代码里怎么设置Cookie，如下所示
    ```
    /**
     * 同步cookie
     *
     * @param url               地址
     * @param cookieList        需要添加的Cookie值,以键值对的方式:key=value
     */
    private void syncCookie (Context context , String url, ArrayList<String> cookieList) {
        //初始化
        CookieSyncManager.createInstance(context);
        //获取对象
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //移除
        cookieManager.removeSessionCookie();
        //添加
        if (cookieList != null && cookieList.size() > 0) {
            for (String cookie : cookieList) {
                cookieManager.setCookie(url, cookie);
            }
        }
        String cookies = cookieManager.getCookie(url);
        X5LogUtils.d("cookies-------"+cookies);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }
    ```
- 在android里面在调用webView.loadUrl(url)之前一句调用此方法就可以给WebView设置Cookie 
    - 注:这里一定要注意一点，在调用设置Cookie之后不能再设置，否则设置Cookie无效。该处需要校验，为何？？？
    ```
    webView.getSettings().setBuiltInZoomControls(true);  
    webView.getSettings().setJavaScriptEnabled(true);  
    ```


### 04.如何保证js安全性
- Android和js如何通信
    - 为了与Web页面实现动态交互，Android应用程序允许WebView通过WebView.addJavascriptInterface接口向Web页面注入Java对象，页面Javascript脚本可直接引用该对象并调用该对象的方法。
    - 这类应用程序一般都会有类似如下的代码：
        ```
        webView.addJavascriptInterface(javaObj, "jsObj");
        ```
    - 此段代码将javaObj对象暴露给js脚本，可以通过jsObj对象对其进行引用，调用javaObj的方法。结合Java的反射机制可以通过js脚本执行任意Java代码，相关代码如下：
        - 当受影响的应用程序执行到上述脚本的时候，就会执行someCmd指定的命令。
        ```
        <script>
        　　function execute(cmdArgs) {
            　　return jsobj.getClass().forName("java.lang.Runtime").getMethod("getRuntime",null).invoke(null,null).exec(cmdArgs);
        　　}
        
        　　execute(someCmd);
        </script>
        ```
- addJavascriptInterface任何命令执行漏洞
    - 在webview中使用js与html进行交互是一个不错的方式，但是，在Android4.2(16，包含4.2)及以下版本中，如果使用addJavascriptInterface，则会存在被注入js接口的漏洞；在4.2之后，由于Google增加了@JavascriptInterface，该漏洞得以解决。
- @JavascriptInterface注解做了什么操作
    - 之前，任何Public的函数都可以在JS代码中访问，而Java对象继承关系会导致很多Public的函数都可以在JS中访问，其中一个重要的函数就是getClass()。然后JS可以通过反射来访问其他一些内容。通过引入 @JavascriptInterface注解，则在JS中只能访问 @JavascriptInterface注解的函数。这样就可以增强安全性。


### 07.如何代码开启硬件加速
- 开启软硬件加速这个性能提升还是很明显的，但是会耗费更大的内存 。直接调用代码api即可完成，webView.setOpenLayerType(true);


### 08.WebView设置Cookie
- https://www.jianshu.com/p/53897db4d734


### 09.webView加载网页不显示图片
- webView从Lollipop(5.0)开始webView默认不允许混合模式, https当中不能加载http资源, 而开发的时候可能使用的是https的链接, 但是链接中的图片可能是http的, 所以需要设置开启。
    ```
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    mWebView.getSettings().setBlockNetworkImage(false);
    ```


### 11.提前显示加载进度条
- 提前显示进度条不是提升性能 ， 但是对用户体验来说也是很重要的一点 ， WebView.loadUrl("url") 不会立马就回调 onPageStarted 或者 onProgressChanged 因为在这一时间段，WebView 有可能在初始化内核，也有可能在与服务器建立连接，这个时间段容易出现白屏，白屏用户体验是很糟糕的 ，所以建议
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
    ```


### 12.绕过证书校验漏洞
- webviewClient中有onReceivedError方法，当出现证书校验错误时，我们可以在该方法中使用handler.proceed()来忽略证书校验继续加载网页，或者使用默认的handler.cancel()来终端加载。
    - 因为我们使用了handler.proceed()，由此产生了该“绕过证书校验漏洞”。如果确定所有页面都能满足证书校验，则不必要使用handler.proceed()
    ```
    @SuppressLint("NewApi")
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        //handler.proceed();// 接受证书
        super.onReceivedSslError(view, handler, error);
    }
    ```



### 13.allowFileAccess漏洞
- 如果webview.getSettings().setAllowFileAccess(boolean)设置为true，则会面临该问题；该漏洞是通过WebView对Javascript的延时执行和html文件替换产生的。
    - 解决方案是禁止WebView页面打开本地文件，即：webview.getSettings().setAllowFileAccess(false);
    - 或者更直接的禁止使用JavaScript：webview.getSettings().setJavaScriptEnabled(false);



### 14.WebView嵌套ScrollView问题
- 问题描述
    - 当 WebView 嵌套在 ScrollView 里面的时候，如果 WebView 先加载了一个高度很高的网页，然后加载了一个高度很低的网页，就会造成 WebView 的高度无法自适应，底部出现大量空白的情况出现。
- 为何出现这种情况



### 15.WebView中图片点击放大




