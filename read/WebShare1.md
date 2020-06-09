#### 目录介绍
- 01.loadUrl到底做了什么
- 02.触发加载网页的行为
- 03.webView重定向怎么办
- 04.js交互的一点知识分享
- 05.拦截缓存如何优雅处理
- 06.关于一些问题和优化
- 07.关于一点面向对象思想
- 08.关于后期需要研究目标



### 01.loadUrl到底做了什么
- WebView.loadUrl(url)加载网页做了什么？
    - 加载网页是一个复杂的过程，大概需要执行以下一些操作，例如：
    - 加载网页前，进行webView初始化，setting配置，重制WebView状态(重定向状态，回退栈等)，业务状态(比如进度条，是否显示分享按钮等)
    - 加载网页前，根据不同的域拼接本地客户端的参数，包括基本的机型信息、版本信息、登录信息以及埋点使用的Refer信息等。
    - 开始执行页面加载操作时，可以设置获取H5页面标题，加载进度，开始加载，加载完成，以及加载error等不同的状态。
- 加载页面的过程中回调哪些方法？
    - WebChromeClient.onReceivedTitle(webview, title)，用来设置标题。需要注意的是，在部分Android系统版本中可能会回调多次这个方法，而且有时候回调的title是一个url，客户端可以针对这种情况进行特殊处理，避免在标题栏显示不必要的链接。
    - WebChromeClient.onProgressChanged(webview, progress)，根据这个回调，可以控制进度条的进度（包括显示与隐藏）。一般情况下，想要达到100%的进度需要的时间较长（特别是首次加载），用户长时间等待进度条不消失必定会感到焦虑，影响体验。其实当progress达到80的时候，加载出来的页面已经基本可用了。事实上大部分都会提前隐藏进度条，让用户以为网页加载很快。
    - WebViewClient.shouldInterceptRequest(webview, request)，无论是普通的页面请求(使用GET/POST)，还是页面中的异步请求，或者页面中的资源请求，都会回调这个方法，给开发一次拦截请求的机会。在这个方法中，可以进行静态资源的拦截并使用缓存数据代替，也可以拦截页面，使用自己的网络框架来请求数据。
    - WebViewClient.shouldOverrideUrlLoading(webview, request)，如果遇到了重定向，或者点击了页面中的a标签实现页面跳转，那么会回调这个方法。可以说这个是WebView里面最重要的回调之一。
    - WebViewClient.onReceivedError(webview,handler,error)，加载页面的过程中发生了错误，会回调这个方法。主要是http错误以及ssl错误。在这两个回调中，我们可以进行异常上报，监控异常页面、过期页面，及时反馈给运营或前端修改。在处理ssl错误时，遇到不信任的证书可以进行特殊处理，例如对域名进行判断，针对自己公司的域名“放行”，防止进入丑陋的错误证书页面。
- 加载页面结束回调哪些方法
    - 会回调WebViewClient.onPageFinished(webview,url)。
    - 这时候可以根据回退栈的情况判断是否显示关闭WebView按钮。通过mActivityWeb.canGoBackOrForward(-1)判断是否可以回退。
- 加载h5页面大概流程
    - dns域名解析(将域名解析成ip地址，比较耗时)
    - TCP的三次握手
    - 建立TCP连接后发起HTTP请求
    - 服务器响应HTTP请求
    - 浏览器解析html代码
    - 同时请求html代码中的资源（如js、css、图片等），注意html中资源是串行
    - 最后浏览器对页面进行渲染并呈现给用户



### 02.触发加载网页的行为
- 触发加载网页的行为主要有两种方式：
    - （A）点击页面，触发<link>标签。
    - （B）调用WebView的loadUrl()方法
    - 这两种方法都会发出一条地址，区别就在于这条地址是目的地址还是重定向地址。以访问 http://www.baidu.com 百度的页面来测试一下方法的执行顺序。
- 触发加载网页流程分析
    - 关于分析流程，由于内容较多，故这里暂不展示。具体看：[07.触发加载网页的行为](https://github.com/yangchong211/YCWebView/wiki/6.1-webView%E5%9F%BA%E7%A1%801)
- 得出结论分析说明
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
- shouldOverrideUrlLoading返回值
    - 没有设置WebViewClient通常用浏览器打开；有设置WebViewClient，如果是true则是程序员自己处理，如果是false即用webView加载url
    - 何时调用：WebView的前进、后退、刷新、以及post请求都不会调用shouldOverrideUrlLoading方法。即不是通过webView.loadUrl来加载的 或者 是重定向这个条件
    - 具体看：[45.shouldOverrideUrlLoading返回值](https://github.com/yangchong211/YCWebView/wiki/6.4-webView%E5%9F%BA%E7%A1%804)



### 03.webView重定向怎么办
- webView出现302/303重定向
    - 302重定向又称之为302代表暂时性转移，比如你跳转A页面，但由于网页添加了约束条件，可能让你跳转到B页面，甚至多次重定向。
- 导致的问题
    - 1.A-->B-->C，比如你跳转A页面，最终重定向到C页面。这个时候调用goBack方法，返回到B链接，但是B链接又会跳转到C链接，从而导致没法返回到A链接界面
    - 2.会多次执行onPageStarted和onPageFinished，如果你这里有加载进度条或者loading，那么会导致进度条或者loading执行多次
- 常见的解决方案
    - 手动管理回退栈，遇到重定向时回退两次。
    - 通过HitTestResult判断是否是重定向，从而决定是否自己加载url。具体看：[16.301/302回退栈问题解决方案2](https://github.com/yangchong211/YCWebView/wiki/6.2-webView%E5%9F%BA%E7%A1%802)
    - 通过设置标记位，在onPageStarted和onPageFinished分别标记变量避免重定向。具体看：[17.301/302回退栈问题解决方案3](https://github.com/yangchong211/YCWebView/wiki/6.2-webView%E5%9F%BA%E7%A1%802)
    - 通过用户的touch事件来判断重定向。具体看：[15.301/302回退栈如何处理1](https://github.com/yangchong211/YCWebView/wiki/6.2-webView%E5%9F%BA%E7%A1%802)
- 如何判断重定向
    - 通过getHitTestResult()返回值，如果返回null，或者UNKNOWN_TYPE，则表示为重定向。具体看：[18.如何用代码判断是否重定向](https://github.com/yangchong211/YCWebView/wiki/6.2-webView%E5%9F%BA%E7%A1%802)
    - 在加载一个页面开始的时候会回调onPageStarted方法，在该页面加载完成之后会回调onPageFinished方法。而如果该链接发生了重定向，回调shouldOverrideUrlLoading会在回调onPageFinished之前。
- 终极解决方案如下
    - 需要准备的条件
        - 创建一个栈，主要是用来存取和移除url的操作。这个url包括所有的请求链接
        - 定义一个变量，用于判断页面是否处于正在加载中。
        - 定义一个变量，用于记录重定向前的链接url
        - 定一个重定向时间间隔，主要为了避免刷新造成循环重定向
    - 具体怎么操作呢
        - 在执行onPageStarted时，先移除栈中上一个url，然后将url加载到栈中。
        - 当出现错误重定向的时候，如果和上一次重定向的时间间隔大于3秒，则reload页面。
        - 在回退操作的时候，判断如果可以回退，则从栈中获取最后停留的url，然后loadUrl。即可解决回退问题。
    - 具体方法思路
        - 可以看：[20.重定向终极优雅解决方案](https://github.com/yangchong211/YCWebView/wiki/6.2-webView%E5%9F%BA%E7%A1%802)
        - 具体代码看：[X5WebViewClient](https://github.com/yangchong211/YCWebView/blob/master/WebViewLib/src/main/java/com/ycbjie/webviewlib/base/X5WebViewClient.java)



### 04.js交互的一点知识分享
- js交互介绍
    - Java调用js方法有两种：
        - WebView.loadUrl("javascript:" + javascript);
        - WebView.evaluateJavascript(javascript, callbacck);
    - js调用Java的方法有三种，分别是：
        - JavascriptInterface
        - WebViewClient.shouldOverrideUrlLoading()
        - WebChromeClient.onJsPrompt()
- js调用java方法比较和区别分析
    - 1.通过 addJavascriptInterface 方法进行添加对象映射。js最终通过对象调用原生方法
    - 2.shouldOverrideUrlLoading拦截操作，获取scheme匹配，与网页约定好一个协议，如果匹配，执行相应操作
    - 3.利用WebChromeClient回调接口onJsPrompt拦截操作。
        - onJsAlert 是不能返回值的，而 onJsConfirm 只能够返回确定或者取消两个值，只有 onJsPrompt 方法是可以返回字符串类型的值，操作最全面方便。
    - 详细分析可以看：[03.Js调用Android](https://github.com/yangchong211/YCWebView/wiki/6.1-webView%E5%9F%BA%E7%A1%801)
- js调用java原生方法可能存在的问题？
    - 提出问题
        - 1.原生方法是否可以执行耗时操作，如果有会阻塞通信吗？[4.4.8 prompt的一个坑导致js挂掉](https://github.com/yangchong211/YCWebView/wiki/4.4-%E9%97%AE%E9%A2%98%E6%B1%87%E6%80%BB%E4%BB%8B%E7%BB%8D4)
        - 2.多线程中调用多个原生方法，如何保证原生方法每一个都会被执行到？
        - 3.js会阻塞等待当前原生函数（耗时操作的那个）执行完毕再往下走，所以js调用java方法里面最好也不要做耗时操作
    - 解决方案
        - 1.在js调用​window.alert​，​window.confirm​，​window.prompt​时，​会调用WebChromeClient​对应方法，可以此为入口，作为消息传递通道，考虑到开发习惯，一般不会选择alert跟confirm，​通常会选prompt作为入口，在App中就是onJsPrompt作为jsbridge的调用入口。由于onJsPrompt是在UI线程执行，所以尽量不要做耗时操作，可以借助Handler灵活处理。
        - 2.利用Handler封装一下，让每个任务自己处理，耗时的话就开线程自己处理。具体可以看：[WvWebView](https://github.com/yangchong211/YCWebView/blob/master/WebViewLib/src/main/java/com/ycbjie/webviewlib/wv/WvWebView.java)
- java调用js的时机
    - onPageFinished()或者onPageStarted()方法中注入js代码吗？
        - js交互，大部分都会认为js在WebViewClient.onPageFinished()方法中注入最合适，此时dom树已经构建完成，页面已经完全展现出来。但如果做过页面加载速度的测试，会发现WebViewClient.onPageFinished()方法通常需要等待很久才会回调（首次加载通常超过3s），这是因为WebView需要加载完一个网页里主文档和所有的资源才会回调这个方法。
        - 能不能在WebViewClient.onPageStarted()中注入呢？答案是不确定。经过测试，有些机型可以，有些机型不行。在WebViewClient.onPageStarted()中注入还有一个致命的问题——这个方法可能会回调多次，会造成js代码的多次注入。
        - 从7.0开始，WebView加载js方式发生了一些小改变，**官方建议把js注入的时机放在页面开始加载之后**。
    - 可以在onProgressChanged中方法中注入js代码
        - 页面的进度加载到80%的时候，实际上dom树已经渲染得差不多了，表明WebView已经解析了<html>标签，这时候注入一般是成功的。
        - 提到的多次注入控制，使用了boolean值变量控制；重新加载一个URL之前，需要重置boolean值变量，让重新加载后的页面再次注入js




### 05.拦截缓存如何优雅处理
- WebView为何加载慢
    - webView是怎么加载网页的呢？
        - webView初始化->DOM下载→DOM解析→CSS请求+下载→CSS解析→渲染→绘制→合成
    - 渲染速度慢
        - 前端H5页面渲染的速度取决于 两个方面：
            - Js 解析效率。Js 本身的解析过程复杂、解析速度不快 & 前端页面涉及较多 JS 代码文件，所以叠加起来会导致 Js 解析效率非常低
            - 手机硬件设备的性能。由于Android机型碎片化，这导致手机硬件设备的性能不可控，而大多数的Android手机硬件设备无法达到很好很好的硬件性能
    - 页面资源加载缓慢
        - H5 页面从服务器获得，并存储在 Android手机内存里：
            - H5页面一般会比较多
            - 每加载一个 H5页面，都会产生较多网络请求：
                - HTML 主 URL 自身的请求；
                - HTML外部引用的JS、CSS、字体文件，图片也是一个独立的 HTTP 请求
            - 每一个请求都串行的，这么多请求串起来，这导致 H5页面资源加载缓慢
- 解决WebView加载慢
    - 前端H5的缓存机制（WebView 自带）
    - 资源拦截缓存
    - 资源拦截替换
- webView浏览器缓存机制
    - 这些技术都是协议层所定义的，在Android的webView当中我们可以通过配置决定是否采纳这几个协议的头部属性
    ```java
    // LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
    // LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
    // LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
    // LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
    ws.setCacheMode(WebSettings.LOAD_DEFAULT);
    ```
    - 一般设置为默认的缓存模式就可以了。关于缓存的配置, 主要还是靠web前端和后台设置。关于[浏览器缓存机制](https://github.com/yangchong211/YCWebView/wiki/7.1-%E7%BC%93%E5%AD%98%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86)
- 具体会缓存那些内容
    - 当我们加载Html时候，会在我们data/应用package下生成database与cache两个文件夹。具体可以看：[05.具体缓存那些内容](https://github.com/yangchong211/YCWebView/blob/master/read/WebCache1.md)
    - 网页数据缓存（存储打开过的页面及资源）：指加载一个网页时的html、JS、CSS，图片等页面或者资源数据。通过配置HTTP响应头影响浏览器的行为才能间接地影响到这些缓存数据，该案例通过配置OkHttp可以做到
    - 数据缓存：数据缓存分为AppCache和DOM Storage两种，这些缓存资源是由开发者的直接行为而产生，所有的缓存数据都由开发者直接完全地掌控。这块可以直接交给OkHttp去做，会把数据下载到本地磁盘缓存
- 自身构建缓存方案
    - 拦截处理
        - 在shouldInterceptRequest方法中拦截处理
        - 步骤1:判断拦截资源的条件，即判断url里的图片资源的文件名
        - 步骤2:创建一个输入流，这里可以先从内存中拿，拿不到从磁盘中拿，再拿不到就从网络获取数据
        - 步骤3:打开需要替换的资源(存放在指定文件夹里)，或者从lru中取出缓存的数据
        - 步骤4:替换资源
    - 在哪里进行拦截
        - webView在加载网页的时候，用户能够通过系统提供的API干预各个中间过程。我们要拦截的就是网页资源请求的环节。这个过程，WebViewClient当中提供了以下两个入口：
            ```java
            // android5.0以上的版本加入
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
              return super.shouldInterceptRequest(webView, webResourceRequest);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
              return super.shouldInterceptRequest(webView, s);
            }
            ```
        - 替换资源操作
            - 只要在这两个入口构造正确的WebResourceResponse对象，就可以替换默认的请求为我们提供的资源
            - 因此，在每次请求资源的时候根据请求的URL/WebResourceRequest判断是否存在本地的缓存，并在缓存存在的情况下将缓存的输入流返回
- 关于缓存的梳理
    - okHttp缓存原理大概是什么？
        - CacheInterceptor，在建立连接之前，我们检查响应是否已经被缓存、缓存是否可用，如果是则直接返回缓存的数据，否则就进行后面的流程，并在返回之前，把网络的数据写入缓存。
        - 主要涉及 HTTP 协议缓存细节的实现，而具体的缓存逻辑 OkHttp 内置封装了一个Cache类，它利用DiskLruCache，用磁盘上的有限大小空间进行缓存，按照 LRU 算法进行缓存淘汰（主要用到LinkedHashMap），这里也不再展开。
        - 关于CacheInterceptor缓存拦截的代码分析，具体可以看：[OkHttp缓存介绍](https://github.com/yangchong211/YCWebView/blob/master/read/WebCache5.md)
    - okHttp缓存优势
        - 1.三级缓存，网络缓存(http)，磁盘缓存(file)，内存缓存(Lru)
        - 2.使用okio流，数据进行了分块处理(Segment)，提供io流超时处理，对数据的读写都进行了封装和交给Buffer管理。具体看这篇文章：[OkHttp中OKio分析](https://github.com/yangchong211/YCWebView/blob/master/read/WebCache6.md)
    - 如何查看缓存
        - 1.在data/data文件下找到自己设置的缓存文件，比如设置的是YcCacheWebView。在该目录下，直接点开文件，可以看到.0后缀名文件是缓存内容，.1后缀名文件是缓存file。具体可以点进去看看
        - 2.调用response.cacheResponse()方法，如果缓存得到的内容不为空，则表示缓存成功呢
        - 3.使用流量获取的方式，比较没有缓存的情况和有缓存的情况，它们消耗的网络流量对比


### 06.关于一些问题和优化
- 影响页面加载的一些因素有那些？
    - 1.加载网页中，如果图片很多，而这些图片的请求又是一个个独立并且串行的请求。那么可能会导致加载页面比较缓慢……
    - 2.app原生和webView中请求，都会涉及到https的网络请求，那么在请求前会有域名dns的解析，这个也会有大约200毫秒的解析时间(主要耗费时间dns，connection，服务器处理等)……
    - 3.webView加载html网页时，有些js一直在执行比如动画之类的东西，此刻webView挂在了后台这些资源是不会被释放用户也无法感知。导致耗费资源……
    - 4.关于加载loading或者加载进度条，不一定要放到onPageStarted开始执行即显示出来，因为webView从创建到这个方法会有一个时间……
    - 5.webView默认开启密码保存功能，如果网页涉及到用户登陆，密码会被明文保到 /data/data/com.package.name/databases/webview.db 中，这样就有被盗取密码的危险……
    - 6.h5页面被拦截或者注入广告，重定向，或者DNS劫持。一般跟连接的wifi有关系（http劫持），也可能跟运营商有关系(dns劫持)
- 具体可以操作的优化分析
    - 1.加载webView中的资源时，针对图片，等页面finish后再发起图片加载(也就是执行onPageFinished设置加载图片)。具体看[5.0.2图片加载次序优化](https://github.com/yangchong211/YCWebView/wiki/5.1-webView%E4%BC%98%E5%8C%961)
    - 2.[DNS域名解析](https://github.com/yangchong211/YCBlogs/blob/master/net/%E7%BD%91%E7%BB%9C%E5%9F%BA%E7%A1%80/04.%E5%9F%9F%E5%90%8D%E8%A7%A3%E6%9E%90DNS.md)采用和客户端API相同的域名， DNS会在系统级别进行缓存，对于WebView的地址，如果使用的域名与native的API相同，则可以直接使用缓存的DNS而不用再发起请求图片。具体看[5.0.7 DNS采用和客户端API相同的域名](https://github.com/yangchong211/YCWebView/wiki/5.1-webView%E4%BC%98%E5%8C%961)
    - 3.在后台的时候，会调用onStop方法，即此时关闭js交互，回到前台调用onResume再开启js交互。具体看[5.0.9 后台无法释放js导致发热耗电](https://github.com/yangchong211/YCWebView/wiki/5.1-webView%E4%BC%98%E5%8C%961)
    - 4.提前显示进度条不是提升性能，但是对用户体验来说也是很重要的一点 ，WebView.loadUrl("url") 不会立马就回调onPageStarted方法，因为在这一时间段，WebView 有可能在初始化内核，也有可能在与服务器建立连接，这个时间段容易出现白屏
    - 5.需要通过 WebSettings.setSavePassword(false) 关闭密码保存功能。
    - 6.一般可以处理：1使用https代替http；2.添加白名单(比如添加自己网站的host，其他不给访问)；3.对页面md5校验(不太好)。设置白名单参考：[5.0.8 如何设置白名单操作](https://github.com/yangchong211/YCWebView/wiki/5.1-webView%E4%BC%98%E5%8C%961)
- 还有一些其他的优化小细节
    - a.WebView处理404、500逻辑，在WebChromeClient子类中可以重写他的onReceivedTitle()方法监听标题，还有在WebChromeClient子类中onReceivedHttpError可以监听statusCode。具体操作看[5.1.5 WebView处理404、500逻辑](https://github.com/yangchong211/YCWebView/wiki/5.1-webView%E4%BC%98%E5%8C%961)
    - b.如果不显示图片，开发的时候可能使用的是https的链接, 但是链接中的图片可能是http的，需要开启设置。具体看：[4.1.4 webView加载网页不显示图片](https://github.com/yangchong211/YCWebView/wiki/4.1-%E9%97%AE%E9%A2%98%E6%B1%87%E6%80%BB%E4%BB%8B%E7%BB%8D1)
    - c.evaluateJavascript(String var1, ValueCallback<String> var2)中url长度有限制，在19以上超过2097152个字符失效，这个地方可以加个判断。不过一般很难碰到……具体可以参考：[4.3.1 Android与js传递数据大小有限制](https://github.com/yangchong211/YCWebView/wiki/4.3-%E9%97%AE%E9%A2%98%E6%B1%87%E6%80%BB%E4%BB%8B%E7%BB%8D3)
    - d.在web页面android软键盘覆盖问题，常见的有android:windowSoftInputMode的值adjustPan或者adjustResize即可，如果webView是全屏模式则仍然会出现问题。具体看：[4.6.1 在web页面android软键盘覆盖问题](https://github.com/yangchong211/YCWebView/wiki/4.4-%E9%97%AE%E9%A2%98%E6%B1%87%E6%80%BB%E4%BB%8B%E7%BB%8D4)
    - e.关于WebView隐藏H5页面中的某个标签视图，大概操作就是在页面加载完成，通过getElementsByClassName找到h5中标签name，然后手动写function方法隐藏标签。但加载时机很关键，不过会造成闪屏和多次加载。具体看：[4.6.6 WebView如何隐藏H5的部分内容问题](https://github.com/yangchong211/YCWebView/wiki/4.5-%E9%97%AE%E9%A2%98%E6%B1%87%E6%80%BB%E4%BB%8B%E7%BB%8D5)
    - f.页面重定向，会导致onPageStarted多次执行，那么这个时候如何避免加载进度条出现执行多次，或者跳动的问题。具体可见：[09.web进度条避免多次加载](https://github.com/yangchong211/YCWebView/wiki/6.1-webView%E5%9F%BA%E7%A1%801)
    - g.建议开启Google安全浏览服务，用户访问不安全网页会提示安全问题；webView使用上的建议设置布局高度和宽度设置为 match_parent；具体可见[48.开启Google安全浏览服务](https://github.com/yangchong211/YCWebView/wiki/6.4-webView%E5%9F%BA%E7%A1%804)



### 07.关于一点面向对象的思想
- 针对webView视频播放演变
    - 1.最刚开始把视频全屏show和hide的逻辑都放到X5WebChromeClient中处理，相当于这个类中逻辑比较多
    - 2.后期把视频全屏播放逻辑都抽到了VideoWebChromeClient类中处理，这样只需要继承该类即可。这个类独立，拿来即用。
    - 3.后期演变，一个视频全屏播放接口 + 接口实现类 + VideoChromeClient，接口主要能够解耦
- 关于webView拦截缓存处理
    - 1.最开始把拦截的逻辑都放到X5WebViewClient类中的shouldInterceptRequest方法中。后期演变抽取+接口
    - 2.代码结构大概是：拦截缓存接口 + 接口实现类 + 接口委派类
    - 3.优点：委派类和实现类解耦；便于增加过滤功能(比如用了https+dns优化就不用拦截缓存)；
    ```
    //1.创建委托对象
    WebViewCacheDelegate webViewCacheDelegate = WebViewCacheDelegate.getInstance();
    //2.通过委托对象调用方法
    WebResourceResponse webResourceResponse = webViewCacheDelegate.interceptRequest(url);
    ```
    - 接口(WebViewRequestClient)和实现(WebViewCacheWrapper)相分离，封装不稳定的实现，暴露稳定的接口。上游系统面向接口而非实现编程，不依赖不稳定的实现细节，这样当实现发生变化的时候，上游系统的代码基本上不需要做改动，以此来降低代码间的耦合性，提高代码的扩展性。
- 关于shouldOverrideUrlLoading处理多类型
    - 比如：封装库中需要处理打电话，发短信，发邮件，地图定位，图片，超链接等拦截逻辑。还有关于在shouldOverrideUrlLoading拦截做js交互的逻辑……可以说最开始这个类代码很臃肿！
    - 最刚开始是把处理的逻辑都放到了WebViewClient中的shouldOverrideUrlLoading方法中处理。不过发现这个类代码越来越多……
    - 后期演变，针对电话短信等将处理逻辑抽取到WebSchemeIntent类中，针对图片处理逻辑抽取到SaveImageProcessor类中。具体看[WebSchemeIntent](https://github.com/yangchong211/YCWebView/blob/master/WebViewLib/src/main/java/com/ycbjie/webviewlib/helper/WebSchemeIntent.java)
    - 后期演变，将js操作单独抽取出来写到JsX5WebViewClient，这样极大提高了阅读性，类的结构也是更加清晰呢
    - 这样做，相当于保证了类的单一性职责，即类尽量保证内部处理的功能尽可能单一，而不是错综复杂……
- 其他的一些感受
    - 合理使用注解限定符，比如InterWebListener接口中的showErrorView方法，异常的类型可能会出现多种，设置type如果是1，2，3等容易让人看不懂。这时候限定符就发挥作用呢！
    - 项目库中如果有和业务代码交接的地方，可以通过接口暴露出来，这样避免了功能和业务耦合，增强了拓展性
    - 对于某些不想让别人修改或者继承的类，可以使用finial修饰。比如有赞webView的sdk中，比如ChromeClientWrapper继承了WebChromeClient，为了避免内部不被修改和反射。
    - 有抽象意识、封装意识、接口意识。接口的定义只表明做什么，而不是怎么做。在设计接口的时候，这样的接口设计是否足够通用，是否能够做到在替换具体的接口实现的时候，不需要任何接口定义的改动。


### 08.关于后期需要研究的目标
- 目标
    - web页面特别消耗流量，每次打开页面都会请求网络，建议对流量的消耗进行优化……除了对lib库中对拦截做OkHttp缓存，还有什么其他方案
- web页面涉及流量的几个方面
    - 普通https请求，一般过程是服务端(对象)-->网络中(二进制流)-->客户端(对象)，文本内容会做传输压缩
    - 网络图片下载，图片下载消耗的流量较多
    - h5页面展示，由于h5页面是交由前端处理显示，客户端开发关注的少些，而此处消耗了大量的流量
- 如何查看web页面消耗流量
    - 使用TrafficStats即可查看流量的消耗



### 09.关于其他说明
- 由于分享文章有限，很多问题和技术细节点，都附带有链接。demo的地址：https://github.com/yangchong211/YCWebView



