package com.ycbjie.ycwebview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ycbjie.ycwebview.ten.TenActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_1).setOnClickListener(this);
        findViewById(R.id.tv_2_1).setOnClickListener(this);
        findViewById(R.id.tv_2_2).setOnClickListener(this);
        findViewById(R.id.tv_3).setOnClickListener(this);
        findViewById(R.id.tv_5).setOnClickListener(this);
        findViewById(R.id.tv_6).setOnClickListener(this);
        findViewById(R.id.tv_7).setOnClickListener(this);
        findViewById(R.id.tv_8).setOnClickListener(this);
        findViewById(R.id.tv_9).setOnClickListener(this);
        findViewById(R.id.tv_10).setOnClickListener(this);
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
            case R.id.tv_3:
                startActivity(new Intent(this,ThreeActivity.class));
                break;
            case R.id.tv_5:
                startActivity(new Intent(this,FiveActivity.class));
                break;
            case R.id.tv_6:
                startActivity(new Intent(this,SixActivity.class));
                break;
            case R.id.tv_7:
                startActivity(new Intent(this,NativeActivity.class));
                break;
            case R.id.tv_8:
                startActivity(new Intent(this,EightActivity.class));
                break;
            case R.id.tv_9:
                startActivity(new Intent(this,FileDisplayActivity.class));
                break;
            case R.id.tv_10:
                startActivity(new Intent(this, TenActivity.class));
                break;
            default:
                break;
        }
    }
}
