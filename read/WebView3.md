#### 基础使用目录介绍
- 21.loadUrl(url)流程分析
- 22.js的调用时机分析
- 23.如何使用DeepLink
- 24.应用被作为三方浏览器打开
- 25.理解WebView独立进程
- 26.使用外部浏览器下载
- 27.tel,sms等协议用法
- 29.关于拦截处理注意要点
- 30.FileChooser文件处理



### 11.WebView.loadUrl(url)流程
- WebView.loadUrl(url)加载网页做了什么？
    - 加载网页是一个复杂的过程，在这个过程中，我们可能需要执行一些操作，包括：
    - 加载网页前，重置WebView状态以及与业务绑定的变量状态。WebView状态包括重定向状态(mTouchByUser)、前端控制的回退栈(mBackStep)等，业务状态包括进度条、当前页的分享内容、分享按钮的显示隐藏等。
    - 加载网页前，根据不同的域拼接本地客户端的参数，包括基本的机型信息、版本信息、登录信息以及埋点使用的Refer信息等，有时候涉及交易、财产等还需要做额外的配置。
    - 开始执行页面加载操作时，会回调WebViewClient.onPageStarted(webView,url,favicon)。在此方法中，可以重置重定向保护的变量(mRedirectProtected)，当然也可以在页面加载前重置，由于历史遗留代码问题，此处尚未省去优化。
- 加载页面的过程中回调哪些方法？
    - WebChromeClient.onReceivedTitle(webview, title)，用来设置标题。需要注意的是，在部分Android系统版本中可能会回调多次这个方法，而且有时候回调的title是一个url，客户端可以针对这种情况进行特殊处理，避免在标题栏显示不必要的链接。
    - WebChromeClient.onProgressChanged(webview, progress)，根据这个回调，可以控制进度条的进度（包括显示与隐藏）。一般情况下，想要达到100%的进度需要的时间较长（特别是首次加载），用户长时间等待进度条不消失必定会感到焦虑，影响体验。其实当progress达到80的时候，加载出来的页面已经基本可用了。事实上，国内厂商大部分都会提前隐藏进度条，让用户以为网页加载很快。
    - WebViewClient.shouldInterceptRequest(webview, request)，无论是普通的页面请求(使用GET/POST)，还是页面中的异步请求，或者页面中的资源请求，都会回调这个方法，给开发一次拦截请求的机会。在这个方法中，我们可以进行静态资源的拦截并使用缓存数据代替，也可以拦截页面，使用自己的网络框架来请求数据。包括后面介绍的WebView免流方案，也和此方法有关。
    - WebViewClient.shouldOverrideUrlLoading(webview, request)，如果遇到了重定向，或者点击了页面中的a标签实现页面跳转，那么会回调这个方法。可以说这个是WebView里面最重要的回调之一，后面WebView与Native页面交互一节将会详细介绍这个方法。
    - WebViewClient.onReceivedError(webview,handler,error)，加载页面的过程中发生了错误，会回调这个方法。主要是http错误以及ssl错误。在这两个回调中，我们可以进行异常上报，监控异常页面、过期页面，及时反馈给运营或前端修改。在处理ssl错误时，遇到不信任的证书可以进行特殊处理，例如对域名进行判断，针对自己公司的域名“放行”，防止进入丑陋的错误证书页面。也可以与Chrome一样，弹出ssl证书疑问弹窗，给用户选择的余地。
- 加载页面结束回调哪些方法
    - 会回调WebViewClient.onPageFinished(webview,url)。
    - 这时候可以根据回退栈的情况判断是否显示关闭WebView按钮。通过mActivityWeb.canGoBackOrForward(-1)判断是否可以回退。



### 12.js的调用时机分析
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



### 13.如何使用DeepLink
- 假设一个场景：
    - 小明告诉小新，一鹿有车APP上有一个很有创意的抽奖活动，小新想要参与这个活动：
        - 如果小新已经安装了APP，他需要找到且打开APP，然后找到相应的活动，共计2步；
        - 如果小新没有安装APP，他需要在应用市场搜索一鹿有车APP、下载、打开APP且找到相应的活动，共计4步；
- 什么是DeepLink
    - Deep Link，又叫deep linking，中文翻译作深层链接。简单地从用户体验来讲，Deep Link，就是可以让你在手机的浏览器/Google Search上点击搜索的结果，便能直接跳转到已安装的应用中的某一个页面的技术。
- 什么是Deferred DeepLink
    - 相比DeepLink，它增加了判断APP是否被安装，用户匹配的2个功能；
        - 1.当用户点击链接的时候判断APP是否安装，如果用户没有安装时，引导用户跳转到应用商店下载应用。
        - 2.用户匹配功能，当用户点击链接时和用户启动APP时，分别将这两次用户Device Fingerprint（设备指纹信息）传到服务器进行模糊匹配，使用户下载且启动APP时，直接打开相应的指定页面。
- 什么是AppLink
    - AppLink相对复杂，需要App与Web协作完成系统验证，但可以保证直接唤起目标App，无需用户二次选择或确认
- DeepLink和AppLink用到的核心技术
    - URL SCHEMES。不论是IOS还是Android。
    - 比如微信：URL Schemes：weixin://dl/moments（打开微信朋友圈）
    - DeepLink与AppLink，本质上都是基于Intent框架，使App能够识别并处理来自系统或其他App的某种特殊URL，在原生App之间相互跳转，实现良好的用户体验
- 如何实现DeepLink实践该方案
    - 1.指定scheme跳转规则，比如暂时是这样设定的：yilu://link/?page=main
    - 2.被唤起方，客户端需要配置清单文件activity。关于SchemeActivity注意查看下面代码：
        ```
        <!--用于DeepLink，html跳到此页面  scheme_Adr: 'yilu://link/?page=main',-->
        <activity android:name=".activity.link.SchemeActivity"
            android:screenOrientation="portrait">
            <!--Android 接收外部跳转过滤器-->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <!-- 协议部分配置 ,要在web配置相同的-->
                <!--yilu://link/?page=main-->
                <data
                    android:host="link"
                    android:scheme="yilu" />
            </intent-filter>
        </activity>
        
        //解析数据
        @Override
        public void onCreate(Bundle savesInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
    
            Intent intent=getIntent();
            String action=intent.getAction();
            Uri data=intent.getData();
    
            //解析data
            String scheme=data.getScheme();
            String host=data.getHost();
            String path=data.getPath();
            int port=data.getPort();
            Set<String> paramKeySet=data.getQueryParameterNames();
        }
        ```
    - 3.唤起方也需要操作
    ```
    Intent intent=new Intent();
    intent.setData(Uri.parse("yilu://link/?page=main"));
    startActivity(intent);
    ```
- 如何避免通过deep link打开多个应用实例
    - http://stackoverflow.com/a/25997627
- 具体可以看这篇文章：https://www.jianshu.com/p/127c80f62655





### 14.应用被作为第三方浏览器打开
- 微信里的文章页面，可以选择“在浏览器打开”。现在很多应用都内嵌了WebView，那是否可以使自己的应用作为第三方浏览器打开此文章呢？
- 在Manifest文件中，给想要接收跳转的Activity添加<intent-filter>配置：
    ```
    <activity
        android:name=".X5WebViewActivity"
        android:configChanges="orientation|screenSize"
        android:hardwareAccelerated="true"
        android:launchMode="singleTask"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <!--需要添加下面的intent-filter配置-->
        <intent-filter tools:ignore="AppLinkUrlError">
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <!--使用http，则只能打开http开头的网页-->
            <data android:scheme="https" />
        </intent-filter>
    </activity>
    ```
- 然后在 X5WebViewActivity 中获取相关传递数据。具体可以看lib中的X5WebViewActivity类代码。
    ```
    public class X5WebViewActivity extends AppCompatActivity {
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_web_view);
            getIntentData();
            initTitle();
            initWebView();
            webView.loadUrl(mUrl);
           // 处理 作为三方浏览器打开传过来的值
            getDataFromBrowser(getIntent());
        }
    
       /**
         * 使用singleTask启动模式的Activity在系统中只会存在一个实例。
         * 如果这个实例已经存在，intent就会通过onNewIntent传递到这个Activity。
         * 否则新的Activity实例被创建。
         */
        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            getDataFromBrowser(intent);
        }
    
        /**
         * 作为三方浏览器打开传过来的值
         * Scheme: https
         * host: www.jianshu.com
         * path: /p/yc
         * url = scheme + "://" + host + path;
         */
        private void getDataFromBrowser(Intent intent) {
            Uri data = intent.getData();
            if (data != null) {
                try {
                    String scheme = data.getScheme();
                    String host = data.getHost();
                    String path = data.getPath();
                    String text = "Scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path;
                    Log.e("data", text);
                    String url = scheme + "://" + host + path;
                    webView.loadUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    ```
- 一些重点说明
    - 在微信中“通过浏览器”打开自己的应用，然后将自己的应用切到后台。重复上面的操作，会一直创建应用的实例，这样肯定是不好的，为了避免这种情况我们设置启动模式为：launchMode="singleTask"。



### 15.理解WebView独立进程
- WebView实例在Android7.0系统以后，已经可以选择运行在一个独立进程上7；8.0以后默认就是运行在独立的沙盒进程中。
- Android7.0系统以后，WebView相对来说是比较稳定的，无论承载WebView的容器是否在主进程，都不需要担心WebView崩溃导致应用也跟着崩溃。然后7.0以下的系统就没有这么幸运了，特别是低版本的WebView。考虑应用的稳定性，我们可以把7.0以下系统的WebView使用一个独立进程的Activity来包装，这样即使WebView崩溃了，也只是WebView所在的进程发生了崩溃，主进程还是不受影响的。
    - 该方案是考拉的分享，具体内容可以看这篇文章。[如何设计一个优雅健壮的Android WebView](https://blog.klmobile.app/2018/02/27/design-an-elegant-and-powerful-android-webview-part-two/)
    ```
    public static Intent getWebViewIntent(Context context) {
        Intent intent;
        if (isWebInMainProcess()) {
            intent = new Intent(context, MainWebviewActivity.class);
        } else {
            intent = new Intent(context, WebviewActivity.class);
        }
        return intent;
    }
    
    public static boolean isWebInMainProcess() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;
    }
    ```


### 16.使用外部浏览器下载
- 比如，打开一个h5的链接，遇到下载链接，即跳转到外部浏览器打开；其他的则在webView中打开。如何操作呢？代码如下所示：
    ```
    private class MyX5WebViewClient extends X5WebViewClient {
        public MyX5WebViewClient(X5WebView webView, Context context) {
            super(webView, context);
        }
    
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            boolean activityAlive = X5WebUtils.isActivityAlive(WebViewActivity.this);
            if (!activityAlive){
                return false;
            }
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            //如果是返回数据
            if (url.contains(".apk")) {
                //为false调用系统浏览器或第三方浏览器
                Uri issuesUrl = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
                WebViewActivity.this.startActivity(intent);
                return false;
            }else {
                mWebView.loadUrl(url);
                return true;
            }
            //return super.shouldOverrideUrlLoading(webView, url);
        }
    
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            boolean activityAlive = X5WebUtils.isActivityAlive(WebViewActivity.this);
            if (!activityAlive){
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String url = request.getUrl().toString();
                if (TextUtils.isEmpty(url)) {
                    return false;
                }
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                //如果是返回数据
                if (url.contains(".apk")) {
                    //为false调用系统浏览器或第三方浏览器
                    Uri issuesUrl = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
                    WebViewActivity.this.startActivity(intent);
                    return false;
                }else {
                    mWebView.loadUrl(url);
                    return true;
                }
            }else {
                return super.shouldOverrideUrlLoading(webView, request);
            }
        }
    }
    ```


### 17.tel,sms等协议用法
- 网页中tel,sms,mailTo,Intent,Market协议，那么他们分别都是怎么用的呢
- tel:协议---拨打电话
    - 在html中
    ```
    <a href="tel:13667225184">电话给我</a>
    ```
    - 在java中
    ```
    //tel:协议---拨打电话
    if(url.startsWith("tel:")) {
        //直接调出界面，不需要权限
        Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
        startActivity(sendIntent);
        //或者
        //直接拨打，需要权限<uses-permission android:name="android.permission.CALL_PHONE"/>
        //Intent sendIntent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        //startActivity(sendIntent);
        //否则键盘回去，页面显示"找不到网页"
        return true;
    }
    ```
- sms:协议---发送短信
    - 在html中
    ```
    <a href="sms:">调出发短信界面</a></br>
    <a href="sms:13667225184">调出发短信界面显示号码</a></br>
    <a href="sms:13667225184?body=contents">调出发短信界面显示号码和发送内容</a></br>
    <a href="sms:13667225184&body=contents1">ios调出发短信界面显示号码和发送内容</a></br>
    <a href="sms:13667225184;10010?body=contents2">调出发短信界面给多个号码发内容</a><br/>
    <a href="sms:+13667225184?body=contents3">调出发短信界面显示号码 </a></br>
    <a href="sms:+13667225184;10010?body=contents4">调出发短信界面给多个号码发内容 </a><br/>
    ```
    - 在java中
    ```
    if(url.startsWith("sms:")||url.startsWith("smsto:")||url.startsWith("mms:")||url.startsWith("mmsto:")) {
        //直接调出界面，不需要权限
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(sendIntent);
    
        //或者
        //打开短信页面，不需要权限
        //Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
        //startActivity(sendIntent);
    
        //或者
        //import android.telephony.SmsManager;
        //SmsManager smsg = SmsManager.getDefault();//----看不到已发送信息。。。
        //smsg.sendTextMessage("10086", null, "tttttt", null, null);
        
        //或者
        //---可以看到已发的信息
        //ContentValues values = new ContentValues(); 
        //values.put("address", "10086");
        //values.put("body", "contents");
        //ContentResolver contentResolver = getContentResolver();
        //contentResolver.insert(Uri.parse("content://sms/sent"), values);
        // contentResolver.insert(Uri.parse("content://sms/inbox"), values);
        //<uses-permission android:name="android.permission.SEND_SMS"/>
        //<uses-permission android:name="android.permission.READ_SMS"/>
        //<uses-permission android:name="android.permission.WRITE_SMS"/>
        //否则键盘回去，页面显示"找不到网页"
        return true;
    }
    ```
- mailto:协议---发送邮件
    - 在html中
    ```
    <a href="mailto:yangchong211@163.com">邮件</a>
    ```
    - 在java中
    ```
    if (url.startsWith("mailto:")) {
        //打开发邮件窗口
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
        startActivity(mailIntent);
        //<uses-permission android:name="android.permission.SEND_TO"/>
        return true;
    }
    ```



### 29.关于拦截处理注意要点



### 30.FileChooser文件处理
- https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650238618&idx=1&sn=1b791c0df4e48b317b986e7cbb07bb1d&chksm=88639ff5bf1416e3b7b50879677dc1ff4da1a833f3bba9b0abc52ee21b81a89ce1d863bec7a1&scene=38#wechat_redirect






