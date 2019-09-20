package com.tttangerine.availableseat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.Room;
import com.tttangerine.availableseat.db.Seat;
import com.tttangerine.availableseat.db.User;
import com.tttangerine.availableseat.view.ChooseSeatView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.tttangerine.availableseat.db.User.USER_LEAVING;
import static com.tttangerine.availableseat.db.User.USER_USING;

public class ChooseSeatActivity extends Activity implements View.OnClickListener {

    //创建activity本身的实例，供其他activity调用
    @SuppressLint("StaticFieldLeak")
    public static ChooseSeatActivity instance = null;

    private ChooseSeatView mChooseSeatView;  //自定义选座视图
    private Button btn_select_seat;
    private Button btn_book_select;
    private TextView tv_room_id;

    private String ROOM_ID;
    private int btn_type;

    private User currentUser;

    //已选、预约座位列表
    private List<Seat> usedSeatList = new ArrayList<>();
    private List<Seat> waitingSeatList = new ArrayList<>();

    private Toast toast = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        smoothSwitchScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_seat);
        instance = this;
        init();
    }

    private void init(){

        tv_room_id = findViewById(R.id.tv_room_id);

        //获取intent传递的信息，根据按钮类型决定预约/选座按钮的显示和隐藏
        btn_type = getIntent().getIntExtra("btn_type", 2);
        btn_book_select = findViewById(R.id.book_select);
        btn_select_seat = findViewById(R.id.select_seat);
        if (btn_type == 0){
            btn_book_select.setOnClickListener(this);
            btn_book_select.setVisibility(View.VISIBLE);
            btn_select_seat.setVisibility(View.GONE);
            setBtnClickable(btn_book_select, false);
        } else if (btn_type == 1){
            btn_select_seat.setOnClickListener(this);
            btn_select_seat.setVisibility(View.VISIBLE);
            btn_book_select.setVisibility(View.GONE);
            setBtnClickable(btn_select_seat, false);
        } else {
            //当按钮类型既不是预约，也不是选座时，两个按钮都不显示
            btn_select_seat.setVisibility(View.GONE);
            btn_book_select.setVisibility(View.GONE);
        }

        currentUser = BmobUser.getCurrentUser(User.class);

        //获取intent传递的信息，得到房间信息，获取相应的座位信息
        ROOM_ID = getIntent().getStringExtra("room_id");
        BmobQuery<Room> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("objectId", ROOM_ID);
        bmobQuery.findObjects(new FindListener<Room>() {
            @Override
            public void done(List<Room> list, BmobException e) {
                if (list != null){
                    //按房间的行列数更新选座视图
                    mChooseSeatView.setData(list.get(0).getSeatRows(), list.get(0).getSeatColumns());
                    tv_room_id.setText(String.format(getResources().getString(R.string.room_id),
                            list.get(0).getId()));
                }
                if (e!=null){
                    showToast(e.getMessage());
                }
            }
        });

        mChooseSeatView = findViewById(R.id.choose_seat);
        loadSeatView();
    }

    private void loadSeatView(){
        usedSeatList.clear();
        waitingSeatList.clear();

        //联网获取已占用/预约的座位
        Room room = new Room();
        room.setObjectId(ROOM_ID);
        BmobQuery<Seat> seatBmobQuery = new BmobQuery<>();
        seatBmobQuery.addWhereEqualTo("mRoom", new BmobPointer(room));
        seatBmobQuery.findObjects(new FindListener<Seat>() {
            @Override
            public void done(List<Seat> list, BmobException e) {
                if (list != null){
                    for (int i = 0; i < list.size(); i++){
                        if (list.get(i).getSeatType()==Seat.SEAT_TYPE_USED)
                            usedSeatList.add(list.get(i));
                        else if (list.get(i).getSeatType()==Seat.SEAT_TYPE_WAITING)
                            waitingSeatList.add(list.get(i));
                    }
                }
                if (e!=null){
                    showToast(e.getMessage());
                }
            }
        });

        //通过接口更新选座视图
        mChooseSeatView.setSeatChecker(new ChooseSeatView.SeatChecker() {

            @Override
            public boolean isValidSeat(int row, int column) { return true; }

            @Override
            public boolean isUsed(int row, int column) {

                boolean isUsedSeat = false;
                if (usedSeatList != null){
                    for (int i = 0; i<usedSeatList.size(); i++){
                        isUsedSeat = isUsedSeat || ((row == usedSeatList.get(i).getRow())&&
                                column == usedSeatList.get(i).getColumn());
                    }
                }
                return isUsedSeat;
            }

            @Override
            public boolean isWaiting(int row, int column) {

                boolean isWaitingSeat = false;
                if (waitingSeatList != null){
                    for (int i = 0; i<waitingSeatList.size(); i++){
                        isWaitingSeat = isWaitingSeat || ((row == waitingSeatList.get(i).getRow())&&
                                column == waitingSeatList.get(i).getColumn());
                    }
                }
                return isWaitingSeat;
            }

            @Override
            public void checked(int row, int column) { }

            @Override
            public void unCheck(int row, int column) { }

            @Override
            public String[] checkedSeatTxt(int row, int column) { return null; }

        });

        mChooseSeatView.invalidate();  //重绘选座视图

        //获取选择的座位
        BmobQuery<Seat> query = new BmobQuery<>();
        query.addWhereEqualTo("mUser", currentUser);
        query.findObjects(new FindListener<Seat>() {
            @Override
            public void done(final List<Seat> list, BmobException e) {
                if (list != null){
                    mChooseSeatView.setSelectedId(list.get(0).getId());
                    mChooseSeatView.invalidate();
                }
            }
        });
    }

    public void onClick(View v){
        switch (v.getId()){

            //选择座位按钮，更新用户使用信息、房间空座信息、座位使用状态……跳转到计时页面
            case R.id.select_seat:

                HomepageActivity.instance.finish();

                updateUser(USER_USING);

                upateRoom();

                andQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(List<Seat> list, BmobException e) {
                        if (list != null){
                            list.get(0).setUser(currentUser);
                            list.get(0).setSeatType(Seat.SEAT_TYPE_USED);
                            list.get(0).update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e!=null){
                                        showToast(e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                });

                Intent select_intent = new Intent(ChooseSeatActivity.this
                        , TimerActivity.class);
                select_intent.putExtra("timer_type", 1);
                startActivity(select_intent);
                finish();

                break;

            //预约按钮，更新用户使用信息、房间空座信息、座位使用状态……跳转到计时页面
            case R.id.book_select:

                HomepageActivity.instance.finish();

                updateUser(USER_LEAVING);

                upateRoom();

                andQuery().findObjects(new FindListener<Seat>() {
                    @Override
                    public void done(List<Seat> list, BmobException e) {
                        if (list != null){
                            list.get(0).setUser(currentUser);
                            list.get(0).setSeatType(Seat.SEAT_TYPE_WAITING);
                            list.get(0).update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e!=null){
                                        showToast(e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                });

                Intent book_intent = new Intent(ChooseSeatActivity.this
                        , TimerActivity.class);
                book_intent.putExtra("timer_type", 0);
                startActivity(book_intent);
                finish();

                break;
        }
    }

    //查询选中的房间中选中的座位
    private BmobQuery<Seat> andQuery(){
        Room room = new Room();
        room.setObjectId(ROOM_ID);
        BmobQuery<Seat> seatQuery1 = new BmobQuery<>();
        seatQuery1.addWhereEqualTo("mRoom", new BmobPointer(room));  //查询的房间
        BmobQuery<Seat> seatQuery2 = new BmobQuery<>();
        seatQuery2.addWhereEqualTo("id", mChooseSeatView.getSelectedId());  //相应房间中选中的座位
        List<BmobQuery<Seat>> andQuerys = new ArrayList<>();
        andQuerys.add(seatQuery1);
        andQuerys.add(seatQuery2);
        BmobQuery<Seat> query = new BmobQuery<>();
        query.and(andQuerys);
        return query;
    }

    //更新用户信息
    private void updateUser(Integer STATE_TYPE){
        Room room = new Room();
        room.setObjectId(ROOM_ID);
        currentUser.setRoom(room);
        currentUser.USER_STATE = STATE_TYPE;
        currentUser.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e!=null){
                    showToast(e.getMessage());
                }
            }
        });
    }

    //更新房间座位信息（选中座位，空座数-1）
    private void upateRoom(){
        BmobQuery<Room> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", ROOM_ID);
        query.findObjects(new FindListener<Room>() {
            @Override
            public void done(List<Room> list, BmobException e) {
                list.get(0).availableSeatNum--;
                list.get(0).update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e!=null){
                            showToast(e.getMessage());
                        }
                    }
                });
            }
        });
    }

    //按座位选中状态，更新底部按钮文字、颜色、是否允许点击
    public void refreshButton(Boolean clickable){
        if (btn_type == 0)
            setBtnClickable(btn_book_select, clickable);
        else if (btn_type == 1)
            setBtnClickable(btn_select_seat, clickable);
    }
    private void setBtnClickable(Button btn, boolean clickable) {
        if (clickable){
            btn.setClickable(true);
            if (btn == btn_book_select)
                btn.setText(getResources().getString(R.string.book_select));
            else if (btn == btn_select_seat)
                btn.setText(getResources().getString(R.string.select_seat));
            btn.setBackgroundColor(getResources().getColor(R.color.focus_LivingCoral));
        } else {
            btn.setClickable(false);
            btn.setText(getResources().getText(R.string.before_select_seat));
            btn.setBackgroundColor(getResources().getColor(R.color.bg_ViridianGreen));
        }
    }

    @SuppressLint("ShowToast")
    private void showToast(String msg){
        if (toast == null){
            toast = Toast.makeText(ChooseSeatActivity.this, msg, Toast.LENGTH_SHORT);
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
