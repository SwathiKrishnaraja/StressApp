package de.fachstudie.stressapp.model;

import java.util.Date;

/**
 * Created by Paul Kuznecov on 13.11.2016.
 */
public class StressNotification {

    private String title;
    private String application;
    private String content;
    private Date timestamp;

    public StressNotification(String title, String application, String content, Date timestamp) {
        this.title = title;
        this.application = application;
        this.content = content;
        this.timestamp = timestamp;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
