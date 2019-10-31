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
    - 3.1 如何使用项目js调用
    - 3.2 js的调用时机分析
- 04.问题反馈
- 05.webView优化
- 06.关于参考
- 07.其他说明介绍


### 01.前沿说明
- 基于腾讯x5封源库，提高webView开发效率，大概要节约你百分之六十的时间成本。该案例支持处理js的交互逻辑且无耦合、同时暴露进度条加载进度、可以监听异常error状态、支持视频播放并且可以全频、支持加载word，xls，ppt，pdf，txt等文件文档、发短信、打电话、发邮件、打开文件操作上传图片、唤起原生App、x5库为最新版本，功能强大。


#### 1.1 案例展示效果
- WebView启动过程大概分为以下几个阶段，这里借鉴美团的一张图片
    - ![image](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2017/9a2f8beb.png)
- 项目案例效果展示图
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-0.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-1.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-2.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-3.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-4.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-5.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-6.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-7.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-8.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-9.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-10.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-11.jpg" width="500" hegiht="313" />
    - <img src="https://github.com/yangchong211/YCWebView/blob/master/image/QQ20190923-12.jpg" width="500" hegiht="313" />



#### 1.2 该库功能和优势
- 提高webView开发效率，大概要节约你百分之六十的时间成本，一键初始化操作；
- 支持处理js的交互逻辑，方便快捷，并且无耦合；
- 暴露进度条加载进度，结束，以及异常状态listener给开发者；
- 支持视频播放，可以切换成全频播放视频，可旋转屏幕；
- 集成了腾讯x5的WebView，最新版本，功能强大；
- 支持打开文件的操作，比如打开相册，然后选中图片上传，兼容版本(5.0)
- 支持加载word，xls，ppt，pdf，txt等文件文档，使用方法十分简单
- 



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
#### 2.1 如何引入
- **如何引用，该x5的库已经更新到最新版本**
    ```
    implementation 'cn.yc:WebViewLib:1.1.2'
    ```

#### 2.2 最简单使用
- **项目初始化**
    ```
    X5WebUtils.init(this);
    ```
- **最普通使用，需要自己做手动设置setting相关属性**
    ```
    <BridgeWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- **也可以使用X5WebView，已经做了常见的setting属性设置**
    ```
    <X5WebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- **如果想有带进度的，可以使用ProgressWebView**
    ```
    <可以使用ProgressWebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbarSize="3dp" />
    ```
- **如何使用自己的WebViewClient和WebChromeClient**
    ```
    //主要是在X5WebViewClient和X5WebChromeClient已经做了很多常见的逻辑处理，如果不满足你使用，可以如下这样写
    MyX5WebViewClient webViewClient = new MyX5WebViewClient(webView, this);
    webView.setWebViewClient(webViewClient);
    MyX5WebChromeClient webChromeClient = new MyX5WebChromeClient(this);
    webView.setWebChromeClient(webChromeClient);
    
    private class MyX5WebViewClient extends X5WebViewClient {
        public MyX5WebViewClient(BridgeWebView webView, Context context) {
            super(webView, context);
        }
        
        //重写你需要的方法即可
    }
    
    private class MyX5WebChromeClient extends X5WebChromeClient{
        public MyX5WebChromeClient(Activity activity) {
            super(activity);
        }
        
        //重写你需要的方法即可
    }
    ```


#### 2.3 常用api
- **关于web的接口回调，包括常见状态页面切换，进度条变化等监听处理**
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
            //该方法是是监听进度条进度变化的逻辑
            pb.setProgress(newProgress);
        }
    };
    ```
- **关于视频播放的时候，web的接口回调，主要是视频相关回调，比如全频，取消全频，隐藏和现实webView**
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
- **其他api说明**
```
//X5WebView中
//设置是否开启密码保存功能，不建议开启，默认已经做了处理，存在盗取密码的危险
mWebView.setSavePassword(false);
//是否开启软硬件加速
mWebView.setOpenLayerType(false);
//获取x5WebChromeClient对象
x5WebChromeClient = mWebView.getX5WebChromeClient();
//获取x5WebViewClient对象
x5WebViewClient = mWebView.getX5WebViewClient();
```


#### 2.4 使用建议
- **优化一下相关的操作**
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
#### 3.1 如何使用项目js调用
- **代码如下所示，下面中的jsname代表的是js这边提供给客户端的方法名称**
    ```
    mWebView.registerHandler("jsname", new BridgeHandler() {
        @Override
        public void handler(String data, CallBackFunction function) {
            
        }
    });
    ```
- **如何回调数据给web那边**
    ```
    function.onCallBack("回调数据");
    ```



#### 3.2 js的调用时机分析
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



### [04.问题反馈](https://github.com/yangchong211/YCWebView/blob/master/Question.md)
- 4.0.0 WebView进化史介绍
- 4.0.1 提前初始化WebView必要性
- 4.0.2 x5加载office资源
- 4.0.3 WebView播放视频问题
- 4.0.4 无法获取webView的正确高度
- 4.0.5 使用scheme协议打开链接风险
- 4.0.6 如何处理加载错误
- 4.0.7 webView防止内存泄漏
- 4.0.8 关于js注入时机修改
- 4.0.9 视频播放宽度超过屏幕
- 4.1.0 如何保证js安全性
- 4.1.1 如何代码开启硬件加速
- 4.1.2 WebView设置Cookie
- 4.1.4 webView加载网页不显示图片
- 4.1.5 绕过证书校验漏洞
- 4.1.6 allowFileAccess漏洞
- 4.1.7 WebView嵌套ScrollView问题
- 4.1.8 WebView中图片点击放大
- [更多问题反馈内容](https://github.com/yangchong211/YCWebView/blob/master/Question.md)



### [05.webView优化](https://github.com/yangchong211/YCWebView/blob/master/Optimize.md)
- 5.0.1 视频全屏播放按返回页面被放大
- 5.0.2 加快加载webView中的图片资源
- 5.0.3 自定义加载异常error的状态页面
- 5.0.4 WebView硬件加速导致页面渲染闪烁
- 5.0.5 WebView加载证书错误
- 5.0.6 web音频播放销毁后还有声音
- 5.0.7 DNS采用和客户端API相同的域名
- 5.0.8 如何设置白名单操作
- 5.0.9 后台无法释放js导致发热耗电
- 5.1.0 可以提前显示加载进度条
- [更多webView优化内容](https://github.com/yangchong211/YCWebView/blob/master/Optimize.md)


### 06.关于参考
- 感谢开源库
    - [x5开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)
- 参考博客
    - [WebView性能、体验分析与优化](https://tech.meituan.com/2017/06/09/webviewperf.html)
    - [WebView详解，常见漏洞详解和安全源码](https://juejin.im/post/58a037df86b599006b3fade4)



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




