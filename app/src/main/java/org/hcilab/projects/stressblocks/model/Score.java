package org.hcilab.projects.stressblocks.model;

import android.provider.BaseColumns;

/**
 * Created by Sanjeev on 14.01.2017.
 */

public class Score {

    private String userID;
    private int value;
    private String username;

    public Score(String userID, String username, int value) {
        this.userID = userID;
        this.username = username;
        this.value = value;
    }

    public String getUserID() {
        return userID;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getUsername() {
        return username;
    }

    public static class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "score";
        public static final String VALUE = "value";
    }
}