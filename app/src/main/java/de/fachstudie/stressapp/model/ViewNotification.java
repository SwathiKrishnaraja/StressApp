package de.fachstudie.stressapp.model;

import android.databinding.ObservableField;
import android.provider.BaseColumns;

/**
 * Created by Paul Kuznecov on 08.11.2016.
 */
public class ViewNotification {
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> application = new ObservableField<>();
    public final ObservableField<String> timestamp = new ObservableField<>();

    public static class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notification";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String APPLICATION = "application";
        public static final String TIMESTAMP = "timestamp";

    }
}
