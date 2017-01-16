package de.fachstudie.stressapp.model;

import android.provider.BaseColumns;

import com.vdurmont.emoji.Emoji;

import java.util.Date;
import java.util.Map;

/**
 * Created by Paul Kuznecov on 13.11.2016.
 */
public class StressNotification {

    private int id;
    private String title;
    private String application;
    private int contentLength;
    private Map<Emoji, Integer> emoticons;
    private boolean loaded;
    private Date timestamp;

    public StressNotification(int id, String title, String application, int contentLength,
                              Map<Emoji, Integer> emoticons, boolean loaded, Date timestamp) {
        this.id = id;
        this.title = title;
        this.application = application;
        this.contentLength = contentLength;
        this.emoticons = emoticons;
        this.loaded = loaded;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public Map<Emoji, Integer> getEmoticons() {
        return emoticons;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public static class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notification";
        public static final String TITLE = "title";
        public static final String APPLICATION = "application";
        public static final String TIMESTAMP = "timestamp";
        public static final String CONTENT_LENGTH = "content_length";
        public static final String EMOTICONS = "emoticons";
        public static final String LOADED = "loaded";
    }
}
