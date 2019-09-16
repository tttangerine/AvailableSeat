package com.tttangerine.availableseat.activity;

import android.app.Activity;
import android.os.Bundle;

import com.tttangerine.availableseat.R;

public class AppInfoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
    }

    //监听返回键,销毁activity
    @Override
    public void onBackPressed() {
        finish();
    }

}
