package com.ycbjie.ycwebview;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import com.tencent.smtt.sdk.ValueCallback;
import com.ycbjie.webviewlib.inter.DefaultWebListener;
import com.ycbjie.webviewlib.utils.ToastUtils;
import com.ycbjie.webviewlib.utils.X5LogUtils;
import com.ycbjie.webviewlib.utils.X5WebUtils;
import com.ycbjie.webviewlib.view.X5WebView;
import com.ycbjie.webviewlib.widget.WebProgress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/17
 *     desc  : webView页面
 *     revise: 暂时先用假数据替代
 * </pre>
 */
public class TestWebViewActivity extends AppCompatActivity {

    private X5WebView mWebView;
    private WebProgress progress;
    private String url;
    private LinkedHashMap<Integer,String> hashMap = new LinkedHashMap<>();
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private ArrayList<String> urls = new ArrayList<>();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() ==
                KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mWebView.pageCanGoBack()) {
                //退出网页
                return mWebView.pageGoBack();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            //mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.stop();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yc_web_view);
        initData();
        initView();
        initListener();
    }

    private void initListener() {
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initData() {
        Intent intent = getIntent();
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=dd4c506de93551a8bf2439612d80e3b7&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=0f89385c4205377ef2442ed0969e0b9c&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=8b9bdf976a8aba1969a151c494ef3139&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d21e8abce7413b7983a2dd529415d3ea&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d3b1128f5eff790a0ba239a63b92c4ab&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d2aa3868136e67056de4d1f84b11f9ba&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=3f40295ef7f661c984ae4380aea789e6&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=84dd6a4782397e436306cd2f7f71e1e2&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=a002881fe33c4ccc41a405791c50d333&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=dd4c506de93551a8bf2439612d80e3b7&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=0f89385c4205377ef2442ed0969e0b9c&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=8b9bdf976a8aba1969a151c494ef3139&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d21e8abce7413b7983a2dd529415d3ea&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d3b1128f5eff790a0ba239a63b92c4ab&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=d2aa3868136e67056de4d1f84b11f9ba&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=3f40295ef7f661c984ae4380aea789e6&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=84dd6a4782397e436306cd2f7f71e1e2&hideNativeTitleBar=1");
        urls.add("https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=a002881fe33c4ccc41a405791c50d333&hideNativeTitleBar=1");
    }

    public void initView() {
        mWebView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorAccent),this.getResources().getColor(R.color.colorPrimaryDark));

        url = urls.get(atomicInteger.get());
        mWebView.setFidderOpen(true);
        mWebView.loadUrl(url);
        mWebView.getX5WebChromeClient().setWebListener(interWebListener);
        mWebView.getX5WebViewClient().setWebListener(interWebListener);
        if (!X5WebUtils.isConnected(this)){
            ToastUtils.showRoundRectToast("请先连接上网络");
        }
        mWebView.clearAllWebViewCache(true);
    }


    private DefaultWebListener interWebListener = new DefaultWebListener() {
        @Override
        public void hindProgressBar() {
            progress.hide();
            //setJavascript("ugc-content");
        }

        @Override
        public void showErrorView(@X5WebUtils.ErrorType int type) {
            switch (type){
                //没有网络
                case X5WebUtils.ErrorMode.NO_NET:
                    break;
                //404，网页无法打开
                case X5WebUtils.ErrorMode.STATE_404:

                    break;
                //onReceivedError，请求网络出现error
                case X5WebUtils.ErrorMode.RECEIVED_ERROR:

                    break;
                //在加载资源时通知主机应用程序发生SSL错误
                case X5WebUtils.ErrorMode.SSL_ERROR:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void startProgress(int newProgress) {
            progress.setWebProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }

        @Override
        public void onPageFinished(String url) {
            setJavascript("ugc-content");
        }
    };

    public void setJavascript(final String className) {
        try {
            X5LogUtils.i("抓包返回数据---setJavascript");
            //https://h5.zybang.com/plat/app-vue/ugcDetail.html?articleId=0f89385c4205377ef2442ed0969e0b9c&hideNativeTitleBar=1
            //定义javaScript方法
            //定义javaScript方法
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String javascript = "javascript:function getDivContent() { "
                            + "return document.getElementsByClassName('" + className + "')[0].innerText; }";
                    String javaScript = "javascript:function getDivContent() { "
                            + "return document.getElementsByClassName(\"ugc-content\")[0].innerText; }";
                    String javascript3 = "javascript:function getDivContent() { "
                            + "return document.getElementsByClassName(\"ugc-content\")[0]; }";
                    mWebView.loadUrl(javaScript);
                    //mWebView.loadUrl("javascript:getDivContent();");
                    mWebView.evaluateJavascript("javascript:getDivContent();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            //此处为 js 返回的结果
                            X5LogUtils.i("抓包返回数据---"+s);
                            if (s!=null && s.length()>0 && !s.equals("null")){
                                atomicInteger.getAndIncrement();
                                hashMap.put(atomicInteger.get(),s);
                                url = urls.get(atomicInteger.get());
                                X5LogUtils.i("抓包返回数据---atomicInteger---"+atomicInteger.get() +"------"+url);
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (atomicInteger.get()+1 >= urls.size()){
                                        ToastUtils.showRoundRectToast("加载完成");
                                        return;
                                    }
                                    mWebView.loadUrl(url);
                                    mWebView.reLoadView();
                                }
                            },2000);
                        }
                    });
                }
            },1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test(){
        //夜间模式，enable:true(日间模式)，enable：false（夜间模式）
        mWebView.setDayOrNight(true);
        //前进后退缓存，true表示缓存
        mWebView.setContentCacheEnable(true);
        //对于无法缩放的页面当用户双指缩放时会提示强制缩放，再次操作将触发缩放功能
        mWebView.setForcePinchScaleEnabled(true);
        //设置无痕模式
        mWebView.setShouldTrackVisitedLinks(true);
        //刘海屏适配
        mWebView.setDisplayCutoutEnable(true);
        //一次性删除所有缓存
        mWebView.clearAllWebViewCache(true);
        //缓存清除，针对性删除
        mWebView.clearCache();
    }
}
