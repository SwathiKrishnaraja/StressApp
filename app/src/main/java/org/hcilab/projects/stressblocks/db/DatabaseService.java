package org.hcilab.projects.stressblocks.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.hcilab.projects.stressblocks.EmojiFrequency;
import org.hcilab.projects.stressblocks.model.Score;
import org.hcilab.projects.stressblocks.model.StressLevel;
import org.hcilab.projects.stressblocks.model.StressNotification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides several services for handling the 'CRUD' functions regarding the model data.
 */

public class DatabaseService {

    private final String[] notificationColumns = {StressNotification.NotificationEntry._ID,
            StressNotification.NotificationEntry.TITLE_LENGTH,
            StressNotification.NotificationEntry.APPLICATION,
            StressNotification.NotificationEntry.CONTENT_LENGTH,
            StressNotification.NotificationEntry.EMOTICONS,
            StressNotification.NotificationEntry.LOADED,
            StressNotification.NotificationEntry.TIMESTAMP,
            StressNotification.NotificationEntry.EVENT,
            StressNotification.NotificationEntry.SENT};

    private final String NOTIFICATIONS_SELECT_QUERY = "SELECT * FROM " + StressNotification.NotificationEntry
            .TABLE_NAME + " WHERE " + StressNotification.NotificationEntry.APPLICATION + " != ''" +
            " AND " + StressNotification.NotificationEntry.LOADED + " = ?" +
            " AND " + StressNotification.NotificationEntry.EVENT + " = ?" +
            " ORDER BY " + StressNotification.NotificationEntry.TIMESTAMP + " DESC";

    private final String NOTIFICATIONS_NOT_SENT_QUERY = "SELECT * FROM " +
            StressNotification.NotificationEntry.TABLE_NAME + " WHERE " +
            StressNotification.NotificationEntry.SENT + " = ?";

    private final String SCORE_SELECT_QUERY = "SELECT MAX(" + Score.ScoreEntry.VALUE + ") AS " +
            Score.ScoreEntry.VALUE + " FROM " + Score.ScoreEntry.TABLE_NAME;

    private final String STRESSLEVEL_SELECT_QUERY = "SELECT * FROM " + StressLevel.StressLevelEntry
            .TABLE_NAME + " WHERE " + StressLevel.StressLevelEntry.SENT + " = ?";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static DatabaseService dbService;
    private static DatabaseHelper dbHelper;

    public static synchronized DatabaseService getInstance(Context context) {
        if (dbService == null) {
            dbService = new DatabaseService(context.getApplicationContext());
        }
        return dbService;
    }

    private DatabaseService(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public int getHighScore() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(SCORE_SELECT_QUERY, null);
        int score = 0;

        if (c != null && c.moveToFirst()) {
            score = c.getInt(c.getColumnIndex(Score.ScoreEntry.VALUE));
        }

        closeDatabaseComponents(c);
        return score;
    }

    public void saveScore(int score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(Score.ScoreEntry.VALUE, score);
        db.insert(Score.ScoreEntry.TABLE_NAME, null, value);
    }

    public void saveStressLevel(int level, boolean sent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(StressLevel.StressLevelEntry.VALUE, level);
        value.put(StressLevel.StressLevelEntry.SENT, "" + sent);
        db.insert(StressLevel.StressLevelEntry.TABLE_NAME, null, value);
    }

    public void saveNotification(Intent intent, boolean successful) {
        String titleLength = intent.getStringExtra("title_length");
        String content = intent.getStringExtra("content");
        String application = intent.getStringExtra("application");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        String event = intent.getStringExtra("event");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.TITLE_LENGTH, Integer.valueOf(titleLength));
        values.put(StressNotification.NotificationEntry.CONTENT_LENGTH, content.length());
        values.put(StressNotification.NotificationEntry.EMOTICONS,
                EmojiFrequency.getCommaSeparatedEmoticons(content));
        values.put(StressNotification.NotificationEntry.APPLICATION, application);
        values.put(StressNotification.NotificationEntry.LOADED, "false");
        values.put(StressNotification.NotificationEntry.TIMESTAMP, timestamp);
        values.put(StressNotification.NotificationEntry.EVENT, event);
        values.put(StressNotification.NotificationEntry.SENT, "" + successful);
        db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
    }

    public List<StressNotification> getNotifications() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, notificationColumns, null,
                null, null, null, null);

        List<StressNotification> notifications = loadNotifications(c);
        closeDatabaseComponents(c);
        return notifications;
    }

    public List<StressNotification> getSpecificNotifications(String loaded) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {loaded, "NOTIFICATION"};
        Cursor c = db.rawQuery(NOTIFICATIONS_SELECT_QUERY, selectionArgs);

        List<StressNotification> notifications = loadNotifications(c);
        closeDatabaseComponents(c);
        return notifications;
    }

    public List<StressNotification> getNotSentNotifications() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false"};
        Cursor c = db.rawQuery(NOTIFICATIONS_NOT_SENT_QUERY, selectionArgs);

        List<StressNotification> results = loadNotifications(c);
        closeDatabaseComponents(c);
        return results;
    }

    public List<StressLevel> getNotSentStressLevels() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false"};
        Cursor c = db.rawQuery(STRESSLEVEL_SELECT_QUERY, selectionArgs);

        List<StressLevel> results = loadStressLevels(c);
        closeDatabaseComponents(c);
        return results;
    }

    public void updateNotificationIsLoaded(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.LOADED, "true");

        String selection = StressNotification.NotificationEntry._ID + " LIKE ?";
        String[] selectionArgs = {"" + id};

        db.update(StressNotification.NotificationEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateNotificationIsSent(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.SENT, "true");

        String selection = StressNotification.NotificationEntry._ID + " LIKE ?";
        String[] selectionArgs = {"" + id};

        db.update(StressNotification.NotificationEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateStressLevelIsSent(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StressLevel.StressLevelEntry.SENT, "true");

        String selection = StressLevel.StressLevelEntry._ID + " LIKE ?";
        String[] selectionArgs = {"" + id};

        db.update(StressLevel.StressLevelEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private List<StressNotification> loadNotifications(Cursor c) {
        List<StressNotification> notifications = new ArrayList<>();
        if (c != null) {
            while (!c.isClosed() && c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry._ID));

                int titleLength = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry
                        .TITLE_LENGTH));
                String application = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry.APPLICATION));
                int contentLength = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry
                        .CONTENT_LENGTH));
                String emoticons = c.getString(c.getColumnIndex(StressNotification.NotificationEntry.
                        EMOTICONS));

                Log.d("emoticons", emoticons);

                String timeStampText = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry
                        .TIMESTAMP));

                Date timeStampDate = getTimeStampDate(timeStampText);

                StressNotification notification = new StressNotification(id, titleLength, application,
                        contentLength, emoticons, EmojiFrequency.getEmoticons(emoticons),
                        timeStampDate);
                notifications.add(notification);
            }
        }

        return notifications;
    }

    private List<StressLevel> loadStressLevels(Cursor c) {
        List<StressLevel> results = new ArrayList<>();
        if (c != null) {
            while (!c.isClosed() && c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressLevel.StressLevelEntry._ID));
                int value = c.getInt(c.getColumnIndex(StressLevel.StressLevelEntry.VALUE));

                StressLevel result = new StressLevel(id, value);
                results.add(result);
            }
        }
        return results;
    }

    private Date getTimeStampDate(String timeStampText) {
        Date timeStampDate = null;
        try {
            timeStampDate = sdf.parse(timeStampText);
        } catch (ParseException e) {
        }

        return timeStampDate;
    }

    private void closeDatabaseComponents(Cursor c) {
        c.close();
    }

    public void saveScreenEvent(String event, boolean successful) {
        int titleLength = 0;
        String content = "";
        String application = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.TITLE_LENGTH, titleLength);
        values.put(StressNotification.NotificationEntry.CONTENT_LENGTH, content.length());
        values.put(StressNotification.NotificationEntry.EMOTICONS,
                EmojiFrequency.getCommaSeparatedEmoticons(content));
        values.put(StressNotification.NotificationEntry.APPLICATION, application);
        values.put(StressNotification.NotificationEntry.LOADED, "false");
        values.put(StressNotification.NotificationEntry.TIMESTAMP, timestamp);
        values.put(StressNotification.NotificationEntry.EVENT, event);
        values.put(StressNotification.NotificationEntry.SENT, "" + successful);
        db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
    }
}
