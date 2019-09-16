package com.tttangerine.availableseat.activity;

import android.app.Activity;
import android.os.Bundle;

import com.tttangerine.availableseat.R;

public class UseInfoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_info);
    }

    //监听回退键
    @Override
    public void onBackPressed() {
        finish();
    }

}
