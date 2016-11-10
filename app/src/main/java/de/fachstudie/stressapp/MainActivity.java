package de.fachstudie.stressapp;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.fachstudie.stressapp.databinding.ActivityMainBinding;
import de.fachstudie.stressapp.db.DatabaseHelper;
import de.fachstudie.stressapp.model.StressNotification;

public class MainActivity extends AppCompatActivity {
    private StressNotification notification;
    private NotificationReceiver notificationReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        notification = new StressNotification();
        notification.title.set("Title");
        notification.content.set("Content");
        notification.application.set("Application");
        binding.setCurrentNotification(notification);

        notificationReceiver = new NotificationReceiver();
        filter = new IntentFilter();
        filter.addAction("com.test");
        registerReceiver(notificationReceiver, filter);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {StressNotification.NotificationEntry._ID, StressNotification
                .NotificationEntry.TITLE, StressNotification.NotificationEntry.APPLICATION,
                StressNotification.NotificationEntry.TIMESTAMP, StressNotification
                .NotificationEntry.CONTENT};
        Cursor c = db.query(StressNotification.NotificationEntry.TABLE_NAME, projection, null,
                null, null, null, null);

        if (c != null) {
            c.moveToFirst();
            String dbTitle = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                    .TITLE));
            String dbContent = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                    .CONTENT));
            String dbApplication = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                    .APPLICATION));
            String timeStamp = c.getString(c.getColumnIndex(StressNotification.NotificationEntry
                    .TIMESTAMP));
            Log.d("Timestamp", timeStamp);

            notification.title.set(dbTitle);
            notification.content.set(dbContent);
            notification.application.set(dbApplication);

            db.close();
            dbHelper.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(notificationReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NotificationReceiver extends BroadcastReceiver {
        DatabaseHelper dbHelper = null;

        public NotificationReceiver() {
            dbHelper = new DatabaseHelper(MainActivity.this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String application = intent.getStringExtra("application");

            notification.title.set(title);
            notification.content.set(content);
            notification.application.set(application);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(StressNotification.NotificationEntry.TITLE, title);
            values.put(StressNotification.NotificationEntry.CONTENT, content);
            values.put(StressNotification.NotificationEntry.APPLICATION, application);
            db.insert(StressNotification.NotificationEntry.TABLE_NAME, null, values);
        }
    }
}
