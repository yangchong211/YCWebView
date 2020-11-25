#### 问题汇总目录介绍
- 4.6.6 WebView如何隐藏H5的部分内容
- 4.6.7 setUserAgentString作用是干什么
- 4.6.8 WebView中onPause和onResume误区
- 4.6.9 iframe是什么东西
- 4.6.10 如何设置字体大小或者更换字体
- 4.6.11 net::ERR_PROXY_CONNECTION_FAILED











### 4.6.6 WebView如何隐藏H5的部分内容问题
- 产品需求
    - 要在App中打开xx的链接，并且要隐藏掉H5页面的某些内容，这个就需要在Java代码中操作WebView，让H5页面加载完成后能够隐藏掉某些内容。
- 需要几个前提条件
    - 页面加载完成
    - 在Java代码中执行Js代码
    - 利用Js代码找到页面中的底部栏并将其隐藏
- 如何在h5中找隐藏元素
    - 在H5页面中找到某个元素还是有很多方法的，比如getElementById()、getElementsByClassName()、getElementsByTagName()等，具体根据页面来选择
    - 找到要隐藏的h5视图div，然后可以看到有id，或者class。这里用class举例子，比如div的class叫做'copyright'
    - document.getElementsByClassName('copyright')[0].style.display='none'
- 可能出现的问题
    - 等到页面加载完毕后，执行隐藏div标签方法，会造成控件闪屏，不抬友好。但是如果在onProgressChanged执行到85左右隐藏标签又会导致偶发性没有隐藏成功。
    - 如果有重定向，则会出现执行多次。写了这个隐藏逻辑，会造成所有的页面都会执行，不知道是否会影响性能？待研究……
- 代码操作如下所示
    ```
    /**
     * 可以等页面加载完成后，执行Js代码，找到底部栏并将其隐藏
     * 如何找h5页面元素：
     *      在H5页面中找到某个元素还是有很多方法的，比如getElementById()、getElementsByClassName()、getElementsByTagName()等，具体根据页面来选择
     * 隐藏底部有赞的东西
     *      这个主要找到copyright标签，然后反射拿到该方法，调用隐藏逻辑
     * 步骤操作如下：
     * 1.首先通过getElementByClassName方法找到'class'为'copyright'的所有元素，返回的是一个数组，
     * 2.在这个页面中，只有底部栏的'class'为'copyright'，所以取数组中的第一个元素对应的就是底部栏元素
     * 3.然后将底部栏的display属性设置为'none'，表示底部栏不显示，这样就可以将底部栏隐藏
     *
     * 可能存在问题：
     * onPageFinished没有执行，导致这段代码没有走
     */
    private void hideBottom() {
        try {
            if (mWebView!=null) {
                //定义javaScript方法
                String javascript = "javascript:function hideBottom() { "
                        + "document.getElementsByClassName('copyright')[0].style.display='none'"
                        + "}";
                //加载方法
                mWebView.loadUrl(javascript);
                //执行方法
                mWebView.loadUrl("javascript:hideBottom();");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ```


### 4.6.7 setUserAgentString作用是干什么
- 关于UA的设置和获取
    ```
    String userAgentString = mWebView.getSettings().getUserAgentString();
    LogUtils.d("WebView----初始化webView操作","-userAgentString---"+userAgentString);
    String newUa = userAgentString + "_new";
    mWebView.getSettings().setUserAgentString(newUa);
    ```
    - 打印ua值
    ```
    Mozilla/5.0 (Linux; Android 9; COL-AL10 Build/HUAWEICOL-AL10; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36
    ```
- ua值中的参数分别是什么意思
    - Mozilla/5.0	伪装成Mozilla排版引擎的浏览器以达到兼容
    - (Linux; Android 9; COL-AL10 Build/HUAWEICOL-AL10; wv)	浏览器所运行的系统的详细信息
    - AppleWebKit/537.36	浏览器所使用的平台
    - (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100	浏览器平台的详细信息
    - Mobile Safari/537.36	被浏览器用于指示特定的直接由浏览器提供或者通过第三方提供的可用的增强功能
- UA值的作用是什么
    - User-Agent,中文名为用户代理，是Http请求协议中请求头的一部分，在手机端/pc端，可以通过UA来判断不同的设备，从而可以显示不同的排版，进而给用户提供更好的体验。
    - ua一般就是告诉web当前加载网页的是安卓、ios、平板还是电脑的浏览器，web可以获取这个值，然后根据不同浏览器做不同渲染，从而达到兼容。
    - webview也可以在setUserAgentString设置加入自己的参数，配合web做一些权限判断，这样可以达到在其他没有我们独有的ua参数的浏览器就没法正常加载，当然还可以做其他功能啦。
- UA设置建议
    - 获取webview的ua然后再拼接上我们自己的参数这种方式来setUserAgentString，以防止带来其他的bug
    ```
    WebSettings settings = webView.getSettings();
    // 获取默认的UA
    String ua = settings.getUserAgentString();
    // UA追加自定义标识符
    settings.setUserAgentString(ua + "; ****");
    ```

### 4.6.8 WebView中onPause和onResume误区
- 代码案例如下所示
    - 这个是什么意思呢？
    ```
    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }
    ```



### 4.6.9 iframe是什么东西
- 所有浏览器都支持 <iframe> 标签
提示和注释：
提示：您可以把需要的文本放置在 <iframe> 和 </iframe> 之间，这样就可以应对无法理解 iframe 的浏览器。




### 4.6.10 如何设置字体大小或者更换字体
- 如何设置字体大小
    ``` java
    //WebView加上这个设置后,WebView里的字体就不会随系统字体大小设置发生变化了.
    webview.getSettings().setTextZoom(100);
    ```
- 设置字体大小
    ``` java
    /**
     * 设置字体大小
     * @param fontSize                      字体大小
     */
    public void setTextSize(int fontSize){
        WebSettings settings = this.getSettings();
        settings.setSupportZoom( true);
        switch (fontSize) {
            case  1:
                settings.setTextSize(WebSettings.TextSize.SMALLEST);
                break;
            case  2:
                settings.setTextSize(WebSettings.TextSize.SMALLER);
                break;
            case  3:
                settings.setTextSize(WebSettings.TextSize.NORMAL);
                break;
            case  4:
                settings.setTextSize(WebSettings.TextSize.LARGER);
                break;
            case  5:
                settings.setTextSize(WebSettings.TextSize.LARGEST);
                break;
            default:
                settings.setTextSize(WebSettings.TextSize.NORMAL);
                break;
        }
    }
    ```
- 通过屏幕密度调整分辨率
    ```
    /**
     * 通过屏幕密度调整分辨率
     */
    public void setDensityZoom(){
        WebSettings settings = this.getSettings();
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                //75
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                //100
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                //150
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        settings.setDefaultZoom(zoomDensity);
    }
    ```
- 设置自适应
    ``` java
    settings.setUseWideViewPort(true); 
    settings.setLoadWithOverviewMode(true); 
    settings.setTextZoom(100);
    ```




### 4.6.11 net::ERR_PROXY_CONNECTION_FAILED
- 解决办法
    - 这个时候要查下手机网络是否设置成代理了，把代理关掉就可以了。设置方法：修改当前连接的网络配置，“代理”项设置成“无”。












