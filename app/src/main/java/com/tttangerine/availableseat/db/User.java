package com.tttangerine.availableseat.db;

import cn.bmob.v3.BmobUser;

@SuppressWarnings("unused")
public class User extends BmobUser {

    //使用状态
    public static final int USER_NONE = 0;
    public static final int USER_USING = 1;
    public static final int USER_LEAVING = 2;
    public Integer USER_STATE = USER_NONE;

    //违规次数
    private Integer fault = 0;

    private Room mRoom;

    public Room getRoom() {
        return mRoom;
    }

    public void setRoom(Room room) { mRoom = room; }

    public Integer getFault() {
        return fault;
    }

    public void setFault(Integer fault) {
        this.fault = fault;
    }
}
