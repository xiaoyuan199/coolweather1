package com.coolweather1.android;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;

import cn.sharesdk.framework.ShareSDK;


public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShareSDK.initSDK(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示
        // 设置切换动画从左边进入，从右边退出
        overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        setContentView(R.layout.mian);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent intent=new Intent(Main.this,MainActivity.class);
                    startActivity(intent);
                    finish();


            }
        },2000);


    }

}
