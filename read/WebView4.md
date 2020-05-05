#### 基础使用目录介绍
- 41.管理Cookies分析说明
- 42.WebView中长按处理逻辑
- 43.8.0关于WebView新特性
- 44.H5页面为何加载速度慢
- 47.WebView多布局连贯滑动




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



### 47.WebView多布局连贯滑动




