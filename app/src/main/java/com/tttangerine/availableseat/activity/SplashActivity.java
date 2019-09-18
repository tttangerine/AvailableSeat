package com.tttangerine.availableseat.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.User;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobUser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tttangerine.availableseat.db.User.USER_LEAVING;
import static com.tttangerine.availableseat.db.User.USER_NONE;
import static com.tttangerine.availableseat.db.User.USER_USING;

public class SplashActivity extends Activity {

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        bingPicImg = findViewById(R.id.splash_header);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://guolin.tech/api/bing_pic").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = Objects.requireNonNull(response.body()).string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(SplashActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(SplashActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });

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
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                finish();
            }
        }, 3000);

    }
}
