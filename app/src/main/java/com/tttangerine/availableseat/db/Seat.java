package com.tttangerine.availableseat.db;

import cn.bmob.v3.BmobObject;

@SuppressWarnings("unused")
public class Seat extends BmobObject {

    //座位已被占用
    public static final int SEAT_TYPE_USED = 1;

    //座位已经选中
    public static final int SEAT_TYPE_SELECTED = 2;

    //座位可选
    public static final int SEAT_TYPE_AVAILABLE = 3;

    //座位不可用
    public static final int SEAT_TYPE_NOT_AVAILABLE = 4;

    //用户暂时离座
    public static final int SEAT_TYPE_WAITING = 5;

    //座位状态
    private Integer seatType;

    public int getSeatType() { return seatType; }

    public void setSeatType(int seatType) {
        this.seatType = seatType;
    }


    private User mUser;
    private Room mRoom;

    public User getUser() { return mUser; }

    public void setUser(User user) { this.mUser = user; }

    public Room getRoom() { return mRoom; }

    public void setRoom(Room room) { this.mRoom = room; }


    private Integer row;
    private Integer column;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    private Integer id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
