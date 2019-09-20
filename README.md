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
- 07.其他说明介绍


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
- 关于web的接口回调，包括常见状态页面切换，进度条变化等监听处理
    ```
    mWebView.getX5WebChromeClient().setWebListener(interWebListener);
    private InterWebListener interWebListener = new InterWebListener() {
        @Override
        public void hindProgressBar() {
            pb.setVisibility(View.GONE);
        }
    
        @Override
        public void showErrorView() {
            //设置自定义异常错误页面
        }
    
        @Override
        public void startProgress(int newProgress) {
            pb.setProgress(newProgress);
        }
    };
    ```
- 关于视频播放的时候，web的接口回调，主要是视频相关回调，比如全频，取消全频，隐藏和现实webView
    ```
    x5WebChromeClient = x5WebView.getX5WebChromeClient();
    x5WebChromeClient.setVideoWebListener(new VideoWebListener() {
        @Override
        public void showVideoFullView() {
            //视频全频播放时监听
        }
    
        @Override
        public void hindVideoFullView() {
            //隐藏全频播放，也就是正常播放视频
        }
    
        @Override
        public void showWebView() {
            //显示webView
        }
    
        @Override
        public void hindWebView() {
            //隐藏webView
        }
    });
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
- **5.0.1 视频全屏播放按返回页面被放大（部分手机出现），至于原因暂时没有找到，解决方案如下所示**
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
- **5.0.2 加载webView中的资源时，加快加载的速度优化，主要是针对图片**
    - html代码下载到WebView后，webkit开始解析网页各个节点，发现有外部样式文件或者外部脚本文件时，会异步发起网络请求下载文件，但如果在这之前也有解析到image节点，那势必也会发起网络请求下载相应的图片。在网络情况较差的情况下，过多的网络请求就会造成带宽紧张，影响到css或js文件加载完成的时间，造成页面空白loading过久。解决的方法就是告诉WebView先不要自动加载图片，等页面finish后再发起图片加载。
    ```
    //初始化的时候设置，具体代码在X5WebView类中
    if(Build.VERSION.SDK_INT >= KITKAT) {
        //设置网页在加载的时候暂时不加载图片
        ws.setLoadsImagesAutomatically(true);
    } else {
        ws.setLoadsImagesAutomatically(false);
    }
    
    /**
     * 当页面加载完成会调用该方法
     * @param view                              view
     * @param url                               url链接
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //页面finish后再发起图片加载
        if(!webView.getSettings().getLoadsImagesAutomatically()) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        }
    }
    ```
- **5.0.3 自定义加载异常error的状态页面，比如下面这些方法中可能会出现error**
    - 当WebView加载页面出错时（一般为404 NOT FOUND），安卓WebView会默认显示一个出错界面。当WebView加载出错时，会在WebViewClient实例中的onReceivedError()，还有onReceivedTitle方法接收到错误
    ```
    /**
     * 请求网络出现error
     * @param view                              view
     * @param errorCode                         错误🐎
     * @param description                       description
     * @param failingUrl                        失败链接
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            String data = "Page NO FOUND！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        } else {
            if (webListener!=null){
                webListener.showErrorView();
            }
        }
    }
    
    // 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
    // 值得注意的是，不同的是过时的版本的回调，新的版本将被称为任何资源（iframe，图像等）
    // 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
    // 6.0 之后
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            X5WebUtils.log("服务器异常"+error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        if (webListener!=null){
            webListener.showErrorView();
        }
    }
    
    /**
     * 这个方法主要是监听标题变化操作的
     * @param view                              view
     * @param title                             标题
     */
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (title.contains("404") || title.contains("网页无法打开")){
            if (webListener!=null){
                webListener.showErrorView();
            }
        } else {
            // 设置title
        }
    }
    ```
- **5.0.4 WebView硬件加速导致页面渲染闪烁**
    - 4.0以上的系统我们开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑。但有个副作用就是，当WebView视图被整体遮住一块，然后突然恢复时（比如使用SlideMenu将WebView从侧边滑出来时），这个过渡期会出现白块同时界面闪烁。解决这个问题的方法是在过渡期前将WebView的硬件加速临时关闭，过渡期后再开启
    ```
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    ```


### 06.关于参考
- 感谢开源库
    - [x5开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)
- 参考博客
    - [WebView性能、体验分析与优化](https://tech.meituan.com/2017/06/09/webviewperf.html)



### 07.其他说明介绍
#### 关于其他内容介绍
![image](https://upload-images.jianshu.io/upload_images/4432347-7100c8e5a455c3ee.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 关于博客汇总链接
- 1.[技术博客汇总](https://www.jianshu.com/p/614cb839182c)
- 2.[开源项目汇总](https://blog.csdn.net/m0_37700275/article/details/80863574)
- 3.[生活博客汇总](https://blog.csdn.net/m0_37700275/article/details/79832978)
- 4.[喜马拉雅音频汇总](https://www.jianshu.com/p/f665de16d1eb)
- 5.[其他汇总](https://www.jianshu.com/p/53017c3fc75d)


#### 其他推荐
- 博客笔记大汇总【15年10月到至今】，包括Java基础及深入知识点，Android技术博客，Python学习笔记等等，还包括平时开发中遇到的bug汇总，当然也在工作之余收集了大量的面试题，长期更新维护并且修正，持续完善……开源的文件是markdown格式的！同时也开源了生活博客，从12年起，积累共计47篇[近20万字]，转载请注明出处，谢谢！
- 链接地址：https://github.com/yangchong211/YCBlogs
- 如果觉得好，可以star一下，谢谢！当然也欢迎提出建议，万事起于忽微，量变引起质变！



#### 关于LICENSE
```
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```




