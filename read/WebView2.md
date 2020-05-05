#### 基础使用目录介绍
- 11.什么是302/303重定向
- 12.301/302业务场景白屏描述
- 13.301/302业务白屏解决方案
- 14.301/302回退栈问题描述
- 15.301/302回退栈问题解决方案
- 16.如何设置WebView触摸点击事件
- 17.如何用代码判断是否重定向
- 18.如何解决重定向回退栈问题
- 19.shouldOverrideUrlLoading



### 12.301/302业务场景白屏描述
- https://iluhcm.com/2017/12/10/design-an-elegant-and-powerful-android-webview-part-one/



### 13.301/302回退栈问题描述



### 15.301/302回退栈如何处理
- https://iluhcm.com/2017/12/10/design-an-elegant-and-powerful-android-webview-part-one/





### 16.如何设置WebView触摸点击事件
- 如何在android中的webView上获得onclick事件
    -  WebView似乎没有发送点击事件OnClickListener
- 如何设置触摸事件



### 17.如何用代码判断是否重定向
- 遇到问题说明
    - 用WebView调用webView.loadUrl(url)加载某个网页。点击WebView的某个链接跳转下一个页面。但是下面这样做，会出现问题；
    - 如果webView第一次加载的url重定向到了另一个地址，此时也会走shouldOverrideUrlLoading的回调。这样一来，出现的现象就是WebView是空的，直接打开了浏览器。
    ```
    webView.setWebViewClient(new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    view.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
        }
    });
    ```
- 解决问题分析思考
    - 要解决这个问题，很容易想到的解决方案是找找WebView有没有对重定向的判断方法，如果有的话，我们就可以对重定向的回调另外处理。
    - 很不幸，WebView并没有提供相应的方法。是不是就没办法处理了呢？当然不是。
- 第一种解决方案案例
    - WebView有一个getHitTestResult():返回的是一个HitTestResult，一般会根据打开的链接的类型，返回一个extra的信息，如果打开链接不是一个url，或者打开的链接是JavaScript的url，他的类型是UNKNOWN_TYPE，这个url就会通过requestFocusNodeHref(Message)异步重定向。返回的extra为null，或者没有返回extra。根据此方法的返回值，判断是否为null，可以用于解决网页重定向。
    - 该方案有个缺陷是，如果遇到的需求是前言描述的那样，二正好点击的链接发生了重定向，就不会在另一个页面打开，而是直接在当前的WebView里了。
    ```
    webView.setWebViewClient(new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //判断重定向的方式一
            WebView.HitTestResult hitTestResult = view.getHitTestResult();
            if(hitTestResult == null) {
                return false;
            }
            if(hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
                return false;
            }
    
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                view.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }
    });
    ```
- 第二种解决方案案例
    - WebView在加载一个页面开始的时候会回调onPageStarted方法，在该页面加载完成之后会回调onPageFinished方法。而如果该链接发生了重定向，回调shouldOverrideUrlLoading会在回调onPageFinished之前。
    - 有了这个前提，我们就可以加一个mIsPageLoading的标记，在onPageStarted回调的时候置为true，在onPageFinished回调的时候置为false。在shouldOverrideUrlLoading里面就可以判断该标记，如果为true，则表示该回调是重定向，否则直接打开浏览器。代码如下：
    ```
    private boolean mIsPageLoading;
    //代码省略
    webView.setWebViewClient(new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //判断重定向的方式二
            if(mIsPageLoading) {
                return false;
            }
    
            if(url != null && url.startsWith("http")) {
                webView.loadUrl(url);
                return true;
            } else {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    view.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
    
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mIsPageLoading= true;
            Log.d(TAG, "onPageStarted");
        }
    
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mIsPageLoading= false;
            Log.d(TAG, "onPageFinished");
        }
    });
    ```
    - 该方案也会产生另一个问题：当页面没有全部加载完之前，加载出来的部分页面的链接也是可以点击的。这样一来在shouldOverrideUrlLoading里面本来是对链接的点击也会被当成重定向链接在当前的WebView里面打开。
    - 要避免这种情况，只能在页面完全加载出来之前禁止WebView的点击。
    ```
    webView.setOnTouchListener(new WebViewTouchListener());
    //代码省略
    private class WebViewTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return !mIsLoading;
        }
    }
    ```


### 18.如何解决重定向回退栈问题
- 重定向回退栈问题描述
    - webView执行goBack为什么不能返回上一页面，而为什么有的网页可以返回上一个页面呢？这到底是什么原因导致的这个问题呢？
    - 是因为web页面在被打开的时候是以url1打开，一部分网页是执行了重定向，那么它就会定向到另外一个url2地址上面去，导致你goback返回是返回了，当它从url3返回的时候其实并不是跳转到url2，而是直接返回到url1，而跳转到url1，又因为url1是打开后直接进行重定向的，那么就直接又跳转到url2了,所以会一直循环执行。所以你退不出去。
    - 而另外一部分是可以退回上一个页面是因为这些页面没有重定向的操作。所以会直接退回到上一个面。
- 解决方案
    - https://www.kanzhun.com/jiaocheng/212017.html



### 19.shouldOverrideUrlLoading
- 首先看一下shouldOverrideUrlLoading方法
    - 这个方法中可以做拦截，主要的作用是处理各种通知和请求事件。
    - 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器。
- 有哪些几个方法，之间有何区别
    - boolean shouldOverrideUrlLoading(WebView view, String url)
    - boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) 
- 具体看
    - https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650237226&idx=1&sn=d7d434b8644bb9543485ce81226125e5&chksm=88639845bf141153b265ee26b39aa8a2ef74248e010b568b288cffc0726012f8bce6f5a675bf&scene=38#wechat_redirect



