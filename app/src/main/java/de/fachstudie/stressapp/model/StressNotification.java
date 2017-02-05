package de.fachstudie.stressapp.model;

import android.provider.BaseColumns;

import com.vdurmont.emoji.Emoji;

import java.util.Date;
import java.util.Map;

/**
 * Created by Paul Kuznecov on 13.11.2016.
 */
public class StressNotification {

    private String event;
    private int id;
    private String title;
    private String application;
    private int contentLength;
    private String emoticons;
    private Map<Emoji, Integer> emoticonFrequencies;
    private Date timestamp;

    public StressNotification(int id, String title, String application, int contentLength,
                              String emoticons, Map<Emoji, Integer> emoticonFrequencies,
                              Date timestamp) {
        this.id = id;
        this.title = title;
        this.application = application;
        this.contentLength = contentLength;
        this.emoticons = emoticons;
        this.emoticonFrequencies = emoticonFrequencies;
        this.timestamp = timestamp;
        this.event = "NOTIFICATION";
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

    public String getEmoticons() {
        return emoticons;
    }

    public Map<Emoji, Integer> getEmoticonFrequencies() {
        return emoticonFrequencies;
    }

    public String getEvent() {
        return event;
    }

    public static class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notification";
        public static final String TITLE = "title";
        public static final String APPLICATION = "application";
        public static final String TIMESTAMP = "timestamp";
        public static final String CONTENT_LENGTH = "content_length";
        public static final String EMOTICONS = "emoticons";
        public static final String LOADED = "loaded";
        public static final String EVENT = "event";
        public static final String SENT = "sent";
    }
}
