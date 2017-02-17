package de.fachstudie.stressapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static de.fachstudie.stressapp.tetris.utils.NotificationUtils.createNotification;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            if (currentHour < 11) {
                cal.set(Calendar.HOUR_OF_DAY, 11);
                cal.set(Calendar.MINUTE, 0);
            } else if (currentHour < 14) {
                cal.set(Calendar.HOUR_OF_DAY, 14);
                cal.set(Calendar.MINUTE, 0);
            } else if (currentHour < 17) {
                cal.set(Calendar.HOUR_OF_DAY, 17);
                cal.set(Calendar.MINUTE, 0);
            } else if (currentHour < 20) {
                cal.set(Calendar.HOUR_OF_DAY, 20);
                cal.set(Calendar.MINUTE, 0);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 11);
                cal.set(Calendar.MINUTE, 0);
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            createNotification(context);

            PendingIntent sender = PendingIntent.getBroadcast(context, 10, intent, PendingIntent
                    .FLAG_UPDATE_CURRENT);
            // Get the AlarmManager service
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            // am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        } catch (Exception e) {
        }
    }
}
