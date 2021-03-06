package com.tttangerine.availableseat.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.Seat;
import com.tttangerine.availableseat.db.SpSaveModel;
import com.tttangerine.availableseat.db.User;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.tttangerine.availableseat.db.User.USER_LEAVING;
import static com.tttangerine.availableseat.db.User.USER_NONE;
import static com.tttangerine.availableseat.db.User.USER_USING;

public class TimerActivity extends Activity implements View.OnClickListener {

    private int beginTime = 0;  //开始使用次数，第二次点击开始使用时加入时间限制
    private SharedPreferences sp;  //本地存储时间
    private SharedPreferences.Editor mEditor;
    public static final String filename = "LeaveInterval";
    public static final String key = "LI";  //存储key值
    public static final int MS_TO_SECOND = 1000;  //毫秒到秒
    public static final int MS_TO_MIN = 60000;  //毫秒到分钟
    public static final int LEAVE_INTERVAL = 900;  //两次暂离至少间隔900秒，即15分钟
    public static final String faultFile = "FaultFile";
    public static final String faultKey = "FF";  //存储key值
    public static final int FAULT_INTERVAL = 4320;  //两次暂离至少间隔4320分钟，即72小时

    private RelativeLayout timer_bg;

    private Button btn_timer_begin_use;
    private Button btn_timer_leave;

    private TextView tickText;
    private TextView timerHint;

    private User currentUser;

    //标识定时器状态
    private boolean upTimerIsStart = false;
    private boolean downTimerIsStart = false;

    private Chronometer mChronometer;

    private CountDownTimer mCountDownTimer = new CountDownTimer
            (1800000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (!TimerActivity.this.isFinishing()){
                int min = (int) millisUntilFinished/60000;
                int sec = (int) millisUntilFinished%60000/1000;
                tickText.setText(String.format(getResources().getString(R.string.timer_num), min, sec));
            }
        }

        @Override
        public void onFinish() {

            tickText.setText("倒计时结束");

            //解除 座位-用户 绑定
            seatQuery().findObjects(new FindListener<Seat>() {
                @Override
                public void done(final List<Seat> list, BmobException e) {
                    if (e == null){
                        list.get(0).setSeatType(Seat.SEAT_TYPE_AVAILABLE);
                        list.get(0).remove("mUser");
                        list.get(0).update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) { }
                        });
                        list.get(0).getRoom().availableSeatNum++;
                        list.get(0).getRoom().update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) { }
                        });
                    } else {
                        showToast(e.getMessage());
                    }
                }
            });


            //解除 用户-房间 绑定，记一次违规
            currentUser.setFault(currentUser.getFault()+1);
            currentUser.remove("mRoom");
            currentUser.USER_STATE = USER_NONE;
            currentUser.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e != null){
                        showToast(e.getMessage());
                    }}
            });

            if (currentUser.getFault() >= 3){
                showToast("累计违规已达三次，用户退出登录");
                BmobUser.logOut();
                finish();
            } else {
                showToast("超出倒计时，记一次违规");
                Intent intent = new Intent(TimerActivity.this, HomepageActivity.class);
                startActivity(intent);
                finish();
            }

            //记录登出时间
            SharedPreferences fsp = getSharedPreferences(faultFile, MODE_PRIVATE);
            SharedPreferences.Editor fEditor = fsp.edit();
            SpSaveModel spTime = new SpSaveModel(FAULT_INTERVAL, System.currentTimeMillis());
            String json = JSON.toJSONString(spTime);
            fEditor.putString(faultKey, json);
            fEditor.apply();
        }
    };

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState){
        smoothSwitchScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        currentUser = BmobUser.getCurrentUser(User.class);

        initViews();

        sp = getSharedPreferences(filename, MODE_PRIVATE);
        mEditor = sp.edit();
    }

    private void initViews(){
        /*
         * 注册视图控件
         */
        timer_bg = findViewById(R.id.timer_bg);
        tickText = findViewById(R.id.tick_text);
        timerHint = findViewById(R.id.tv_timer_hint);
        mChronometer = findViewById(R.id.up_chronometer);
        btn_timer_begin_use = findViewById(R.id.btn_timer_begin_use);
        btn_timer_leave = findViewById(R.id.btn_timer_leave);
        Button btn_timer_end_use = findViewById(R.id.btn_timer_end_use);
        Button btn_seat_text = findViewById(R.id.btn_seat_text);
        /*
         * 注册按键监听
         */
        btn_seat_text.setOnClickListener(this);
        btn_timer_begin_use.setOnClickListener(this);
        btn_timer_end_use.setOnClickListener(this);
        btn_timer_leave.setOnClickListener(this);
        int timer_type = getIntent().getIntExtra("timer_type", 2);

        if (timer_type == 0){
            showToast("请在倒计时结束前就座");
            //初始化背景
            timer_bg.setBackground(getDrawable(R.drawable.bg_waiting));
            timerHint.setText(getResources().getString(R.string.hint_waiting));
            /*
             * 初始化按钮
             */
            btn_timer_begin_use.setVisibility(View.VISIBLE);
            btn_timer_leave.setVisibility(View.GONE);
            /*
             *初始化计时器
             */
            tickText.setVisibility(View.VISIBLE);
            mChronometer.setVisibility(View.GONE);
            mCountDownTimer.start();
            downTimerIsStart = true;
        } else if (timer_type == 1){
            showToast("开始学习计时");
            //初始化背景
            timer_bg.setBackground(getDrawable(R.drawable.bg_used));
            timerHint.setText(getResources().getString(R.string.hint_used));
            /*
             * 初始化按钮
             */
            btn_timer_begin_use.setVisibility(View.GONE);
            btn_timer_leave.setVisibility(View.VISIBLE);
            /*
             *初始化计时器
             */
            tickText.setVisibility(View.GONE);
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.start();
            upTimerIsStart = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void onClick(View v){
        switch (v.getId()){

            //查看已选座位
            case R.id.btn_seat_text:
                seatQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(final List<Seat> list, BmobException e) {
                        if (e == null){
                            String roomId = list.get(0).getRoom().getObjectId();
                            Intent intent = new Intent(TimerActivity.this, ChooseSeatActivity.class);
                            intent.putExtra("room_id", roomId);
                            intent.putExtra("btn_type", 2);
                            startActivity(intent);
                        } else {
                            showToast(e.getMessage());
                        }
                    }
                });
                break;

            //暂时离开，停止正向学习计时，开启倒计时，更新用户、座位信息
            case R.id.btn_timer_leave:
                if (beginTime > 1){
                    String json = sp.getString(key, "");
                    if (!TextUtils.isEmpty(json)){
                        SpSaveModel spTime = JSON.parseObject(json, new TypeReference<SpSaveModel>(){});
                        assert spTime != null;
                        if ((System.currentTimeMillis()-spTime.getStartTime())/MS_TO_SECOND<spTime.getSaveTime()){
                            long lastTime = spTime.getSaveTime() -
                                    (System.currentTimeMillis()-spTime.getStartTime())/MS_TO_SECOND;
                            long lastMin = lastTime/60;
                            long lastSec = lastTime%60;
                            showToast(lastMin+"分钟"+lastSec+"秒"+"后才可以离开座位");
                            break;
                        }
                    }
                }

                //圆环设为亮色
                timer_bg.setBackground(getDrawable(R.drawable.bg_waiting));
                timerHint.setText(getResources().getString(R.string.hint_waiting));

                //开始使用按钮设为可见，暂时离开按钮设为不可见
                btn_timer_begin_use.setVisibility(View.VISIBLE);
                btn_timer_leave.setVisibility(View.GONE);

                //开始倒计时，使倒计时可见
                if (!downTimerIsStart){
                    showToast("请在倒计时结束前回到座位");
                    mCountDownTimer.start();
                    downTimerIsStart = true;
                    tickText.setVisibility(View.VISIBLE);
                }

                //停止学习计时，使计时不可见
                if (upTimerIsStart){
                    mChronometer.stop();
                    mChronometer.setVisibility(View.GONE);
                }

                mCountDownTimer.start();
                downTimerIsStart = true;

                seatQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(List<Seat> list, BmobException e) {
                        list.get(0).setSeatType(Seat.SEAT_TYPE_WAITING);
                        list.get(0).update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e != null){
                                    showToast(e.getMessage());
                                }
                            }
                        });
                    }
                });

                currentUser.USER_STATE = USER_LEAVING;
                currentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null){
                            showToast(e.getMessage());
                        }}
                });

                break;

            //开始使用，结束倒计时，开始正向学习计时，更新用户、座位信息
            case R.id.btn_timer_begin_use:
                beginTime++;
                if (beginTime > 1){
                    SpSaveModel spTime = new SpSaveModel(LEAVE_INTERVAL, System.currentTimeMillis());
                    String json = JSON.toJSONString(spTime);
                    mEditor.putString(key, json);
                    mEditor.commit();
                }

                //圆环设为暗色
                timer_bg.setBackground(getDrawable(R.drawable.bg_used));
                timerHint.setText(getResources().getString(R.string.hint_used));

                //开始使用按钮设为不可见，暂时离开按钮设为可见
                btn_timer_begin_use.setVisibility(View.GONE);
                btn_timer_leave.setVisibility(View.VISIBLE);

                //停止倒计时，使倒计时不可见
                if (downTimerIsStart){
                    mCountDownTimer.cancel();
                    downTimerIsStart = false;
                    tickText.setVisibility(View.GONE);
                }

                //开始学习计时，使计时可见
                if (!upTimerIsStart){
                    showToast("开始学习计时");
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        mChronometer.setCountDown(false);
                    mChronometer.start();
                    upTimerIsStart = true;
                } else {
                    mChronometer.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        mChronometer.setCountDown(false);
                    mChronometer.start();
                }

                seatQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(List<Seat> list, BmobException e) {
                        list.get(0).setSeatType(Seat.SEAT_TYPE_USED);
                        list.get(0).update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e != null){
                                    showToast(e.getMessage());
                                } }
                        });
                    }
                });

                currentUser.USER_STATE = USER_USING;
                currentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null){
                            showToast(e.getMessage());
                        } }
                });

                break;

            //结束使用，停止所有计时，更新用户、座位、教室信息
            case R.id.btn_timer_end_use:
                beginTime = 0;

                //停止倒计时
                if (downTimerIsStart){
                    mCountDownTimer.cancel();
                    downTimerIsStart = false;
                }

                //停止计时
                if (upTimerIsStart){
                    mChronometer.stop();
                    upTimerIsStart = false;
                }

                mCountDownTimer.cancel();

                seatQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(List<Seat> list, BmobException e) {
                        if (e == null){
                            list.get(0).remove("mUser");
                            list.get(0).setSeatType(Seat.SEAT_TYPE_AVAILABLE);
                            list.get(0).update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e != null){
                                        showToast(e.getMessage());
                                    } }
                            });
                            list.get(0).getRoom().availableSeatNum++;
                            list.get(0).getRoom().update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    HomepageActivity.instance.refreshCard();
                                }
                            });
                        } else {
                            showToast(e.getMessage());
                        }
                    }
                });

                currentUser.remove("mRoom");
                currentUser.USER_STATE = USER_NONE;
                currentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null){
                            showToast(e.getMessage());
                        }}
                });

                Intent intent = new Intent(TimerActivity.this, HomepageActivity.class);
                startActivity(intent);
                finish();

                break;

        }
    }

    //查询用户所在的房间
    private BmobQuery<Seat> seatQuery(){
        BmobQuery<Seat> query = new BmobQuery<>();
        query.addWhereEqualTo("mUser", currentUser);
        query.include("mRoom");
        return query;
    }


    //监听返回键，定时器后台运行
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //activity销毁时，销毁计时器
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mCountDownTimer != null){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(TimerActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void smoothSwitchScreen() {
        ViewGroup rootView = (this.findViewById(android.R.id.content));
        rootView.setBackground(getDrawable(R.drawable.bg_root));
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        rootView.setPadding(0, statusBarHeight, 0, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

}
