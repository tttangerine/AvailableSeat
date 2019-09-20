package com.tttangerine.availableseat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.WindowManager;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.User;


import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobUser;

import static com.tttangerine.availableseat.db.User.USER_LEAVING;
import static com.tttangerine.availableseat.db.User.USER_NONE;
import static com.tttangerine.availableseat.db.User.USER_USING;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        BmobConfig config = new BmobConfig.Builder(this)
                .setApplicationId("19d07a8c2eb75e6db9d59c4137b8ff5a")
                .setConnectTimeout(15)
                .build();
        Bmob.initialize(config);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断当前是否有用户登录，若登录则跳转主页，没有则跳转登录页面
                if (!BmobUser.isLogin()) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else if (BmobUser.getCurrentUser(User.class).USER_STATE == USER_NONE){
                    Intent intent = new Intent(SplashActivity.this, HomepageActivity.class);
                    startActivity(intent);
                } else if (BmobUser.getCurrentUser(User.class).USER_STATE == USER_LEAVING){
                    Intent intent = new Intent(SplashActivity.this, TimerActivity.class);
                    intent.putExtra("timer_type",0);
                    startActivity(intent);
                } else if (BmobUser.getCurrentUser(User.class).USER_STATE == USER_USING){
                    Intent intent = new Intent(SplashActivity.this, TimerActivity.class);
                    intent.putExtra("timer_type",1);
                    startActivity(intent);
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                finish();
            }
        }, 3000);

    }
}
