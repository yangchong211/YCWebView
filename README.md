#### 目录介绍
- 01.相关类介绍说明
- 02.如何使用
- 03.js调用
- 04.问题反馈
- 05.webView优化
- 06.关于参考


### 01.相关类介绍说明
- BridgeHandler         接口，主要处理消息回调逻辑
- BridgeUtil            工具类，静态常量，以及获取js消息的一些方法，final修饰
- BridgeWebView         自定义WebView类，主要处理与js之间的消息
- CallBackFunction      js回调
- DefaultHandler        默认的BridgeHandler
- InterWebListener      接口，web的接口回调，包括常见状态页面切换【状态页面切换】，进度条变化【显示和进度监听】等
- Message               自定义消息Message实体类
- ProgressWebView       自定义带进度条的webView
- WebViewJavascriptBridge       js桥接接口
- X5WebChromeClient     自定义x5的WebChromeClient，处理进度监听，title变化，以及上传图片，后期添加视频处理逻辑
- X5WebUtils            工具类，初始化腾讯x5浏览器webView，及调用该类init方法
- X5WebView             可以使用这个类，方便统一初始化WebSettings的一些属性，
                        如果不用这里的，想单独初始化setting属性，也可以直接使用BridgeWebView
- X5WebViewClient       自定义x5的WebViewClient，如果要自定义WebViewClient必须要集成此类
                        一定要继承该类，因为注入js监听是在该类中操作的


### 02.如何使用
- 项目初始化
    ```
    X5WebUtils.init(this);
    ```
- 最普通使用，需要自己做手动设置setting相关属性
    ```
    <BridgeWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- 也可以使用X5WebView，已经做了常见的setting属性设置
    ```
    <X5WebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- 如果想有带进度的，可以使用ProgressWebView
    ```
    <可以使用ProgressWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```

### 03.js调用
- 代码如下所示
    ```
    mWebView.registerHandler("name", new BridgeHandler() {
        @Override
        public void handler(String data, CallBackFunction function) {
            
        }
    });
    ```


### 06.关于参考
- 感谢
    - [x5开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)






