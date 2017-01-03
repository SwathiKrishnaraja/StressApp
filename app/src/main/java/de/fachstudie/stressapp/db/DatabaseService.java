package de.fachstudie.stressapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.fachstudie.stressapp.model.StressNotification;

/**
 * Created by Sanjeev on 03.01.2017.
 */

public class DatabaseService {

    private final String[] columns = {StressNotification.NotificationEntry._ID, StressNotification
            .NotificationEntry.TITLE, StressNotification.NotificationEntry.APPLICATION,
            StressNotification.NotificationEntry.LOADED, StressNotification
            .NotificationEntry.TIMESTAMP, StressNotification.NotificationEntry.CONTENT};

    private final String SELECT_QUERY = "SELECT * FROM " + StressNotification.NotificationEntry
            .TABLE_NAME + " ORDER BY DESC " + StressNotification.NotificationEntry.TIMESTAMP + " AND" +
            " WHERE " + StressNotification.NotificationEntry.LOADED + " = ?";

    private DatabaseHelper dbHelper;

    public DatabaseService(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<StressNotification> getAllNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, columns, null,
                null, null, null, null);

        addNotifications(notifications, c);
        db.close();
        return notifications;
    }

    public List<StressNotification> getAllNotLoadedNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(SELECT_QUERY, new String[]{"false"});

        addNotifications(notifications, c);
        db.close();
        return notifications;
    }

    private void addNotifications(List<StressNotification> notifications, Cursor c) {
        if (c != null && c.moveToFirst()) {
            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                        .TITLE));
                String content = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                        .CONTENT));
                String application = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                        .APPLICATION));
                String timeStampText = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry
                        .TIMESTAMP));

                Log.d("Timestamp", timeStampText);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date timeStampDate = null;
                try {
                    timeStampDate = sdf.parse(timeStampText);
                } catch (ParseException e) {
                }

                boolean loaded = ("true".equals(c.getString(c.getColumnIndex(StressNotification.NotificationEntry.LOADED))));
                Log.d("Loaded", "" + loaded);

                StressNotification not = new StressNotification(title, application, content,
                        timeStampDate, loaded);
                notifications.add(not);
            }
        }
    }
}
