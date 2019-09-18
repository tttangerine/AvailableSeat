package com.tttangerine.availableseat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.Room;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class HomepageActivity extends Activity implements View.OnClickListener {

    //创建activity本身的实例，供其他activity调用
    @SuppressLint("StaticFieldLeak")
    public static HomepageActivity instance = null;

    //侧边栏
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    private Toast toast = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.activity_homepage);
        instance = this;

        /*
          注册视图控件
         */
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Button home_refresh = findViewById(R.id.home_refresh);
        Button btn_nav = findViewById(R.id.btn_nav);
        Button home_select = findViewById(R.id.home_select);
        Button home_book = findViewById(R.id.home_book);
        CardView home_card1 = findViewById(R.id.home_card1);
        CardView home_card2 = findViewById(R.id.home_card2);

        /*
         * 注册按键监听
         */
        btn_nav.setOnClickListener(this);
        home_select.setOnClickListener(this);
        home_book.setOnClickListener(this);
        home_refresh.setOnClickListener(this);
        home_card1.setOnClickListener(this);
        home_card2.setOnClickListener(this);

        //处理侧边栏点击事件，跳转到相应activity
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){

                    case R.id.nav_change_name:
                        Intent name_intent = new Intent(HomepageActivity.this,
                                LoginActivity.class);
                        //使用intent传递信息，说明信息更改类型，用于更改页面布局
                        name_intent.putExtra("change_type", 0);
                        startActivity(name_intent);
                        break;

                    case R.id.nav_change_pw:
                        Intent pw_intent = new Intent(HomepageActivity.this,
                                LoginActivity.class);
                        pw_intent.putExtra("change_type", 1);
                        startActivity(pw_intent);
                        break;

                    case R.id.nav_use_info:
                        Intent useInfo_intent = new Intent(HomepageActivity.this,
                                UseInfoActivity.class);
                        startActivity(useInfo_intent);
                        break;

                    case R.id.nav_app_info:
                        Intent appInfo_intent = new Intent(HomepageActivity.this,
                                AppInfoActivity.class);
                        startActivity(appInfo_intent);
                        break;

                }
                return false;
            }
        });
        refreshUser();

        refreshCard();

    }

    public void onClick(View v){
        switch (v.getId()){

            case R.id.btn_nav:
                //点击按钮展开侧边栏
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.home_select:
                Intent intent_select = new Intent(HomepageActivity.this,
                        ChoosePositionActivity.class);
                intent_select.putExtra("btn_type", 1);
                startActivity(intent_select);
                break;

            case R.id.home_book:
                Intent intent_book = new Intent(HomepageActivity.this,
                        ChoosePositionActivity.class);
                intent_book.putExtra("btn_type", 0);
                startActivity(intent_book);
                break;

            case R.id.home_refresh:
                refreshCard();
                showToast("刷新成功");
                break;

            case R.id.home_card1:

                //点击卡片跳转到相应教室的选座页面
                roomQuery().findObjects(new FindListener<Room>() {
                    @Override
                    public void done(List<Room> list, BmobException e) {

                        if (list != null){
                            Intent card1 = new Intent(HomepageActivity.this,
                                    ChooseSeatActivity.class);
                            card1.putExtra("room_id", list.get(0).getObjectId());
                            card1.putExtra("btn_type", 0);
                            startActivity(card1);
                        } else {
                            showToast(e.getMessage());
                        }

                    }
                });

                break;

            case R.id.home_card2:

                roomQuery().findObjects(new FindListener<Room>() {
                    @Override
                    public void done(List<Room> list, BmobException e) {

                        if (list != null){
                            Intent card2 = new Intent(HomepageActivity.this,
                                    ChooseSeatActivity.class);
                            card2.putExtra("room_id", list.get(1).getObjectId());
                            card2.putExtra("btn_type", 0);
                            startActivity(card2);
                        } else {
                            showToast(e.getMessage());
                        }

                    }
                });

                break;

        }
    }

    public void refreshCard(){

        //按空座数降序查询教室，取前两个更新至首页卡片
        roomQuery().findObjects(new FindListener<Room>() {
            @Override
            public void done(List<Room> list, BmobException e) {

                if (list != null){
                    TextView c1b = findViewById(R.id.card1_building);
                    TextView c1r = findViewById(R.id.card1_room);
                    TextView c1s = findViewById(R.id.card1_seats);
                    c1b.setText(list.get(0).getBuildingName());
                    c1r.setText(list.get(0).getRoomName());
                    c1s.setText(String.format(getResources().getString
                            (R.string.card_auto),list.get(0).availableSeatNum));

                    TextView c2b = findViewById(R.id.card2_building);
                    TextView c2r = findViewById(R.id.card2_room);
                    TextView c2s = findViewById(R.id.card2_seats);
                    c2b.setText(list.get(1).getBuildingName());
                    c2r.setText(list.get(1).getRoomName());
                    c2s.setText(String.format(getResources().getString
                            (R.string.card_auto),list.get(1).availableSeatNum));
                }

                if (e != null){
                    showToast(e.getMessage());
                }

            }
        });
    }

    /**
     * 更新侧边栏用户信息
     */
    public void refreshUser(){

        String username = (String)BmobUser.getObjectByKey("username");
        Integer fault = (Integer)BmobUser.getObjectByKey("fault");

        View headerLayout = navigationView.getHeaderView(0);

        TextView nav_title = headerLayout.findViewById(R.id.nav_title);
        TextView nav_subtitle = headerLayout.findViewById(R.id.nav_subtitle);

        nav_title.setText(username);
        nav_subtitle.setText(String.format(getResources().getString
                (R.string.nav_header_subtitle), fault));

    }

    /**
     * 空座数降序查询教室
     */
    private BmobQuery<Room> roomQuery(){
        BmobQuery<Room> roomBmobQuery = new BmobQuery<>();
        roomBmobQuery.order("-availableSeatNum");
        return roomBmobQuery;
    }

    /**
     * 快速通知
     */
    @SuppressLint("ShowToast")
    private void showToast(String msg){
        if (toast == null){
            toast = Toast.makeText(HomepageActivity.this, msg, Toast.LENGTH_SHORT);
        } else {
            View view = toast.getView();
            toast.cancel();
            toast = new Toast(this);
            toast.setView(view);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(msg);
        }
        toast.show();
    }

}
