package com.tttangerine.availableseat.db;

import cn.bmob.v3.BmobObject;

public class Building extends BmobObject {

    private Integer id;

    private String name;

    public int getId() {
        return id;
    }

    public Building setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Building setName(String name) {
        this.name = name;
        return this;
    }
}
