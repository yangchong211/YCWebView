#### 基础使用目录介绍
- 01.常用的基础介绍
- 02.Android调用Js
- 03.Js调用Android
- 04.WebView.loadUrl(url)流程
- 05.js的调用时机分析
- 06.清除缓存数据方式有哪些
- 07.如何使用DeepLink
- 08.为什么WebView那么难搞
- 09.如何处理加载错误
- 10.应用被作为第三方浏览器打开
- 11.理解WebView独立进程
- 12.使用外部浏览器下载



### 01.常用的基础介绍
- 在activity中最简单的使用
    ```
    webview.loadUrl("http://www.baidu.com/");                    //加载web资源
    //webView.loadUrl("file:///android_asset/example.html");       //加载本地资源
    //这个时候发现一个问题，启动应用后，自动的打开了系统内置的浏览器，解决这个问题需要为webview设置 WebViewClient，并重写方法：
    webview.setWebViewClient(new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            return true;
        }
        //还可以重写其他的方法
    });
    ```
- 那些因素影响页面加载速度
    - 影响页面加载速度的因素有非常多，在对 WebView 加载一个网页的过程进行调试发现
        - 每次加载的过程中都会有较多的网络请求，除了 web 页面自身的 URL 请求
        - 有 web 页面外部引用的JS、CSS、字体、图片等等都是个独立的http请求。这些请求都是串行的，这些请求加上浏览器的解析、渲染时间就会导致 WebView 整体加载时间变长，消耗的流量也对应的真多。
- js交互介绍
    - Java调用js方法有两种：
        - WebView.loadUrl("javascript:" + javascript);
        - WebView.evaluateJavascript(javascript, callbacck);
    - js调用Java的方法有四种，分别是：
        - JavascriptInterface
        - WebViewClient.shouldOverrideUrlLoading()
        - WebChromeClient.onConsoleMessage()
        - WebChromeClient.onJsPrompt()




### 02.Android调用Js
- 第一种方式：native 调用 js 的方法，方法为：
    - 注意的是名字一定要对应上，要不然是调用不成功的，而且还有一点是 JS 的调用一定要在 onPageFinished 函数回调之后才能调用，要不然也是会失败的。
    ```
    //java
    //调用无参方法
    mWebView.loadUrl("javascript:callByAndroid()");
    //调用有参方法
    mWebView.loadUrl("javascript:showData(" + result + ")");
    
    //javascript，下面是对应的js代码
    <script type="text/javascript">
    
    function showData(result){
        alert("result"=result);
        return "success";
    }
    
    function callByAndroid(){
        console.log("callByAndroid")
        showElement("Js:无参方法callByAndroid被调用");
    }
    </script>
    ```
- 第二种方式：
    - 如果现在有需求，我们要得到一个 Native 调用 Web 的回调怎么办，Google 在 Android4.4 为我们新增加了一个新方法，这个方法比 loadUrl 方法更加方便简洁，而且比 loadUrl 效率更高，因为 loadUrl 的执行会造成页面刷新一次，这个方法不会，因为这个方法是在 4.4 版本才引入的，所以使用的时候需要添加版本的判断：
    ```
    if (Build.VERSION.SDK_INT < 18) {
        mWebView.loadUrl(jsStr);
    } else {
        mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //此处为 js 返回的结果
            }
        });
    }
    ```
- 两种方式的对比
    - 一般最常使用的就是第一种方法，但是第一种方法获取返回的值比较麻烦，而第二种方法由于是在 4.4 版本引入的，所以局限性比较大。
- 注意问题
    - 记得添加ws.setJavaScriptEnabled(true)代码




### 03.Js调用Android
- 第一种方式：通过 addJavascriptInterface 方法进行添加对象映射
    - 这种是使用最多的方式了，首先第一步我们需要设置一个属性：
    ```
    mWebView.getSettings().setJavaScriptEnabled(true);
    ```
    - 这个函数会有一个警告，因为在特定的版本之下会有非常危险的漏洞，设置完这个属性之后，Native需要定义一个类：
        - 在 API17 版本之后，需要在被调用的地方加上 @addJavascriptInterface 约束注解，因为不加上注解的方法是没有办法被调用的
    ```
    public class JSObject {
        private Context mContext;
        public JSObject(Context context) {
            mContext = context;
        }
    
        @JavascriptInterface
        public String showToast(String text) {
            Toast.show(mContext, text, Toast.LENGTH_SHORT).show();
            return "success";
        }
        
        /**
         * 前端代码嵌入js：
         * imageClick 名应和js函数方法名一致
         *
         * @param src 图片的链接
         */
        @JavascriptInterface
        public void imageClick(String src) {
            Log.e("imageClick", "----点击了图片");
        }
        
        /**
         * 网页使用的js，方法无参数
         */
        @JavascriptInterface
        public void startFunction() {
            Log.e("startFunction", "----无参");
        }
    }

    //特定版本下会存在漏洞，第一个是对象，第二个是名称
    mWebView.addJavascriptInterface(new JSObject(this), "javascriptInterface");
    ```
    - JS 代码调用
        - 这种方式的好处在于使用简单明了，本地和 JS 的约定也很简单，就是对象名称和方法名称约定好即可，缺点就是要提到的漏洞问题。
        - 在Js代码中就能直接通过“JSObject”的对象直接调用了该Native的类的方法。
    ```
    function showToast(){
        var result = myObj.showToast("我是来自web的Toast");
    }
    
    function showToast(){
        myObj.imageClick("图片");
    }
    
    function showToast(){
        myObj.startFunction();
    }
    ```
- 第二种方式：利用 WebViewClient 接口回调方法拦截 url
    - 这种方式其实实现也很简单，使用的频次也很高，上面介绍到了 WebViewClient ，其中有个回调接口 shouldOverrideUrlLoading (WebView view, String url)) ，就是利用这个拦截 url，然后解析这个 url 的协议，如果发现是我们预先约定好的协议就开始解析参数，执行相应的逻辑。注意这个方法在 API24 版本已经废弃了，需要使用 shouldOverrideUrlLoading (WebView view, WebResourceRequest request)) 替代，使用方法很类似，我们这里就使用 shouldOverrideUrlLoading (WebView view, String url)) 方法来介绍一下：
        - 代码很简单，这个方法可以拦截 WebView 中加载 url 的过程，得到对应的 url，我们就可以通过这个方法，与网页约定好一个协议，如果匹配，执行相应操作。
    ```
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //假定传入进来的 url = "js://openActivity?arg1=111&arg2=222"，代表需要打开本地页面，并且带入相应的参数
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        //如果 scheme 为 js，代表为预先约定的 js 协议
        if (scheme.equals("js")) {
              //如果 authority 为 openActivity，代表 web 需要打开一个本地的页面
            if (uri.getAuthority().equals("openActivity")) {
                  //解析 web 页面带过来的相关参数
                HashMap<String, String> params = new HashMap<>();
                Set<String> collection = uri.getQueryParameterNames();
                for (String name : collection) {
                    params.put(name, uri.getQueryParameter(name));
                }
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("params", params);
                getContext().startActivity(intent);
            }
            //代表应用内部处理完成
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }
    ```
    - JS 代码调用
    ```
    function openActivity(){
        document.location = "js://openActivity?arg1=111&arg2=222";
    }
    ```
    - 存在问题：这个代码执行之后，就会触发本地的 shouldOverrideUrlLoading 方法，然后进行参数解析，调用指定方法。这个方式不会存在第一种提到的漏洞问题，但是它也有一个很繁琐的地方是，如果 web 端想要得到方法的返回值，只能通过 WebView 的 loadUrl 方法去执行 JS 方法把返回值传递回去，相关的代码如下：
    ```
    //java
    mWebView.loadUrl("javascript:returnResult(" + result + ")");
    
    //javascript
    function returnResult(result){
        alert("result is" + result);
    }
    ```
- 第三种方式：利用 WebChromeClient 回调接口的三个方法拦截消息
    - 这个方法的原理和第二种方式原理一样，都是拦截相关接口，只是拦截的接口不一样：
    ```
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }
    
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }
    
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        //假定传入进来的 message = "js://openActivity?arg1=111&arg2=222"，代表需要打开本地页面，并且带入相应的参数
        Uri uri = Uri.parse(message);
        String scheme = uri.getScheme();
        if (scheme.equals("js")) {
            if (uri.getAuthority().equals("openActivity")) {
                HashMap<String, String> params = new HashMap<>();
                Set<String> collection = uri.getQueryParameterNames();
                for (String name : collection) {
                    params.put(name, uri.getQueryParameter(name));
                }
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("params", params);
                getContext().startActivity(intent);
                //代表应用内部处理完成
                result.confirm("success");
            }
            return true;
        }
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }
    ```
    - 和 WebViewClient 一样，这次添加的是WebChromeClient接口，可以拦截JS中的几个提示方法，也就是几种样式的对话框，在 JS 中有三个常用的对话框方法： 
        - onJsAlert 方法是弹出警告框，一般情况下在 Android 中为 Toast，在文本里面加入\n就可以换行； 
        - onJsConfirm 弹出确认框，会返回布尔值，通过这个值可以判断点击时确认还是取消，true表示点击了确认，false表示点击了取消； 
        - onJsPrompt 弹出输入框，点击确认返回输入框中的值，点击取消返回 null。
    - 但是这三种对话框都是可以本地拦截到的，所以可以从这里去做一些更改，拦截这些方法，得到他们的内容，进行解析，比如如果是 JS 的协议，则说明为内部协议，进行下一步解析然后进行相关的操作即可，prompt 方法调用如下所示：
    ```
    function clickprompt(){
        var result=prompt("js://openActivity?arg1=111&arg2=222");
        alert("open activity " + result);
    }
    ```
    - 需要注意的是 prompt 里面的内容是通过 message 传递过来的，并不是第二个参数的 url，返回值是通过 JsPromptResult 对象传递。为什么要拦截 onJsPrompt 方法，而不是拦截其他的两个方法，这个从某种意义上来说都是可行的，但是如果需要返回值给 web 端的话就不行了，因为 onJsAlert 是不能返回值的，而 onJsConfirm 只能够返回确定或者取消两个值，只有 onJsPrompt 方法是可以返回字符串类型的值，操作最全面方便。
- 以上三种方案的总结和对比
    - 以上三种方案都是可行的，在这里总结一下
    - 第一种方式：是现在目前最普遍的用法，方便简洁，但是唯一的不足是在 4.2 系统以下存在漏洞问题；
    - 第二种方式：通过拦截 url 并解析，如果是已经约定好的协议则进行相应规定好的操作，缺点就是协议的约束需要记录一个规范的文档，而且从 Native 层往 Web 层传递值比较繁琐，优点就是不会存在漏洞，iOS7 之下的版本就是使用的这种方式。
    - 第三种方式：和第二种方式的思想其实是类似的，只是拦截的方法变了，这里拦截了 JS 中的三种对话框方法，而这三种对话框方法的区别就在于返回值问题，alert 对话框没有返回值，confirm 的对话框方法只有两种状态的返回值，prompt 对话框方法可以返回任意类型的返回值，缺点就是协议的制定比较麻烦，需要记录详细的文档，但是不会存在第二种方法的漏洞问题。





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


### 07.如何使用DeepLink
- 具体可以看这篇文章：https://www.jianshu.com/p/127c80f62655




### 08.为什么WebView那么难搞
- 繁杂的WebView配置
    - WebView在初始化的时候就提供了默认配置WebSettings，但是很多默认配置是不能够满足业务需求的，还需要进行二次配置
    - 除此之外，使用方还需要根据业务需求实现WebViewClient和WebChromeClient，这两个类所需要覆写的方法更多，用来实现标题定制、加载进度条控制、jsbridge交互、url拦截、错误处理（包括http、资源、网络）等很多与业务相关的功能。
- 复杂的前端环境
    - html、css、js相应的升级与更新。高版本的语法无法在低版本的内核上识别和渲染，业务上需要使用到新的特性时，开发不得不面对后向兼容的问题。
- 需要一定的Web知识
    - 使用WebView.loadUrl()来加载一个网页而不了解底层到底发生了什么，那么url发生错误、url中的某些内容加载不出来、url里的内容点击无效、支付宝支付浮层弹不起来、与前端无法沟通等等问题就会接踵而至。要开发好一个功能完整的WebView，需要对Web知识（html、js、css）有一定了解，知道loadUrl，WebView在后台请求这个url以后，服务器做了哪些响应，又下发了哪些资源，这些资源的作用是怎么样的。



### 09.如何处理加载错误
- 对于WebView加载一个网页过程中所产生的错误回调，大致有三种：
- WebViewClient.onReceivedHttpError(webView, webResourceRequest, webResourceResponse)
    - 任何HTTP请求产生的错误都会回调这个方法，包括主页面的html文档请求，iframe、图片等资源请求。在这个回调中，由于混杂了很多请求，不适合用来展示加载错误的页面，而适合做监控报警。当某个URL，或者某个资源收到大量报警时，说明页面或资源可能存在问题，这时候可以让相关运营及时响应修改。
- WebViewClient.onReceivedSslError(webview, sslErrorHandler, sslError)
    - 任何HTTPS请求，遇到SSL错误时都会回调这个方法。比较正确的做法是让用户选择是否信任这个网站，这时候可以弹出信任选择框供用户选择（大部分正规浏览器是这么做的）。但人都是有私心的，何况是遇到自家的网站时。我们可以让一些特定的网站，不管其证书是否存在问题，都让用户信任它。
    ```
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (UrlUtils.isKaolaHost(getUrl())) {
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
    ```
- WebViewClient.onReceivedError(webView, webResourceRequest, webResourceError)
    - 只有在主页面加载出现错误时，才会回调这个方法。这正是展示加载错误页面最合适的方法。然鹅，如果不管三七二十一直接展示错误页面的话，那很有可能会误判，给用户造成经常加载页面失败的错觉。由于不同的WebView实现可能不一样，所以我们首先需要排除几种误判的例子：
        - 加载失败的url跟WebView里的url不是同一个url，排除；
        - errorCode=-1，表明是ERROR_UNKNOWN的错误，为了保证不误判，排除
        - failingUrl=null&errorCode=-12，由于错误的url是空而不是ERROR_BAD_URL，排除
    - 代码如下所示
    ```
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    
        // -12 == EventHandle.ERROR_BAD_URL, a hide return code inside android.net.http package
        if ((failingUrl != null && !failingUrl.equals(view.getUrl()) && !failingUrl.equals(view.getOriginalUrl())) /* not subresource error*/
                || (failingUrl == null && errorCode != -12) /*not bad url*/
                || errorCode == -1) { //当 errorCode = -1 且错误信息为 net::ERR_CACHE_MISS
            return;
        }
    
        if (!TextUtils.isEmpty(failingUrl)) {
            if (failingUrl.equals(view.getUrl())) {
                if (null != mIWebViewClient) {
                    mIWebViewClient.onReceivedError(view);
                }
            }
        }
    }
    ```


### 10.应用被作为第三方浏览器打开
- 微信里的文章页面，可以选择“在浏览器打开”。现在很多应用都内嵌了WebView，那是否可以使自己的应用作为第三方浏览器打开此文章呢？
- 在Manifest文件中，给想要接收跳转的Activity添加<intent-filter>配置：
    ```
    <activity
        android:name=".X5WebViewActivity"
        android:configChanges="orientation|screenSize"
        android:hardwareAccelerated="true"
        android:launchMode="singleTask"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <!--需要添加下面的intent-filter配置-->
        <intent-filter tools:ignore="AppLinkUrlError">
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <!--使用http，则只能打开http开头的网页-->
            <data android:scheme="https" />
        </intent-filter>
    </activity>
    ```
- 然后在 X5WebViewActivity 中获取相关传递数据。具体可以看lib中的X5WebViewActivity类代码。
    ```
    public class X5WebViewActivity extends AppCompatActivity {
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_web_view);
            getIntentData();
            initTitle();
            initWebView();
            webView.loadUrl(mUrl);
           // 处理 作为三方浏览器打开传过来的值
            getDataFromBrowser(getIntent());
        }
    
       /**
         * 使用singleTask启动模式的Activity在系统中只会存在一个实例。
         * 如果这个实例已经存在，intent就会通过onNewIntent传递到这个Activity。
         * 否则新的Activity实例被创建。
         */
        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            getDataFromBrowser(intent);
        }
    
        /**
         * 作为三方浏览器打开传过来的值
         * Scheme: https
         * host: www.jianshu.com
         * path: /p/yc
         * url = scheme + "://" + host + path;
         */
        private void getDataFromBrowser(Intent intent) {
            Uri data = intent.getData();
            if (data != null) {
                try {
                    String scheme = data.getScheme();
                    String host = data.getHost();
                    String path = data.getPath();
                    String text = "Scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path;
                    Log.e("data", text);
                    String url = scheme + "://" + host + path;
                    webView.loadUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    ```
- 一些重点说明
    - 在微信中“通过浏览器”打开自己的应用，然后将自己的应用切到后台。重复上面的操作，会一直创建应用的实例，这样肯定是不好的，为了避免这种情况我们设置启动模式为：launchMode="singleTask"。



### 11.理解WebView独立进程
- WebView实例在Android7.0系统以后，已经可以选择运行在一个独立进程上7；8.0以后默认就是运行在独立的沙盒进程中。
- Android7.0系统以后，WebView相对来说是比较稳定的，无论承载WebView的容器是否在主进程，都不需要担心WebView崩溃导致应用也跟着崩溃。然后7.0以下的系统就没有这么幸运了，特别是低版本的WebView。考虑应用的稳定性，我们可以把7.0以下系统的WebView使用一个独立进程的Activity来包装，这样即使WebView崩溃了，也只是WebView所在的进程发生了崩溃，主进程还是不受影响的。
    - 该方案是考拉的分享，具体内容可以看这篇文章。[如何设计一个优雅健壮的Android WebView](https://blog.klmobile.app/2018/02/27/design-an-elegant-and-powerful-android-webview-part-two/)
    ```
    public static Intent getWebViewIntent(Context context) {
        Intent intent;
        if (isWebInMainProcess()) {
            intent = new Intent(context, MainWebviewActivity.class);
        } else {
            intent = new Intent(context, WebviewActivity.class);
        }
        return intent;
    }
    
    public static boolean isWebInMainProcess() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;
    }
    ```


### 12.使用外部浏览器下载
- 比如，打开一个h5的链接，遇到下载链接，即跳转到外部浏览器打开；其他的则在webView中打开。如何操作呢？代码如下所示：
    ```
    private class MyX5WebViewClient extends X5WebViewClient {
        public MyX5WebViewClient(X5WebView webView, Context context) {
            super(webView, context);
        }
    
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            boolean activityAlive = X5WebUtils.isActivityAlive(WebViewActivity.this);
            if (!activityAlive){
                return false;
            }
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            //如果是返回数据
            if (url.contains(".apk")) {
                //为false调用系统浏览器或第三方浏览器
                Uri issuesUrl = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
                WebViewActivity.this.startActivity(intent);
                return false;
            }else {
                mWebView.loadUrl(url);
                return true;
            }
            //return super.shouldOverrideUrlLoading(webView, url);
        }
    
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            boolean activityAlive = X5WebUtils.isActivityAlive(WebViewActivity.this);
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
                if (url.contains(".apk")) {
                    //为false调用系统浏览器或第三方浏览器
                    Uri issuesUrl = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
                    WebViewActivity.this.startActivity(intent);
                    return false;
                }else {
                    mWebView.loadUrl(url);
                    return true;
                }
            }else {
                return super.shouldOverrideUrlLoading(webView, request);
            }
        }
    }
    ```




