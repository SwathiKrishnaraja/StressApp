package de.fachstudie.stressapp.db;

import android.content.ContentValues;
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
            .TABLE_NAME + " WHERE " + StressNotification.NotificationEntry.LOADED + " = ?" +
            " ORDER BY " + StressNotification.NotificationEntry.TIMESTAMP + " ASC";

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
        closeDatabaseComponents(db, c);
        return notifications;
    }

    public List<StressNotification> getAllNotLoadedNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false"};
        Cursor c = db.rawQuery(SELECT_QUERY, selectionArgs);

        addNotifications(notifications, c);
        closeDatabaseComponents(db, c);
        return notifications;
    }

    public void updateNotificationIsLoaded(StressNotification notification) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.LOADED, "" + notification.isLoaded());

        String selection = StressNotification.NotificationEntry._ID + " LIKE ?";
        String[] selectionArgs = {"" + notification.getId()};

        db.update(StressNotification.NotificationEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    private void addNotifications(List<StressNotification> notifications, Cursor c) {
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry._ID));

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

                boolean loaded = ("true".equals(c.getString(
                        c.getColumnIndex(StressNotification.NotificationEntry.LOADED))));

                StressNotification notification = new StressNotification(id, title, application, content,
                        timeStampDate, loaded);
                notifications.add(notification);
            }
        }
    }

    private void closeDatabaseComponents(SQLiteDatabase db, Cursor c) {
        c.close();
        db.close();
    }
}
