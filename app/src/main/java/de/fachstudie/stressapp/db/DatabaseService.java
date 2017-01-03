package de.fachstudie.stressapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.fachstudie.stressapp.model.StressNotification;

/**
 * Created by Sanjeev on 03.01.2017.
 */

public class DatabaseService{

    private final String[] columns = {StressNotification.NotificationEntry._ID, StressNotification
            .NotificationEntry.TITLE, StressNotification.NotificationEntry.APPLICATION,
            StressNotification.NotificationEntry.TIMESTAMP, StressNotification
            .NotificationEntry.CONTENT};

    private DatabaseHelper dbHelper;

    public DatabaseService(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<StressNotification> getAllNotifications() {
        List<StressNotification> notifications = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, columns, null,
                null, null, null, null);

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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date timeStampDate = null;
                try {
                    timeStampDate = sdf.parse(timeStampText);
                } catch (ParseException e) {
                }

                StressNotification not = new StressNotification(title, application, content,
                        timeStampDate);
                notifications.add(not);
            }
            db.close();
        }
        return notifications;
    }
}
