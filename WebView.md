#### 基础使用目录介绍
- 04.WebView.loadUrl(url)流程
- 05.js的调用时机分析
- 06.清除缓存数据方式有哪些





### 参考博客
- https://blog.csdn.net/carson_ho/article/details/64904691/
- https://juejin.im/post/58a037df86b599006b3fade4




### 04.WebView.loadUrl(url)流程
- WebView.loadUrl(url)加载网页做了什么？
    - 加载网页是一个复杂的过程，在这个过程中，我们可能需要执行一些操作，包括：
    - 加载网页前，重置WebView状态以及与业务绑定的变量状态。WebView状态包括重定向状态(mTouchByUser)、前端控制的回退栈(mBackStep)等，业务状态包括进度条、当前页的分享内容、分享按钮的显示隐藏等。
    - 加载网页前，根据不同的域拼接本地客户端的参数，包括基本的机型信息、版本信息、登录信息以及埋点使用的Refer信息等，有时候涉及交易、财产等还需要做额外的配置。
    - 开始执行页面加载操作时，会回调WebViewClient.onPageStarted(webview,url,favicon)。在此方法中，可以重置重定向保护的变量(mRedirectProtected)，当然也可以在页面加载前重置，由于历史遗留代码问题，此处尚未省去优化。
    - 加载页面的过程中，WebView会回调几个方法。
    - 页面加载结束后，WebView会回调几个方法。
- 加载页面的过程中回调哪些方法？
    - WebChromeClient.onReceivedTitle(webview, title)，用来设置标题。需要注意的是，在部分Android系统版本中可能会回调多次这个方法，而且有时候回调的title是一个url，客户端可以针对这种情况进行特殊处理，避免在标题栏显示不必要的链接。
    - WebChromeClient.onProgressChanged(webview, progress)，根据这个回调，可以控制进度条的进度（包括显示与隐藏）。一般情况下，想要达到100%的进度需要的时间较长（特别是首次加载），用户长时间等待进度条不消失必定会感到焦虑，影响体验。其实当progress达到80的时候，加载出来的页面已经基本可用了。事实上，国内厂商大部分都会提前隐藏进度条，让用户以为网页加载很快。
    - WebViewClient.shouldInterceptRequest(webview, request)，无论是普通的页面请求(使用GET/POST)，还是页面中的异步请求，或者页面中的资源请求，都会回调这个方法，给开发一次拦截请求的机会。在这个方法中，我们可以进行静态资源的拦截并使用缓存数据代替，也可以拦截页面，使用自己的网络框架来请求数据。包括后面介绍的WebView免流方案，也和此方法有关。
    - WebViewClient.shouldOverrideUrlLoading(webview, request)，如果遇到了重定向，或者点击了页面中的a标签实现页面跳转，那么会回调这个方法。可以说这个是WebView里面最重要的回调之一，后面WebView与Native页面交互一节将会详细介绍这个方法。
    - WebViewClient.onReceivedError(webview,handler,error)，加载页面的过程中发生了错误，会回调这个方法。主要是http错误以及ssl错误。在这两个回调中，我们可以进行异常上报，监控异常页面、过期页面，及时反馈给运营或前端修改。在处理ssl错误时，遇到不信任的证书可以进行特殊处理，例如对域名进行判断，针对自己公司的域名“放行”，防止进入丑陋的错误证书页面。也可以与Chrome一样，弹出ssl证书疑问弹窗，给用户选择的余地。
- 加载页面结束回调哪些方法
    - 会回调WebViewClient.onPageFinished(webview,url)。
    - 这时候可以根据回退栈的情况判断是否显示关闭WebView按钮。通过mActivityWeb.canGoBackOrForward(-1)判断是否可以回退。



### 05.js的调用时机分析
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



### 06.清除缓存数据方式有哪些
- 清除缓存数据的方法有哪些？
    ```
    //清除网页访问留下的缓存
    //由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
    Webview.clearCache(true);
    
    //清除当前webview访问的历史记录//只会webview访问历史记录里的所有记录除了当前访问记录
    Webview.clearHistory()；
    
    //这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
    Webview.clearFormData()；
    ```




