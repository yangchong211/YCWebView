package com.ycbjie.ycwebview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_1).setOnClickListener(this);
        findViewById(R.id.tv_2_1).setOnClickListener(this);
        findViewById(R.id.tv_2_2).setOnClickListener(this);
        findViewById(R.id.tv_2_3).setOnClickListener(this);
        findViewById(R.id.tv_2_4).setOnClickListener(this);
        findViewById(R.id.tv_3).setOnClickListener(this);
        findViewById(R.id.tv_5).setOnClickListener(this);
        findViewById(R.id.tv_5_2).setOnClickListener(this);
        findViewById(R.id.tv_6_1).setOnClickListener(this);
        findViewById(R.id.tv_6_2).setOnClickListener(this);
        findViewById(R.id.tv_7_1).setOnClickListener(this);
        findViewById(R.id.tv_7_2).setOnClickListener(this);
        findViewById(R.id.tv_7_3).setOnClickListener(this);
        findViewById(R.id.tv_8).setOnClickListener(this);
        findViewById(R.id.tv_9).setOnClickListener(this);
        findViewById(R.id.tv_9_2).setOnClickListener(this);
        findViewById(R.id.tv_10).setOnClickListener(this);
        findViewById(R.id.tv_11).setOnClickListener(this);
        findViewById(R.id.tv_12).setOnClickListener(this);
        findViewById(R.id.tv_13).setOnClickListener(this);
        findViewById(R.id.tv_14).setOnClickListener(this);
        findViewById(R.id.tv_14_2).setOnClickListener(this);
        findViewById(R.id.tv_15).setOnClickListener(this);
        findViewById(R.id.tv_16).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_1:
                startActivity(new Intent(this,FirstActivity.class));
                break;
            case R.id.tv_2_1:
                startActivity(new Intent(this,SecondActivity.class));
                break;
            case R.id.tv_2_2:
                startActivity(new Intent(this,WebViewActivity.class));
                break;
            case R.id.tv_2_3:
                startActivity(new Intent(this, CacheWebViewActivity1.class));
                break;
            case R.id.tv_2_4:
                startActivity(new Intent(this, CacheWebViewActivity2.class));
                break;
            case R.id.tv_3:
                startActivity(new Intent(this,ThreeActivity.class));
                break;
            case R.id.tv_5:
                startActivity(new Intent(this,FiveActivity.class));
                break;
            case R.id.tv_5_2:
                startActivity(new Intent(this,FiveActivity2.class));
                break;
            case R.id.tv_6_1:
                startActivity(new Intent(this,SixActivity.class));
                break;
            case R.id.tv_6_2:
                startActivity(new Intent(this,SixActivity2.class));
                break;
            case R.id.tv_7_1:
                startActivity(new Intent(this,NativeActivity.class));
                break;
            case R.id.tv_7_2:
                startActivity(new Intent(this,NativeActivity2.class));
                break;
            case R.id.tv_7_3:
                startActivity(new Intent(this,NativeActivity3.class));
                break;
            case R.id.tv_8:
                startActivity(new Intent(this,EightActivity.class));
                break;
            case R.id.tv_9:
                startActivity(new Intent(this,FileDisplayActivity.class));
                break;
            case R.id.tv_9_2:
                startActivity(new Intent(this,DeepLinkActivity.class));
                break;
            case R.id.tv_10:
                startActivity(new Intent(this, TenActivity.class));
                break;
            case R.id.tv_11:
                openLink(this,"https://juejin.im/user/5939433efe88c2006afa0c6e/posts");
                break;
            case R.id.tv_12:
                startActivity(new Intent(this, DownActivity.class));
                break;
            case R.id.tv_13:
                startActivity(new Intent(this, ScrollViewActivity.class));
                break;
            case R.id.tv_14:
                startActivity(new Intent(this, VertcalWebActivity.class));
                break;
            case R.id.tv_14_2:
                startActivity(new Intent(this, WebViewActivity2.class));
                break;
            case R.id.tv_15:
                startActivity(new Intent(this, WvNativeActivity2.class));
                break;
            case R.id.tv_16:
                startActivity(new Intent(this, WvNativeActivity3.class));
                break;
            default:
                break;
        }
    }

    /**
     * 使用浏览器打开链接
     */
    public void openLink(Context context, String content) {
        if (!TextUtils.isEmpty(content) && content.startsWith("http")) {
            Uri issuesUrl = Uri.parse(content);
            Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
            context.startActivity(intent);
        }
    }

}
