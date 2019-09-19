#### 目录介绍
- 01.前沿说明
    - 1.1 案例展示效果
    - 1.2 该库功能和优势
    - 1.3 相关类介绍说明
- 02.如何使用
    - 2.1 如何引入
    - 2.2 最简单使用
    - 2.3 常用api
    - 2.4 使用建议
- 03.js调用
- 04.问题反馈
- 05.webView优化
- 06.关于参考


### 01.前沿说明
- 基于腾讯x5开源库，提高webView开发效率，大概要节约你百分之六十的时间成本。该案例支持处理js的交互逻辑且无耦合、同时暴露进度条加载进度、可以监听异常error状态、支持视频播放并且可以全频、支持加载word，xls，ppt，pdf，txt等文件文档、发短信、打电话、发邮件、打开文件操作上传图片、唤起原生App、x5库为最新版本，功能强大。


#### 1.1 案例展示效果
- WebView启动过程大概分为以下几个阶段，这里借鉴美团的一张图片
    - ![image](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2017/9a2f8beb.png)




#### 1.2 该库功能和优势
- 提高webView开发效率，大概要节约你百分之六十的时间成本，一键初始化操作；
- 支持处理js的交互逻辑，方便快捷，并且无耦合；
- 暴露进度条加载进度，结束，以及异常状态listener给开发者；
- 支持视频播放，可以切换成全频播放视频，可旋转屏幕；
- 集成了腾讯x5的WebView，最新版本，功能强大；
- 支持打开文件的操作，比如打开相册，然后选中图片上传，兼容版本(5.0)
- 支持加载word，xls，ppt，pdf，txt等文件文档，使用方法十分简单



#### 1.3 相关类介绍说明
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
- X5WebView             可以使用这个类，方便统一初始化WebSettings的一些属性，如果不用这里的，想单独初始化setting属性，也可以直接使用BridgeWebView
- X5WebViewClient       自定义x5的WebViewClient，如果要自定义WebViewClient必须要集成此类，一定要继承该类，因为注入js监听是在该类中操作的


### 02.如何使用
- 如何引用，该x5的库已经更新到最新版本
    ```
    implementation 'cn.yc:WebViewLib:1.1.2'
    ```
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
- 优化一下
    - 关于设置js支持的属性
    ```
    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }
    ```
    - 关于destroy销毁逻辑
    ```
    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                webView.stopLoading();
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            Log.e("X5WebViewActivity", e.getMessage());
        }
        super.onDestroy();
    }
    ```


### 03.js调用
- 代码如下所示，下面中的jsname代表的是js这边提供给客户端的方法名称
    ```
    mWebView.registerHandler("jsname", new BridgeHandler() {
        @Override
        public void handler(String data, CallBackFunction function) {
            
        }
    });
    ```
- 如何回调数据给web那边
    ```
    function.onCallBack("回调数据");
    ```


### 04.问题反馈
- 视频播放宽度比webView设置的宽度大，超过屏幕：这个时候可以设置ws.setLoadWithOverviewMode(false);
- 关于加载word，pdf，xls等文档文件注意事项：Tbs不支持加载网络的文件，需要先把文件下载到本地，然后再加载出来



### 05.webView优化
- 视频全屏播放按返回页面被放大（部分手机出现），至于原因暂时没有找到，解决方案如下所示
    ```
    /**
     * 当缩放改变的时候会调用该方法
     * @param view                              view
     * @param oldScale                          之前的缩放比例
     * @param newScale                          现在缩放比例
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }
    ```






### 06.关于参考
- 感谢
    - [x5开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)






