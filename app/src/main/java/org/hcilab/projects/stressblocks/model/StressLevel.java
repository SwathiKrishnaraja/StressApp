package org.hcilab.projects.stressblocks.model;

import android.provider.BaseColumns;

/**
 * Created by Sanjeev on 05.02.2017.
 */

public class StressLevel {
    private int id;
    private int value;

    public StressLevel(int id, int value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public static class StressLevelEntry implements BaseColumns {
        public static final String TABLE_NAME = "stress_level";
        public static final String VALUE = "value";
        public static final String SENT = "sent";
    }
}
