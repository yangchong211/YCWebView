#### 基础使用目录介绍
- 41.管理Cookies分析说明
- 42.WebView中长按处理逻辑
- 43.8.0关于WebView新特性
- 44.H5页面为何加载速度慢
- 45.shouldOverrideUrlLoading返回值
- 46.webBackForwardList用法
- 47.WebView多布局连贯滑动
- 48.开启Google安全浏览服务
- 49.shouldOverrideUrlLoading不执行
- 50.webView使用上的建议



### 41.管理Cookies分析说明
- Cookie的作用
    - Cookie 是服务器发送到用户浏览器并保存在浏览器上的一块数据，它会在浏览器下一次发起请求时被携带并发送到服务器上。
    - 可通过Cookie保存浏览信息来获得更轻松的在线体验，比如保持登录状态、记住偏好设置，并提供本地的相关内容。
- 会话Cookie 与 持久Cookie
    - 会话cookie不需要指定Expires和Max-Age，浏览器关闭之后它会被自动删除。
    - 持久cookie指定了Expires或Max-Age，会被存储到磁盘上，不会因浏览器而失效。
- 第一方Cookie 与 第三方Cookie
    - 每个Cookie都有与之关联的域，与页面域一样的就是第一方Cookie，不一样的就是第三方Cookie。
    ```
    // 设置接收第三方Cookie
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(vWeb, true);
    }
    ```


### 42.WebView中长按处理逻辑
- 大概的步骤如下
    - 给 WebView添加监听
    - 获取点击的内容信息
    - 操作
- 具体代码如下所示
    ```
    this.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            WebView.HitTestResult result = ((WebView)v).getHitTestResult();
            if (null == result){
                return false;
            }
            int type = result.getType();
            if (type == WebView.HitTestResult.UNKNOWN_TYPE){
                return false;
            }
            // 这里可以拦截很多类型，我们只处理图片类型就可以了
            switch (type) {
                case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                    break;
                case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                    break;
                case WebView.HitTestResult.GEO_TYPE: // 地图类型
                    break;
                case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                    break;
                case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                    break;
                default:
                    break;
            }
            return true;
        }
    });
    ```
- type有这几种类型：
    - WebView.HitTestResult.UNKNOWN_TYPE 未知类型
    - WebView.HitTestResult.PHONE_TYPE 电话类型
    - WebView.HitTestResult.EMAIL_TYPE 电子邮件类型
    - WebView.HitTestResult.GEO_TYPE 地图类型
    - WebView.HitTestResult.SRC_ANCHOR_TYPE 超链接类型
    - WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE 带有链接的图片类型
    - WebView.HitTestResult.IMAGE_TYPE 单纯的图片类型
    - WebView.HitTestResult.EDIT_TEXT_TYPE 选中的文字类型


### 43.8.0关于WebView新特性
- WebView新增了一些非常有用的API，可以使用和chrome浏览器类似的API来实现对恶意网站的检测来保护web浏览的安全性，为此需要在manifest中添加如下meta-data标签：
    ```
    <manifest>
    <meta-data
        android:name="android.webkit.WebView.EnableSafeBrowing"
        android:value="true" />
    <!-- ... -->
    </manifest>
    ```
- WebView还增加了关于多进程的API，可以使用多进程来增强安全性和健壮性，如果render进程崩溃了，你还可以使用Termination Handler API来检测到崩溃并做出相应处理。


### 44.H5页面为何加载速度慢
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



### 45.shouldOverrideUrlLoading返回值
- 返回值是什么意思？
     * 不准确的说法如下：
         * 1.返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
         * 2.返回: return true; 表示webView处理url是根据程序来执行的。 返回: return false; 表示webView处理url是在webView内部执行。
     * 准确说法，该方法说明如下所示：
        * 1.若没有设置 WebViewClient 则由系统（Activity Manager）处理该 url，通常是使用浏览器打开或弹出浏览器选择对话框。
        * 2.1若设置 WebViewClient 且该方法返回 true ，则说明由应用的代码处理该 url，WebView 不处理，也就是程序员自己做处理。
        * 2.2若设置 WebViewClient 且该方法返回 false，则说明由 WebView 处理该 url，即用 WebView 加载该 url。
- 该方法何时调用
    - WebView的前进、后退、刷新、以及post请求都不会调用shouldOverrideUrlLoading方法
    - 除去以上行为，还得满足（ ! isLoadUrl || isRedirect）即（不是通过webView.loadUrl来加载的 或者 是重定向） 这个条件，才会调用shouldOverrideUrlLoading方法。
- 一些词汇解释
    - isRedirect就是重定向的url,即重定向url也会触发shouldOverrideUrlLoading；
    - isLoadUrl是什么意思？凡是webView.loadUrl出load页面的，isLoadUrl都是true(原因是webView.loadUrl最终会调到loadUrl(LoadUrlParams params)，进而params.setTransitionType(params.getTransitionType() | PageTransition.FROM_API))．
- 参考文章
    - shouldOverrideUrlLoading深度分析：https://blog.csdn.net/a0407240134/article/details/51482021?winzoom=1



### 46.webBackForwardList用法
- WebBackForwardList webBackForwardList = webView.copyBackForwardList()获取webView加载栈，然后更具加载栈做逻辑操作。
- webBackForwardList常用的方法
    ```
    int size = webBackForwardList.getSize()
    webBackForwardList.getCurrentItem()
    webBackForwardList.getCurrentIndex()
    webBackForwardList.getItemAtIndex(index)
    getSize()方法获取当前加载栈的长度；
    getCurrentItem()获取当前webView所加载的界面，我们可以在这个方法下获得url,title等内容；
    getCurrentIndex()获取当前加载在加载栈中的位置；
    webBackForwardList.getItemAtIndex(index)获取加载栈中第index页面；
    ```
-



### 47.WebView多布局连贯滑动




### 48.开启Google安全浏览服务
- 为向用户提供更安全的浏览体验，您的 WebView 对象会使用 Google 安全浏览（可让应用在用户尝试访问可能不安全的网站时向用户显示警告）验证网址。
- 当 EnableSafeBrowsing 的默认值为 true 时，在某些情况下，您可能有时会希望仅根据条件启用安全浏览功能或停用此功能。Android 8.0（API 级别 26）及更高版本支持使用 setSafeBrowsingEnabled() 来针对单个 WebView 对象启用或停用安全浏览。
- 如果您希望所有 WebView 对象都选择停用安全浏览检查，则向应用的清单文件添加以下 <meta-data> 元素即可实现这一点：
    ```
    <manifest>
        <application>
            <meta-data android:name="android.webkit.WebView.EnableSafeBrowsing"
                       android:value="false" />
            ...
        </application>
    </manifest>
    ```


### 49.shouldOverrideUrlLoading不执行
- 原因1：shouldOverrideUrlLoading不执行，原因是因为在js里面设置了计时器实现可以判断用户长按的功能，当android遇到html的js代码里面执行有计时器如：setTimeout就不会执行android WebView 里面的 shouldOverrideUrlLoading 。
- 原因2：
    - https://blog.csdn.net/weixin_37806077/article/details/85488680
    - https://blog.csdn.net/KevinsCSDN/article/details/89598789



### 50.webView使用上的建议
- 将布局高度和宽度设置为 match_parent
    - 将 WebView 对象的高度和宽度设置为 match_parent 可以确保应用视图的尺寸合适。我们建议不要将高度设置为 wrap_content，因为它会导致尺寸不正确；在以 Android 4.4（API 级别 19）及更低版本为目标平台的应用中，系统会忽略 HTML 视口元标记以维持向后兼容性。同样，不支持将布局宽度设置为 wrap_content，这会导致 WebView 改用其父级的宽度。由于这一行为，请也务必确保 WebView 对象的父级布局对象的高度和宽度均未设置为 wrap_content。
- 避免多个文件请求
    - 由于移动设备的连接速度通常远远低于桌面设备，因此您应尽可能提高网页加载速度。提高网页加载速度的一种方法是，避免在 <head> 中加载样式表和脚本文件等额外文件。优化移动设备上网页加载速度的更有效方法是，利用 Google 的 PageSpeed Insights 进行移动设备浏览体验分析。如果您希望优化应用的性能，请参阅 PageSpeed Insights 规则。


