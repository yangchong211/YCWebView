#### 使用目录介绍
- 01.WebView为何加载慢
- 02.解决WebView加载慢
- 03.浏览器缓存机制
- 04.具体缓存那些内容
- 05.预加载数据



### 01.WebView为何加载慢
- webView是怎么加载网页的呢？
    - webView初始化->DOM下载→DOM解析→CSS请求+下载→CSS解析→渲染→绘制→合成
- 渲染速度慢
    - 前端H5页面渲染的速度取决于 两个方面：
        - Js 解析效率。Js 本身的解析过程复杂、解析速度不快 & 前端页面涉及较多 JS 代码文件，所以叠加起来会导致 Js 解析效率非常低
        - 手机硬件设备的性能。由于Android机型碎片化，这导致手机硬件设备的性能不可控，而大多数的Android手机硬件设备无法达到很好很好的硬件性能
    - 总结：上述两个原因 导致 H5页面的渲染速度慢。
- 页面资源加载缓慢
    - H5 页面从服务器获得，并存储在 Android手机内存里：
        - H5页面一般会比较多
        - 每加载一个 H5页面，都会产生较多网络请求：
            - HTML 主 URL 自身的请求；
            - HTML外部引用的JS、CSS、字体文件，图片也是一个独立的 HTTP 请求
        - 每一个请求都串行的，这么多请求串起来，这导致 H5页面资源加载缓慢
- 总结：H5页面加载速度慢的原因：渲染速度慢 & 页面资源加载缓慢 导致。



### 02.解决WebView加载慢
- 解决方案
    - 前端H5的缓存机制（WebView 自带）
    - 资源预加载
    - 资源拦截



### 03.浏览器缓存机制
- response的headers中的参数, 注意到这么几个字段:Last-Modified、ETag、Expires、Cache-Control
    - Cache-Control
        - 例如Cache-Control:max-age=2592000, 表示缓存时长为2592000秒, 也就是一个月30天的时间。如果30天内需要再次请求这个文件，那么浏览器不会发出请求，直接使用本地的缓存的文件。这是HTTP/1.1标准中的字段。
    - Expires
        - 例如Expires:Tue,25 Sep 2018 07:17:34 GMT, 这表示这个文件的过期时间是格林尼治时间2018年9月25日7点17分。因为我是北京时间2018年8月26日15点请求的, 所以可以看出也是差不多一个月有效期。在这个时间之前浏览器都不会再次发出请求去获取这个文件。Expires是HTTP/1.0中的字段，如果客户端和服务器时间不同步会导致缓存出现问题，因此才有了上面的Cache-Control。当它们同时出现时，Cache-Control优先级更高。
    - Last-Modified
        - 标识文件在服务器上的最新更新时间, 下次请求时，如果文件缓存过期，浏览器通过If-Modified-Since字段带上这个时间，发送给服务器，由服务器比较时间戳来判断文件是否有修改。如果没有修改，服务器返回304(未修改)告诉浏览器继续使用缓存；如果有修改，则返回200，同时返回最新的文件。
    - Etag
        - Etag的取值是一个对文件进行标识的特征字串, 在向服务器查询文件是否有更新时，浏览器通过If-None-Match字段把特征字串发送给服务器，由服务器和文件最新特征字串进行匹配，来判断文件是否有更新：没有更新回包304，有更新回包200。Etag和Last-Modified可根据需求使用一个或两个同时使用。两个同时使用时，只要满足基中一个条件，就认为文件没有更新。
- 浏览器自身的缓存机制是基于http协议层的Header中的信息实现的
    - Cache-control && Expires
        - 这两个字段的作用是：接收响应时，浏览器决定文件是否需要被缓存；或者需要加载文件时，浏览器决定是否需要发出请求
        - Cache-control常见的值包括：no-cache、no-store、max-age等。其中max-age=xxx表示缓存的内容将在 xxx 秒后失效, 这个选项只在HTTP 1.1可用, 并如果和Last-Modified一起使用时, 优先级较高。
    - Last-Modified && ETag
        - 这两个字段的作用是：发起请求时，服务器决定文件是否需要更新。服务端响应浏览器的请求时会添加一个Last-Modified的头部字段，字段内容表示请求的文件最后的更改时间。
        - 而浏览器会在下一次请求通过If-Modified-Since头部字段将这个值返回给服务端，以决定是否需要更新文件
- 这些技术都是协议层所定义的，在Android的webView当中我们可以通过配置决定是否采纳这几个协议的头部属性
    ```java
    // LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
    // LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
    // LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
    // LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
    ws.setCacheMode(WebSettings.LOAD_DEFAULT);
    ```
- 一般设置为默认的缓存模式就可以了。关于缓存的配置, 主要还是靠web前端和后台设置。




### 04.具体缓存那些内容



### 05.预加载数据
- 大概思路介绍
    - 预加载数据就是在客户端初始化WebView的同时，直接由native开始网络请求数据, 当页面初始化完成后，向native获取其代理请求的数据, 数据请求和WebView初始化可以并行进行，缩短总体的页面加载时间。
    - 简单来说就是配置一个预加载列表，在APP启动或某些时机时提前去请求，这个预加载列表需要包含所需H5模块的页面和资源, 客户端可以接管所有请求的缓存，不走webView默认缓存逻辑, 自行实现缓存机制, 原理其实就是拦截WebViewClient的那两个shouldInterceptRequest方法。




### 开源项目
- https://github.com/Ryan-Shz/FastWebView （23star）
- https://github.com/yale8848/CacheWebView  (1.5k star)
- https://github.com/easilycoder/HybridCache  （54star）   文档可以，但是不通
- https://github.com/NEYouFan/ht-candywebcache-android


### 技术博客
- https://www.jianshu.com/p/5e7075f4875f
- 安卓WebView修改DOM：https://www.jianshu.com/p/e320d6bb11e7








