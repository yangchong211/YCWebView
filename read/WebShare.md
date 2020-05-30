#### WebView分享目录介绍
- loadUrl到底做了什么
- webView重定向怎么办
- js交互的一点知识分享
- 拦截缓存如何优雅处理
- webView独立进程处理
- 关于一些问题和优化
- 关于一点面向对象的思想



### loadUrl到底做了什么



### webView重定向怎么办



### js交互的一点知识分享
- js交互介绍
    - Java调用js方法有两种：
        - WebView.loadUrl("javascript:" + javascript);
        - WebView.evaluateJavascript(javascript, callbacck);
    - js调用Java的方法有四种，分别是：
        - JavascriptInterface
        - WebViewClient.shouldOverrideUrlLoading()
        - WebChromeClient.onConsoleMessage()
        - WebChromeClient.onJsPrompt()




### 拦截缓存如何优雅处理



### webView独立进程处理



### 关于一些问题和优化



### 关于一点面向对象的思想
- 针对webView视频播放演变
    - 1.最刚开始把视频全屏show和hide的逻辑都放到X5WebChromeClient中处理，相当于这个类中逻辑比较多
    - 2.后期把视频全屏播放逻辑都抽到了VideoWebChromeClient类中处理，这样只需要继承该类即可。这个类独立，拿来即用。
    - 3.后期演变，一个视频全屏播放接口 + 接口实现类 + VideoChromeClient，接口主要能够解耦
- 关于webView拦截缓存处理
    - 1.代码结构大概是：拦截缓存接口 + 接口实现类 + 接口委派类
    - 2.优点：委派类和实现类解耦；便于增加过滤功能(比如用了https+dns优化就不用拦截缓存)；
    ```
    //1.创建委托对象
    WebViewCacheDelegate webViewCacheDelegate = WebViewCacheDelegate.getInstance();
    //2.通过委托对象调用方法
    WebResourceResponse webResourceResponse = webViewCacheDelegate.interceptRequest(url);
    ```










