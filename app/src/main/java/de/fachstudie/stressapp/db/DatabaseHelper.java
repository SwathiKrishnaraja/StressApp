package de.fachstudie.stressapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Paul Kuznecov on 10.11.2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stress.db";
    private static final int DATABASE_VERSION = 1;
    private static final String NOTIFICATION_DATABASE_CREATE = "CREATE TABLE notification " +
            "( _id integer primary key autoincrement, title text," +
            " application text, content_length integer, emoticons text," +
            " loaded text, timestamp datetime default current_timestamp, event text)";

    private static final String SURVEY_RESULT_DATABASE_CREATE = "CREATE TABLE survey_result " +
            "( _id integer primary key autoincrement, answers text)";

    private static final String SCORE_DATABASE_CREATE = "CREATE TABLE score " +
            "( _id integer primary key autoincrement, value integer)";

    private static DatabaseHelper dbHelper;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NOTIFICATION_DATABASE_CREATE);
        db.execSQL(SURVEY_RESULT_DATABASE_CREATE);
        db.execSQL(SCORE_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
