#### 基础使用目录介绍
- 01.常用的基础使用介绍
- 02.Android调用Js方式
- 03.Js调用Android方式
- 04.清除缓存和缓存分析
- 05.为什么WebView难搞
- 06.如何处理加载错误
- 07.触发加载网页的行为
- 09.web进度条避免多次加载
- 10.多次获取web标题title




### 01.常用的基础使用介绍
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
    - 需要注意的是 prompt 里面的内容是通过 message 传递过来的，并不是第二个参数的 url，返回值是通过 JsPromptResult 对象传递。
    - 为什么要拦截 onJsPrompt 方法，而不是拦截其他的两个方法，这个从某种意义上来说都是可行的，但是如果需要返回值给 web 端的话就不行了。
    - 因为 onJsAlert 是不能返回值的，而 onJsConfirm 只能够返回确定或者取消两个值，只有 onJsPrompt 方法是可以返回字符串类型的值，操作最全面方便。
- 以上三种方案的总结和对比
    - 以上三种方案都是可行的，在这里总结一下
    - 第一种方式：是现在目前最普遍的用法，方便简洁，但是唯一的不足是在 4.2 系统以下存在漏洞问题；
    - 第二种方式：通过拦截 url 并解析，如果是已经约定好的协议则进行相应规定好的操作，缺点就是协议的约束需要记录一个规范的文档，而且从 Native 层往 Web 层传递值比较繁琐，优点就是不会存在漏洞。
    - 第三种方式：和第二种方式的思想其实是类似的，只是拦截的方法变了，这里拦截了 JS 中的三种对话框方法，而这三种对话框方法的区别就在于返回值问题，alert 对话框没有返回值，confirm 的对话框方法只有两种状态的返回值，prompt 对话框方法可以返回任意类型的返回值，缺点就是协议的制定比较麻烦，需要记录详细的文档，但是不会存在第二种方法的漏洞问题。



### 04.清除缓存和缓存分析
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
- WebView缓存是在什么地方？
    - 在项目中如果使用到 WebView 控件,当加载 html 页面时,会在/data/data/包名目录下生成 database 与 cache 两个文件夹。
    - 请求的 url 记录是保存在 WebViewCache.db,而 url 的内容是保存在 WebViewCache 文件夹下。
- 多种缓存策略分析
    - LOAD_CACHE_ONLY：不使用网络，只读本地缓存。
    - LOAD_NORMAL：在 API Level 17 中已经被废弃，而在API Level 11 开始，策略如 LOAD_DEFALT。
    - LOAD_NO_CACHE：不使用缓存，只从网络获取数据。
    - LOAD_CACHE_ELSE_NETWORK：只要本地有缓存，就从缓存中读取数据。
    - LOAD_DEFAULT：根据 Http 协议，决定是否从网络获取数据。
- 那么缓存的功能如何实现的呢？



### 05.为什么WebView难搞
- 繁杂的WebView配置
    - WebView在初始化的时候就提供了默认配置WebSettings，但是很多默认配置是不能够满足业务需求的，还需要进行二次配置
    - 除此之外，使用方还需要根据业务需求实现WebViewClient和WebChromeClient，这两个类所需要覆写的方法更多，用来实现标题定制、加载进度条控制、jsbridge交互、url拦截、错误处理（包括http、资源、网络）等很多与业务相关的功能。
- 复杂的前端环境
    - html、css、js相应的升级与更新。高版本的语法无法在低版本的内核上识别和渲染，业务上需要使用到新的特性时，开发不得不面对后向兼容的问题。
- 需要一定的Web知识
    - 使用WebView.loadUrl()来加载一个网页而不了解底层到底发生了什么，那么url发生错误、url中的某些内容加载不出来、url里的内容点击无效、支付宝支付浮层弹不起来、与前端无法沟通等等问题就会接踵而至。要开发好一个功能完整的WebView，需要对Web知识（html、js、css）有一定了解，知道loadUrl，WebView在后台请求这个url以后，服务器做了哪些响应，又下发了哪些资源，这些资源的作用是怎么样的。



### 06.如何处理加载错误
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




### 07.触发加载网页的行为
- 触发加载网页的行为主要有两种方式：
    - （A）点击页面，触发<link>标签。
    - （B）调用WebView的loadUrl()方法
    - 这两种方法都会发出一条地址，区别就在于这条地址是目的地址还是重定向地址。以访问http://www.baidu.com百度的页面来测试一下方法的执行顺序。


#### 7.1 触发加载网页流程分析
- 在代码中通过loadUrl加载百度的首页，此时的行为属于（B）方式。
    - 可以发现大概的执行顺序是：onPageStarted ——> shouldOverrideUrlLoading ——> onPageFinished
    - 那么为什么会执行多次呢，思考一下？具体可以看一下7.2得出的结论分析。
    ```
    //访问json.cn网页
    2020-06-08 21:03:11.648 28249-28249/com.ycbjie.ycwebview I/X5LogUtils: -------onPageStarted-------https://www.json.cn/
    2020-06-08 21:03:11.656 28249-28249/com.ycbjie.ycwebview I/X5LogUtils: -------onReceivedTitle-------JSON在线解析及格式化验证 - JSON.cn
    2020-06-08 21:03:11.731 28249-28249/com.ycbjie.ycwebview I/X5LogUtils: -------onPageFinished-------https://www.json.cn/

    //访问百度网页
    2020-06-08 21:04:06.969 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -----shouldOverrideUrlLoading------loadUrl-------https://m.baidu.com/?from=844b&vit=fps
    2020-06-08 21:04:07.616 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onPageStarted-------https://m.baidu.com/?from=844b&vit=fps
    2020-06-08 21:04:07.618 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onReceivedTitle-------百度一下
    2020-06-08 21:04:08.133 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -----shouldOverrideUrlLoading------loadUrl-------http://m.baidu.com/?cip=124.127.44.210&baiduid=E6C897CAF02A1CDBB538FA62922E13E6&from=844b&vit=fps?from=844b&vit=fps&index=&ssid=0&bd_page_type=1&logid=11784780363695702619&pu=sz%401321_480&t_noscript=jump
    2020-06-08 21:04:08.139 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -----shouldOverrideUrlLoading------loadUrl-------https://m.baidu.com/?cip=124.127.44.210&baiduid=E6C897CAF02A1CDBB538FA62922E13E6&from=844b&vit=fps?from=844b&vit=fps&index=&ssid=0&bd_page_type=1&logid=11784780363695702619&pu=sz%401321_480&t_noscript=jump
    2020-06-08 21:04:08.144 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onPageFinished-------https://m.baidu.com/?from=844b&vit=fps
    2020-06-08 21:04:08.248 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onPageStarted-------https://m.baidu.com/?cip=124.127.44.210&baiduid=E6C897CAF02A1CDBB538FA62922E13E6&from=844b&vit=fps?from=844b&vit=fps&index=&ssid=0&bd_page_type=1&logid=11784780363695702619&pu=sz%401321_480&t_noscript=jump
    2020-06-08 21:04:08.257 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onReceivedTitle-------百度一下,你就知道
    2020-06-08 21:04:08.365 28978-28978/com.ycbjie.ycwebview I/X5LogUtils: -------onPageFinished-------https://m.baidu.com/?cip=124.127.44
    ```
- 在首页，点击一下“hao123”,跳转到www.hao123.com的主页上来，此时的行为属于（A）方式。
    - 可以发现大概的执行顺序是：shouldOverrideUrlLoading ——> onPageStarted ——> onPageFinished
    ```
    X5LogUtils: -------shouldOverrideUrlLoading-------http://m.hao123.com/?ssid=0&from=844b&bd_page_type=1&uid=0&pu=sz%401321_1002%2Cta%40utouch_2_9.0_2_6.2&idx=30000&itj=39
    X5LogUtils: -------onPageStarted-------http://m.hao123.com/?ssid=0&from=844b&bd_page_type=1&uid=0&pu=sz%401321_1002%2Cta%40utouch_2_9.0_2_6.2&idx=30000&itj=39
    X5LogUtils: -------onReceivedTitle-------hao123导航-上网从这里开始
    X5LogUtils: -------onPageFinished-------http://m.hao123.com/?ssid=0&from=844b&bd_page_type=1&uid=0&pu=sz%401321_1002%2Cta%40utouch_2_9.0_2_6.2&idx=30000&itj=39
    ```
- 然后在hao123页面，点击优酷网进行跳转，此时的行为属于（A）方式。
    ```
    X5LogUtils: -------shouldOverrideUrlLoading-------http://m.hao123.com/j.php?z=2&page=index_cxv3&pos=cydhwt_n2&category=ty&title=%E4%BC%98%E9%85%B7%E7%BD%91&qt=tz&url=http%3A%2F%2Fwww.youku.com%2F&key=58193753e7a868d9a013056c6c4cd77b
    X5LogUtils: -------onPageStarted-------http://m.hao123.com/j.php?z=2&page=index_cxv3&pos=cydhwt_n2&category=ty&title=%E4%BC%98%E9%85%B7%E7%BD%91&qt=tz&url=http%3A%2F%2Fwww.youku.com%2F&key=58193753e7a868d9a013056c6c4cd77b
    X5LogUtils: -------shouldOverrideUrlLoading-------http://www.youku.com/
    X5LogUtils: -------onPageFinished-------http://m.hao123.com/j.php?z=2&page=index_cxv3&pos=cydhwt_n2&category=ty&title=%E4%BC%98%E9%85%B7%E7%BD%91&qt=tz&url=http%3A%2F%2Fwww.youku.com%2F&key=58193753e7a868d9a013056c6c4cd77b
    X5LogUtils: -------onPageStarted-------http://www.youku.com/
    X5LogUtils: -------shouldOverrideUrlLoading-------https://www.youku.com/
    X5LogUtils: -------onPageFinished-------http://www.youku.com/
    X5LogUtils: -------onPageStarted-------https://www.youku.com/
    X5LogUtils: -------onReceivedTitle-------优酷视频-首页
    X5LogUtils: -------onPageFinished-------https://www.youku.com/
    ```
- 然后从优酷页面回退到hao123页面，看看又回执行哪些方法。
    ```
    X5LogUtils: -------onPageStarted-------http://m.hao123.com/?ssid=0&from=844b&bd_page_type=1&uid=0&pu=sz%401321_1002%2Cta%40utouch_2_9.0_2_6.2&idx=30000&itj=39
    X5LogUtils: -------onReceivedTitle-------hao123导航-上网从这里开始
    X5LogUtils: -------onPageFinished-------http://m.hao123.com/?ssid=0&from=844b&bd_page_type=1&uid=0&pu=sz%401321_1002%2Cta%40utouch_2_9.0_2_6.2&idx=30000&itj=39
    ```
- 然后从hao123页面回退到百度首页，看看又回执行哪些方法。
    ```
    X5LogUtils: -------onPageStarted-------https://m.baidu.com/?cip=117.101.19.67&baiduid=C6FCEED198C994E0D653C094F2708C32&from=844b&vit=fps?from=844b&vit=fps&index=&ssid=0&bd_page_type=1&logid=12175252243175665635&pu=sz%401321_480&t_noscript=jump
    X5LogUtils: -------onReceivedTitle-------百度一下,你就知道
    X5LogUtils: -------onPageFinished-------https://m.baidu.com/?cip=117.101.19.67&baiduid=C6FCEED198C994E0D653C094F2708C32&from=844b&vit=fps?from=844b&vit=fps&index=&ssid=0&bd_page_type=1&logid=12175252243175665635&pu=sz%401321_480&t_noscript=jump
    ```

#### 7.2 得出结论分析说明
- 在(A)行为方式下（用户点击链接的回调）：
    - 1.如果是目的地址，那么方法的执行顺序是：
        - shouldOverrideUrlLoading() -> onPageStarted()-> onPageFinished()
        - shouldOverrideUrlLoading()由于它要提供给APP选择加载网页环境的机会，所以只要是网页上地址请求，都会获取到。
    - 2.如果是重定向地址，在跳转到目的地址之前会进行不断的地址定位，每一次地址定位都会由以下执行顺序体现出来：
        - onPageStarted()->shouldOverrideUrlLoading()->onPageFinished()
        - 暂且设定这种执行顺序叫：fixed position
        - 那么一个正常的重定向地址，方法的执行顺序就是：
        - shouldOverrideUrlLoading() -> fixed position -> … -> fixed position -> onPageStarted() -> onPageFinished()
        - 举个例子：有重定向(A->B->C)，那么
        ```
        shouldOverrideUrlLoading(A) -> onPageStarted(A) -> 
        onPageStarted(B) -> shouldOverrideUrlLoading(B) -> 
        onPageStarted(C) -> shouldOverrideUrlLoading(C) -> onPageFinished(C)
        ```
- 在（B）行为下：
    - 1.如果是目的地址，那么方法的执行顺序是：
        - onPageStarted()-> onPageFinished()
        - loadUrl()加载地址时，一般不会触发shouldOverrideUrlLoading()，一旦触发了，就说明这是一个重定向地址。
    - 2.如果是重定向地址，方法的执行顺序就是：
        - fixed position -> … -> fixed position -> onPageStarted() -> onPageFinished()



#### 7.3 重定向导致的问题
- 如果我们想要自定义进度条，就要考虑如何避免重定向行为造成的多次加载这种情况
- 如果我想想要设置onReceivedTitle中获取到的title标题，就要考虑如何避免重定向行为造成多次加载的这种问题


### 08.重定向优雅处理


### 09.web进度条避免多次加载
- 网页需要在我们加载完成后需要去关闭自定义进度条
    - 如果是一个没有重定向的网页加载这样是没有问题的
    - 如果你的页面重定向了并且还有可能是多次的，我们的在onPageStarted和onPageFinished会回调多次，就会导致进度条出现重复加载
- 第一种解决方案
    - 如果我们想要自定义进度条，就要考虑如何避免重定向行为造成的多次加载这种情况，（也许你的场景不需要这种保护）。我的解决方式是这样的：设置一个Boolean全局变量flag，
    - 1、在onPageStarted()中设置为true，若加载样式没有开启，就开启进度条等加载样式，
    - 2、在onPageFinished()中检测，如果为true，就说明已经是目的地址了，可以关闭加载样式了，如果是false，就不做处理，继续等待，
    - 3、在shouldOverrideUrlLoading()中，设置为false，若加载样式没有开启，就开启进度条等加载样式
- 第二种解决方案
    ```
    Webview.setWebViewClient(new WebViewClient() {
        int running = 0;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            running++;
            return true;
        }
    
        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            running = Math.max(running, 1);
        }
    
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (--running==0) {
                //做操作，隐藏加载进度条或者加载loading
            }
        }
    });
    ```
- 第三种解决方案
    - 在自定义进度条中，定义一个记录状态(已经开始状态，已经结束状态，不能继续开始状态)的临时变量。
    - 1.在设置进度条进度动画的时候，标记为已经开始状态；
    - 2.当进度条达到95以上时，表明页面几乎加载完成，这时候标记为已经结束状态；
    - 3.当进度条动画监听结束后，将状态标记为不能继续开始状态；
    - 这个时候，即使页面有多次重定向，执行多次onPageStarted->onPageFinished方法，也不会出现一次进度条没跑完又出现第二次进度条。具体代码看lib中的WebProgress代码！



### 10.多次获取web标题title
- 出现的问题
- 网上的部分解决思路
    - 网上能查的大部分方法都是在WebChromeClient的onReceivedTitle(WebView view, String title)中拿到title。但是这个方法在网页回退时是无法拿到正确的上一级标题的，网上的处理方法是自己维护一个List去缓存标题，在执行完webView.goBack()后，移除List的最后一条，再将新的最后一条设置给标题栏。
    - 这个方法当然是可行的，但是自己缓存时缓存时机和移除时机都不好确定，onReceivedTitle方法在一个页面打开时并不是仅调用一次，而是多次调用，前面拿到的title都为空。
- 采用原生的WebBackForwardList获取，不过下面这种仍然也会出现问题。
    ```
    webView.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            getWebTitle();
        }
    });
    
    private void getWebTitle(){
        WebBackForwardList forwardList = webView.copyBackForwardList();
        WebHistoryItem item = forwardList.getCurrentItem();
        if (item != null) {
            setActionBarTitle(item.getTitle());
        }
    }
    
    private void onWebViewGoBack(){
        webView.goBack();
        getWebTitle();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack()) {
            onWebViewGoBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    ```









