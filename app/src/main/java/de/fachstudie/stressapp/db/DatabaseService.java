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
            StressNotification.NotificationEntry.EVENT,
            StressNotification.NotificationEntry.SENT};

    private final String[] surveyResultColumns = {SurveyResult.SurveyResultEntry._ID,
            SurveyResult.SurveyResultEntry.ANSWERS, SurveyResult.SurveyResultEntry.SENT};

    private final String NOTIFICATIONS_SELECT_QUERY = "SELECT * FROM " + StressNotification.NotificationEntry
            .TABLE_NAME + " WHERE " + StressNotification.NotificationEntry.APPLICATION + " != ''" +
            " AND " + StressNotification.NotificationEntry.LOADED + " = ?" +
            " AND " + StressNotification.NotificationEntry.EVENT + " = ?" +
            " ORDER BY " + StressNotification.NotificationEntry.TIMESTAMP + " DESC";

    private final String NOTIFICATIONS_NOT_SENT_QUERY = "SELECT * FROM " +
            StressNotification.NotificationEntry.TABLE_NAME + " WHERE " +
            StressNotification.NotificationEntry.SENT + " = ?";

    private final String ANSWERS_SELECT_QUERY = "SELECT * FROM " + SurveyResult.SurveyResultEntry
            .TABLE_NAME + " WHERE " + SurveyResult.SurveyResultEntry.SENT + " = ?";

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
    }

    public void saveSurveyAnswers(String answers, boolean sent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SurveyResult.SurveyResultEntry.ANSWERS, answers);
        values.put(SurveyResult.SurveyResultEntry.SENT, sent + "");
        db.insert(SurveyResult.SurveyResultEntry.TABLE_NAME, null, values);
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

    public void saveNotification(Intent intent, boolean successful) {
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String application = intent.getStringExtra("application");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        String event = intent.getStringExtra("event");

        Log.d("title", title);
        Log.d("app", application);
        Log.d("content", content);

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

    public List<SurveyResult> getNotSentResults() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false"};
        Cursor c = db.rawQuery(ANSWERS_SELECT_QUERY, selectionArgs);

        List<SurveyResult> results = loadResults(c);
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

    public void updateAnswersSent(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(SurveyResult.SurveyResultEntry.SENT, "true");

        String selection = SurveyResult.SurveyResultEntry._ID + " LIKE ?";
        String[] selectionArgs = {"" + id};

        db.update(SurveyResult.SurveyResultEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private List<StressNotification> loadNotifications(Cursor c) {
        List<StressNotification> notifications = new ArrayList<>();
        if (c != null) {
            while (!c.isClosed() && c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry._ID));

                String title = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                        .TITLE));
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

                StressNotification notification = new StressNotification(id, title, application,
                        contentLength, emoticons, EmojiFrequency.getEmoticons(emoticons),
                        timeStampDate);
                notifications.add(notification);
            }
        }

        return notifications;
    }

    private List<SurveyResult> loadResults(Cursor c) {
        List<SurveyResult> results = new ArrayList<>();
        if (c != null) {
            while (!c.isClosed() && c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(SurveyResult.SurveyResultEntry._ID));

                String answer = c.getString(c.getColumnIndex(SurveyResult.SurveyResultEntry
                        .ANSWERS));

                SurveyResult result = new SurveyResult(id);
                result.setEntireAnswer(answer);
                results.add(result);
            }
        }
        return results;
    }

    private void addAnswers(List<SurveyResult> results, Cursor c) {
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(StressNotification.NotificationEntry._ID));

                String answers = c.getString(c.getColumnIndex(SurveyResult.SurveyResultEntry
                        .ANSWERS));

                List<String> items = Arrays.asList(answers.split(","));

                SurveyResult result = new SurveyResult(id);
                result.setAnswers(items);
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

    public void saveScreenEvent(String event, boolean successful) {
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
        values.put(StressNotification.NotificationEntry.SENT, "" + successful);
        db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
    }
}
