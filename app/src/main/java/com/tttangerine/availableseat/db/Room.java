package com.tttangerine.availableseat.db;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

@SuppressWarnings("unused")
public class Room extends BmobObject {

    private Integer id;

    private Floor floor;

    private String floorName;

    private Building building;

    private String buildingName;

    private Integer validSeatNum;

    public Integer availableSeatNum;

    public ArrayList<Seat> seats;

    //座位行数和列数
    private Integer seatRows;
    private Integer seatColumns;

    public Building getbuilding() {
        return building;
    }

    public Room setbuilding(Building building) {
        this.building = building;
        return this;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Integer getId() {
        return id;
    }

    public Room setId(int id) {
        this.id = id;
        return this;
    }

    public String getRoomName() {
        return id + "";
    }

    public Floor getFloor() {
        return floor;
    }

    public Room setFloor(Floor floor) {
        this.floor = floor;
        return this;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public int getSeatRows() {
        return seatRows;
    }

    public void setSeatRows(int seatRows) {
        this.seatRows = seatRows;
    }

    public int getSeatColumns() {
        return seatColumns;
    }

    public void setSeatColumns(int seatColumns) {
        this.seatColumns = seatColumns;
    }

    public int getValidSeatNum() {
        return validSeatNum;
    }

    public void setValidSeatNum(int validSeatNum) {
        this.validSeatNum = validSeatNum;
    }
}

