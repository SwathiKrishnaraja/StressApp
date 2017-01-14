package de.fachstudie.stressapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
            StressNotification.NotificationEntry.LOADED,
            StressNotification.NotificationEntry.TIMESTAMP,
            StressNotification.NotificationEntry.CONTENT};

    private final String[] surveyResultColumns = {SurveyResult.SurveyResultEntry._ID,
            SurveyResult.SurveyResultEntry.ANSWERS};

    private final String NOTIFICATION_SELECT_QUERY = "SELECT * FROM " + StressNotification.NotificationEntry
            .TABLE_NAME + " WHERE " + StressNotification.NotificationEntry.LOADED + " = ?" +
            " ORDER BY " + StressNotification.NotificationEntry.TIMESTAMP + " ASC";

    private final String SCORE_SELECT_QUERY = "SELECT MAX("+ Score.ScoreEntry.VALUE + ") AS " +
            Score.ScoreEntry.VALUE + " FROM " + Score.ScoreEntry.TABLE_NAME;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DatabaseHelper dbHelper;

    public DatabaseService(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public int getHighScore() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(SCORE_SELECT_QUERY, null);
        int score = 0;

        if (c != null && c.moveToFirst()) {
            score = c.getInt(c.getColumnIndex(Score.ScoreEntry.VALUE));
        }

        closeDatabaseComponents(db, c);
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
        closeDatabaseComponents(db, c);
        return results;
    }

    public List<StressNotification> getNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, notificationColumns, null,
                null, null, null, null);

        addNotifications(notifications, c);
        closeDatabaseComponents(db, c);
        return notifications;
    }


    public List<StressNotification> getNotLoadedNotifications() {
        List<StressNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {"false"};
        Cursor c = db.rawQuery(NOTIFICATION_SELECT_QUERY, selectionArgs);

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
                String application = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry.APPLICATION));
                String timeStampText = c.getString(c.getColumnIndex(StressNotification
                        .NotificationEntry
                        .TIMESTAMP));

                Date timeStampDate = getTimeStampDate(timeStampText);

                boolean loaded = ("true".equals(c.getString(
                        c.getColumnIndex(StressNotification.NotificationEntry.LOADED))));

                StressNotification notification = new StressNotification(id, title, application,
                        content, timeStampDate, loaded);
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

    private void closeDatabaseComponents(SQLiteDatabase db, Cursor c) {
        c.close();
        db.close();
    }
}
