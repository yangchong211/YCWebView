#### 目录介绍
- 01.该库具有的功能
- 02.该库如何使用
- 03.js调用原理
- 04.问题反馈总结
- 05.WebView优化
- 06.WebView知识点
- 07.WebView缓存
- 08.其他说明介绍




### 01.前沿说明
- 基于腾讯x5开源库，提高webView开发效率，大概要节约你百分之六十的时间成本。该案例支持处理js的交互逻辑且无耦合、同时暴露进度条加载进度、可以监听异常error状态、支持视频播放并且可以全频、支持加载word，xls，ppt，pdf，txt等文件文档、发短信、打电话、发邮件、打开文件操作上传图片、唤起原生App、x5库为最新版本，功能强大。项目地址：[webView开源库](https://github.com/yangchong211/YCWebView)


#### 1.1 案例展示效果
- WebView启动过程大概分为以下几个阶段，这里借鉴美团的一张图片
    - ![image](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2017/9a2f8beb.png)


#### 1.2 该库功能和优势
|**类型** | 功能说明 |
|--------   |-----        |
|**项目介绍** | x5WebView + 拦截缓存(okHttp) + HttpDns解析优化方案 + 视频播放 + js交互|
|**开发效率** | 大概要节约你百分之六十的时间成本，配置简单，使用炒鸡简单，api文档和注释炒鸡详细 |
|**视频播放** | 支持视频播放，可以切换成全频播放视频，可旋转屏幕，暴露视频操作监听listener给开发者 |
|**js交互逻辑** | 方便快捷，并且无耦合，操作十分简单 |
|**加载进度监听** | 暴露进度条加载进度，结束，以及异常状态(分多种状态：无网络，404，onReceivedError，sslError异常等)listener给开发者 |
|**加载文件** | 支持加载word，xls，ppt，pdf，txt等文件文档，使用方法十分简单 |
|**重定向** | 优雅解决重定向回退，白屏等问题 |
|**https+dns** | 添加了阿里的https+dns优化方案，按照配置初始化填入accountID和host即可使用，默认没有使用 |
|**夜间模式** | 支持设置夜间模式，一行代码即可设置 |
|**文件操作** | 支持打开文件的操作，比如打开相册，然后选中图片上传，兼容版本(5.0) |
|**刘海屏适配** | 支持设置刘海屏适配，对于刘海屏机器如果webview被遮挡会自动padding |
|**仿微信进度条** | 支持设置仿微信加载H5页面进度条，完全无耦合，操作简单，极大提高用户体验 |
|**自定义** | 支持用户按照规范自定义WebViewClient和WebChromeClient，不影响js通信 |
|**缓存** | 除了webView自带缓存外，还添加了资源拦截缓存，交给OkHttp去做，支持设置超时，设置缓存空间大小 |
|**scheme** | 统一处理web页面打电话，发短信，定位，邮件，开启支付宝，微信等scheme拦截处理 |
|**无痕模式** | 支持设置无痕模式 |
|**问题汇总** | 汇集绝大多数问题，以及解决方案，是学习和深入理解webView的一个比较全面的案例 |
|**设计思想** | 充分运用了面向对象的设计思想，将视频全屏播放，scheme拦截，web进度条，拦截缓存抽成独立的部分，你也可以拿来即用，完全解耦 |



### [02.该库如何使用](https://github.com/yangchong211/YCWebView/wiki/02.%E8%AF%A5%E5%BA%93%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8)
- 2.1 如何引入该库
- 2.2 最简单使用
- 2.3 常用api说明
- 2.4 使用建议
- 2.5 关于web页面异常状态区分类型
- 2.6 如何使用拦截缓存
- 2.7 使用Https+Dns
- 2.8 js交互操作处理
- 2.9 关于添加混淆



### [03.js调用原理](https://github.com/yangchong211/YCWebView/wiki/03.Js%E4%BA%A4%E4%BA%92%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90)
- 01.WebView加载html页面
- 02.加载WebViewJavascriptBridge.js
- 03.分析WebViewJavascriptBridge.js
- 04.页面Html注册”functionInJs”方法
- 05.“functionInJs”执行结果回传Java
- 06.该库交互流程图



### 04.问题反馈总结
- [4.0.0 WebView进化史介绍](https://github.com/yangchong211/YCWebView/blob/master/read/Question1.md)
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
- 4.1.3 开启硬件加速导致的闪烁问题
- 4.1.4 webView加载网页不显示图片
- 4.1.5 绕过证书校验漏洞
- [4.1.6 allowFileAccess漏洞](https://github.com/yangchong211/YCWebView/blob/master/read/Question2.md)
- 4.1.7 WebView嵌套ScrollView问题
- 4.1.8 WebView中图片点击放大
- 4.1.9 页面滑动期间不渲染/执行
- 4.2.0 被运营商劫持和注入问题
- 4.2.1 解决资源加载缓慢问题
- 4.2.2 判断是否已经滚动到页面底端
- 4.2.3 使用loadData加载html乱码
- 4.2.4 WebView下载进度无法监听
- 4.2.5 webView出现302/303重定向
- 4.2.6 webView出现302/303白屏
- 4.2.8 onReceiveError问题
- 4.2.9 loadUrl在19以上超过2097152个字符失效
- 4.3.0 WebViewJavascriptBridge: WARNING
- [4.3.1 Android与js传递数据大小有限制](https://github.com/yangchong211/YCWebView/blob/master/read/Question3.md)
- 4.3.2 多次调用callHandler部分回调函数未被调用
- 4.3.3 字符串转义bug探讨
- 4.3.8 Javascript调用原生方法会偶现失败
- 4.3.9 dispatchMessage运行主线程问题
- 4.4.0 怎么实现WebView免流方案
- 4.4.1 Channel is unrecoverably broken and will be disposed!
- 4.4.2 定制js的alert,confirm和prompt对话框
- 4.4.3 x5长按图片如何操作
- 4.4 4 x5长按文字内容如何自定义弹窗
- 4.4.5 webView.goBack()会刷新页面吗
- [4.4.6 mWebView.scrollTo(0, 0)回顶部失效](https://github.com/yangchong211/YCWebView/blob/master/read/Question4.md)
- 4.4.7 部分手机监听滑动顶部或底部失效
- 4.4.8 prompt的一个坑导致js挂掉
- 4.4.9 webView背景设置为透明无效探索
- 4.5.0 如何屏蔽掉WebView中长按事件
- 4.5.1 WeView出现OOM影响主进程如何避免
- 4.5.2 WebView域控制不严格漏洞
- 4.5.3 下载文件时的路径穿越问题
- 4.5.4 WebView中http和https混合使用问题
- 4.5.5 调用系统EMAIL发送邮件崩溃
- 4.5.7 WebView访问部分网页崩溃问题



### 05.webView优化
- [5.0.1 视频全屏播放按返回页面被放大](https://github.com/yangchong211/YCWebView/blob/master/read/Optimize1.md)
- 5.0.2 加快加载webView中的图片资源
- 5.0.3 自定义加载异常error的状态页面
- 5.0.4 WebView硬件加速导致页面渲染闪烁
- 5.0.5 WebView加载证书错误
- 5.0.6 web音频播放销毁后还有声音
- 5.0.7 DNS采用和客户端API相同的域名
- 5.0.8 如何设置白名单操作
- 5.0.9 后台无法释放js导致发热耗电
- 5.1.0 可以提前显示加载进度条
- 5.1.1 WebView密码明文存储漏洞优化
- 5.1.2 页面关闭后不要执行web中js
- 5.1.3 WebView + HttpDns优化
- 5.1.4 如何禁止WebView返回时刷新
- 5.1.5 WebView处理404、300逻辑
- [5.1.6 WebView判断断网和链接超时](https://github.com/yangchong211/YCWebView/blob/master/read/Optimize2.md)
- 5.1.7 @JavascriptInterface注解方法注意点
- 5.1.8 使用onJsPrompt实现js通信注意点
- 5.1.9 Cookie同步场景和具体操作
- 5.2.0 shouldOverrideUrlLoading处理多类型
- 5.2.1 WebView独立进程解决方案
- 5.2.2 截取WebView屏幕的整个可视区域
- 5.2.3 截取WebView屏幕长图效果
- [更多webView优化内容](https://github.com/yangchong211/YCWebView/blob/master/Optimize.md)



### 06.WebView知识点
- [01.常用的基础使用介绍](https://github.com/yangchong211/YCWebView/blob/master/read/WebView1.md)
- 02.Android调用Js方式
- 03.Js调用Android方式
- 04.清除缓存和缓存分析
- 05.为什么WebView难搞
- 06.如何处理加载错误
- 07.触发加载网页的行为
- 09.web进度条避免多次加载
- 10.多次获取web标题title
- [11.什么是302/303重定向](https://github.com/yangchong211/YCWebView/blob/master/read/WebView2.md)
- 12.301/302业务场景白屏描述
- 13.301/302业务白屏解决方案
- 14.301/302回退栈问题描述
- 15.301/302回退栈问题解决方案
- 16.如何设置WebView触摸点击事件
- 17.如何用代码判断是否重定向
- 18.如何解决重定向回退栈问题
- 19.shouldOverrideUrlLoading
- [21.loadUrl(url)流程分析](https://github.com/yangchong211/YCWebView/blob/master/read/WebView3.md)
- 22.js的调用时机分析
- 23.如何使用DeepLink
- 24.应用被作为三方浏览器打开
- 25.理解WebView独立进程
- 26.使用外部浏览器下载
- 27.tel,sms等协议用法
- 29.关于拦截处理注意要点
- 30.FileChooser文件处理
- [41.管理Cookies分析说明](https://github.com/yangchong211/YCWebView/blob/master/read/WebView4.md)
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
- [51.关于图片显示隐藏设置](https://github.com/yangchong211/YCWebView/blob/master/read/WebView5.md)
- 51.关于图片显示隐藏设置
- [更多内容看wiki](https://github.com/yangchong211/YCWebView/wiki)



### 07.WebView缓存
- 01.WebView为何加载慢
- 02.解决WebView加载慢
- 03.浏览器缓存机制
- 04.WebView设置缓存
- 05.具体缓存那些内容
- 06.本地资源替换操作
- 07.如何处理预加载数据
- [更多内容查看wiki](https://github.com/yangchong211/YCWebView/wiki/7.1-%E7%BC%93%E5%AD%98%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86)



### 08.其他说明介绍
#### 关于其他内容介绍
![image](https://upload-images.jianshu.io/upload_images/4432347-7100c8e5a455c3ee.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 关于博客汇总链接
- 1.[技术博客汇总](https://www.jianshu.com/p/614cb839182c)
- 2.[开源项目汇总](https://blog.csdn.net/m0_37700275/article/details/80863574)
- 3.[生活博客汇总](https://blog.csdn.net/m0_37700275/article/details/79832978)
- 4.[喜马拉雅音频汇总](https://www.jianshu.com/p/f665de16d1eb)
- 5.[其他汇总](https://www.jianshu.com/p/53017c3fc75d)



#### 其他推荐
- 博客笔记大汇总【15年10月到至今】，包括Java基础及深入知识点，Android技术博客，Python学习笔记等等，还包括平时开发中遇到的bug汇总，当然也在工作之余收集了大量的面试题，长期更新维护并且修正，持续完善……开源的文件是markdown格式的！同时也开源了生活博客，从12年起，积累共计47篇[近100万字]，转载请注明出处，谢谢！
- 链接地址：https://github.com/yangchong211/YCBlogs
- 如果觉得好，可以star一下，谢谢！当然也欢迎提出建议，万事起于忽微，量变引起质变！
- 感谢开源库
    - [x5官方开发文档](https://x5.tencent.com/tbs/guide/sdkInit.html)
    - [JsBridge开源库](https://github.com/lzyzsd/JsBridge)
    - [WebViewStudy开源库](https://github.com/youlookwhat/WebViewStudy)
    - [DSBridge](https://github.com/wendux/DSBridge-Android)
- 参考博客
    - [WebView性能、体验分析与优化](https://tech.meituan.com/2017/06/09/webviewperf.html)
    - [WebView详解，常见漏洞详解和安全源码上](https://juejin.im/post/58a037df86b599006b3fade4)
    - [WebView详解，常见漏洞详解和安全源码下](https://blog.csdn.net/self_study/article/details/55046348)
    - [如何设计一个优雅健壮的Android WebView](https://juejin.im/post/5a94f9d15188257a63113a74)



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




