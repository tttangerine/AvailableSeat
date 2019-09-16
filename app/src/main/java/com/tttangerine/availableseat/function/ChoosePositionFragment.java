package com.tttangerine.availableseat.function;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.activity.ChooseSeatActivity;
import com.tttangerine.availableseat.db.Building;
import com.tttangerine.availableseat.db.Floor;
import com.tttangerine.availableseat.db.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ChoosePositionFragment extends Fragment {

    //列表级别
    public static final int LEVEL_BUILDING = 0;
    public static final int LEVEL_FLOOR = 1;
    public static final int LEVEL_ROOM = 2;

    private TextView titleText;
    private Button backButton;
    private ListView mListView;

    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
    //楼列表
    private List<Building> mBuildingList = new ArrayList<>();
    //层列表
    private List<Floor> mFloorList = new ArrayList<>();
    //房间列表
    private List<Room> mRoomList = new ArrayList<>();

    //选中的楼
    private Building selectedBuilding;
    //选中的层
    private Floor selectedFloor;
    //选中的房间
    private Room selectedRoom;
    //当前选中的级别
    private int currentLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //初始化控件
        View view = inflater.inflate(R.layout.fragment_choose_position, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        mListView = view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        final int btn_type = Objects.requireNonNull(getActivity()).
                getIntent().getIntExtra("btn_type", 2);

        //列表项点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_BUILDING){  //如果当前列表级别为楼级别
                    selectedBuilding = mBuildingList.get(position);  //将选中列表项对应的楼赋值给选中的楼
                    queryFloors();  //查询楼层
                }else if (currentLevel == LEVEL_FLOOR){
                    selectedFloor = mFloorList.get(position);
                    queryRooms();
                }else if (currentLevel == LEVEL_ROOM){
                    selectedRoom = mRoomList.get(position);
                    String roomId = selectedRoom.getObjectId();
                    Intent intent = new Intent(getActivity(), ChooseSeatActivity.class);
                    intent.putExtra("room_id", roomId);
                    intent.putExtra("btn_type", btn_type);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        });
        //返回键
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_ROOM){
                    queryFloors();
                }else if (currentLevel == LEVEL_FLOOR){
                    queryBuildings();
                }
            }
        });
        queryBuildings();
    }

    //查询所有的楼
    private void queryBuildings() {
        titleText.setText("中国地质大学（北京）");
        backButton.setVisibility(View.GONE);

        BmobQuery<Building> buildingBmobQuery = new BmobQuery<>();
        buildingBmobQuery.findObjects(new FindListener<Building>() {
            @Override
            public void done(List<Building> object, BmobException e) {
                if (e == null){
                    dataList.clear();
                    for (int i = 0; i < object.size(); i++) {
                        dataList.add(object.get(i).getName());
                        mBuildingList.add(object.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    currentLevel = LEVEL_BUILDING;
                } else {
                    showToast(e.getMessage());
                }

            }
        });
    }

    //查询所有楼层
    private void queryFloors() {
        titleText.setText(selectedBuilding.getName());
        backButton.setVisibility(View.VISIBLE);

        BmobQuery<Floor> floorBmobQuery = new BmobQuery<>();
        floorBmobQuery.addWhereEqualTo("buildingName",selectedBuilding.getName());
        floorBmobQuery.findObjects(new FindListener<Floor>() {
            @Override
            public void done(List<Floor> object, BmobException e) {
                if (e == null){
                    dataList.clear();
                    for (int i = 0; i < object.size(); i++) {
                        dataList.add(object.get(i).getFloorName());
                        mFloorList.add(object.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    currentLevel = LEVEL_FLOOR;
                } else {
                    showToast(e.getMessage());
                }

            }
        });
    }

    //查询所有房间
    private void queryRooms() {
        titleText.setText(selectedFloor.getFloorName());
        backButton.setVisibility(View.VISIBLE);

        BmobQuery<Room> roomBmobQuery = new BmobQuery<>();
        roomBmobQuery.addWhereEqualTo("building",selectedBuilding);
        roomBmobQuery.addWhereEqualTo("floor",selectedFloor);
        roomBmobQuery.findObjects(new FindListener<Room>() {
            @Override
            public void done(List<Room> object, BmobException e) {
                if (e == null){
                    dataList.clear();
                    mRoomList.clear();
                    for (int i = 0; i < object.size(); i++) {
                        dataList.add(object.get(i).getRoomName());
                        mRoomList.add(object.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    currentLevel = LEVEL_ROOM;
                } else {
                    showToast(e.getMessage());
                }
            }
        });
    }

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
