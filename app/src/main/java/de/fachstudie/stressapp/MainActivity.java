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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.vdurmont.emoji.Emoji;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fachstudie.stressapp.databinding.ActivityMainBinding;
import de.fachstudie.stressapp.db.DatabaseHelper;
import de.fachstudie.stressapp.model.StressNotification;
import de.fachstudie.stressapp.model.ViewNotification;

public class MainActivity extends AppCompatActivity {
    private Button tetris_button;
    private ViewNotification notification;
    private NotificationReceiver notificationReceiver;
    private IntentFilter filter;
    private BarChart barChart;

    public void changeActivity(View view){
        Intent intent = new Intent(MainActivity.this, TetrisActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        notification = new ViewNotification();
        notification.title.set("Title");
        notification.content.set("Content");
        notification.application.set("Application");
        notification.timestamp.set("Timestamp");
        binding.setCurrentNotification(notification);

        notificationReceiver = new NotificationReceiver();
        filter = new IntentFilter();
        filter.addAction("com.test");
        registerReceiver(notificationReceiver, filter);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {ViewNotification.NotificationEntry._ID, ViewNotification
                .NotificationEntry.TITLE, ViewNotification.NotificationEntry.APPLICATION,
                ViewNotification.NotificationEntry.TIMESTAMP, ViewNotification
                .NotificationEntry.CONTENT};
        Cursor c = db.query(ViewNotification.NotificationEntry.TABLE_NAME, projection, null,
                null, null, null, null);

        List<StressNotification> notificationList = new ArrayList<>();
        EmojiFrequency frequency = new EmojiFrequency();
        if (c != null && c.moveToFirst()) {
            Log.d("Count: ", c.getCount() + "");

            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndex(ViewNotification.NotificationEntry
                        .TITLE));
                String content = c.getString(c.getColumnIndex(ViewNotification.NotificationEntry
                        .CONTENT));
                String application = c.getString(c.getColumnIndex(ViewNotification.NotificationEntry
                        .APPLICATION));
                String timeStampText = c.getString(c.getColumnIndex(ViewNotification
                        .NotificationEntry
                        .TIMESTAMP));

                for(Map.Entry<Emoji, Integer> entry: frequency.getEmojiFrequenciesFromText(content).entrySet()){
                    Log.d("key", entry.getKey().getUnicode());
                    Log.d("value", ""+entry.getValue());

                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date timeStampDate = null;
                try {
                    timeStampDate = sdf.parse(timeStampText);
                } catch (ParseException e) {
                }

                StressNotification not = new StressNotification(title, application, content,
                        timeStampDate);
                notificationList.add(not);
            }

            db.close();
            dbHelper.close();
        }

        barChart = (BarChart) findViewById(R.id.chart);
        barChart.getXAxis().setDrawGridLines(false);
        Description d = new Description();
        d.setText("Stunde");
        barChart.setDescription(d);

        Map<Integer, Integer> dayHourToNotificationCount = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            dayHourToNotificationCount.put(i, 0);
        }

        for (StressNotification n : notificationList) {
            Date timestamp = n.getTimestamp();
            if (timestamp == null) {
                continue;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(timestamp);
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            dayHourToNotificationCount.put(hourOfDay,
                    dayHourToNotificationCount.get(hourOfDay) + 1);
        }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            BarEntry entry = new BarEntry(i, dayHourToNotificationCount.get(i));
            entries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(entries, "# Notifications");
        barDataSet.setColor(ContextCompat.getColor(this, R.color.barChartColor));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();

        Log.d("Notifications: ", notificationList.size() + "");

        if(!notificationList.isEmpty()) {
            StressNotification last = notificationList.get(notificationList.size() - 1);
            notification.title.set(last.getTitle());
            notification.content.set(last.getContent());
            notification.application.set(last.getApplication());
            notification.timestamp.set(last.getTimestamp().toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //lineChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());

            notification.title.set(title);
            notification.content.set(content);
            notification.application.set(application);
            notification.timestamp.set(timestamp);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ViewNotification.NotificationEntry.TITLE, title);
            values.put(ViewNotification.NotificationEntry.CONTENT, content);
            values.put(ViewNotification.NotificationEntry.APPLICATION, application);
            db.insert(ViewNotification.NotificationEntry.TABLE_NAME, null, values);
        }
    }
}
