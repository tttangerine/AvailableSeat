package com.tttangerine.availableseat.db;

import java.io.Serializable;

public class SpSaveModel implements Serializable {
    private int saveTime;  //保存时长
    private long startTime;  //保存时的时间

    public SpSaveModel() {
    }

    public SpSaveModel(int saveTime, long currentTime) {
        this.saveTime = saveTime;
        this.startTime = currentTime;
    }

    public int getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(int saveTime) {
        this.saveTime = saveTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
