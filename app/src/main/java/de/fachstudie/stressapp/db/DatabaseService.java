package de.fachstudie.stressapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.fachstudie.stressapp.EmojiFrequency;
import de.fachstudie.stressapp.model.Score;
import de.fachstudie.stressapp.model.StressNotification;
import de.fachstudie.stressapp.model.SurveyResult;

/**
 * Created by Sanjeev on 03.01.2017.
 */

public class DatabaseService {

    private final String[] notificationColumns = {StressNotification.NotificationEntry._ID,
            StressNotification.NotificationEntry.TITLE,
            StressNotification.NotificationEntry.APPLICATION,
            StressNotification.NotificationEntry.CONTENT_LENGTH,
            StressNotification.NotificationEntry.EMOTICONS,
            StressNotification.NotificationEntry.LOADED,
            StressNotification.NotificationEntry.TIMESTAMP,
            StressNotification.NotificationEntry.EVENT};

    private final String[] surveyResultColumns = {SurveyResult.SurveyResultEntry._ID,
            SurveyResult.SurveyResultEntry.ANSWERS};

    private final String NOTIFICATION_SELECT_QUERY = "SELECT * FROM " + StressNotification.NotificationEntry
            .TABLE_NAME + " WHERE " + StressNotification.NotificationEntry.LOADED + " = ?" +
            " AND WHERE " + StressNotification.NotificationEntry.EVENT + " = ?" +
            " ORDER BY " + StressNotification.NotificationEntry.TIMESTAMP + " ASC";

    private final String SCORE_SELECT_QUERY = "SELECT MAX(" + Score.ScoreEntry.VALUE + ") AS " +
            Score.ScoreEntry.VALUE + " FROM " + Score.ScoreEntry.TABLE_NAME;

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
        db.close();
    }

    public List<SurveyResult> getSurveyResults() {
        List<SurveyResult> results = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(SurveyResult.SurveyResultEntry.TABLE_NAME, surveyResultColumns, null,
                null, null, null, null);

        addAnswers(results, c);
        closeDatabaseComponents(c);
        return results;
    }

    public void saveNotification(Intent intent){
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String application = intent.getStringExtra("application");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        Log.d("Title", title);
        Log.d("Received Notification", application);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.TITLE, title);
        values.put(StressNotification.NotificationEntry.CONTENT_LENGTH, content.length());
        values.put(StressNotification.NotificationEntry.EMOTICONS,
                EmojiFrequency.getCommaSeparatedEmoticons(content));
        values.put(StressNotification.NotificationEntry.APPLICATION, application);
        values.put(StressNotification.NotificationEntry.LOADED, "false");
        values.put(StressNotification.NotificationEntry.TIMESTAMP, timestamp);
        values.put(StressNotification.NotificationEntry.EVENT, "NOTIFICATION");
        db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
        db.close();
    }

    public List<StressNotification> getNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, notificationColumns, null,
                null, null, null, null);

        addNotifications(notifications, c);
        closeDatabaseComponents(c);
        return notifications;
    }


    public List<StressNotification> getNotLoadedNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false", "NOTIFICATION"};
        Cursor c = db.rawQuery(NOTIFICATION_SELECT_QUERY, selectionArgs);

        addNotifications(notifications, c);
        closeDatabaseComponents(c);
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
                String application = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry.APPLICATION));
                int contentLength = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry
                        .CONTENT_LENGTH));
                String emoticons = c.getString(c.getColumnIndex(StressNotification.NotificationEntry.
                        EMOTICONS));
                String timeStampText = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry
                        .TIMESTAMP));
                String event = c.getString(c.getColumnIndex(StressNotification.NotificationEntry.EVENT));

                Date timeStampDate = getTimeStampDate(timeStampText);

                boolean loaded = ("true".equals(c.getString(
                        c.getColumnIndex(StressNotification.NotificationEntry.LOADED))));

                StressNotification notification = new StressNotification(id, title, application,
                        contentLength, EmojiFrequency.getEmoticons(emoticons),
                        loaded, timeStampDate, event);
                notifications.add(notification);
            }
        }
    }

    private void addAnswers(List<SurveyResult> results, Cursor c) {
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry._ID));

                String answers = c.getString(c.getColumnIndex(SurveyResult.SurveyResultEntry
                        .ANSWERS));

                List<String> items = Arrays.asList(answers.split(","));

                SurveyResult result = new SurveyResult(id, items);
                results.add(result);
            }
        }
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

    public void saveScreenEvent(String event) {
        String title = "";
        String content = "";
        String application = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StressNotification.NotificationEntry.TITLE, title);
        values.put(StressNotification.NotificationEntry.CONTENT_LENGTH, content.length());
        values.put(StressNotification.NotificationEntry.EMOTICONS,
                EmojiFrequency.getCommaSeparatedEmoticons(content));
        values.put(StressNotification.NotificationEntry.APPLICATION, application);
        values.put(StressNotification.NotificationEntry.LOADED, "false");
        values.put(StressNotification.NotificationEntry.TIMESTAMP, timestamp);
        values.put(StressNotification.NotificationEntry.EVENT, event);
        db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
        db.close();
    }
}
