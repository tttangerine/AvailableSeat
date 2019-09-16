package com.tttangerine.availableseat.db;

import cn.bmob.v3.BmobObject;

@SuppressWarnings("ALL")
public class Floor extends BmobObject {

    private Integer id;

    private String floorName;

    private Building building;

    private String buildingName;

    public int getId() {
        return id;
    }

    public Floor setId(int id) {
        this.id = id;
        return this;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public Building getBuilding() {
        return building;
    }

    public Floor setBuilding(Building building) {
        this.building = building;
        return this;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
}
