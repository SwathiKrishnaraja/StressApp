package org.hcilab.projects.stressblocks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Random;

import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.createNotification;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Random r = new Random();
            int hourOffset = r.nextInt(2);
            int minuteOffset = r.nextInt(60);
            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);

            if (currentHour < 10) {
                cal.set(Calendar.HOUR_OF_DAY, 10 + hourOffset);
                cal.set(Calendar.MINUTE, minuteOffset);
            } else if (currentHour < 13) {
                cal.set(Calendar.HOUR_OF_DAY, 13 + hourOffset);
                cal.set(Calendar.MINUTE, minuteOffset);
            } else if (currentHour < 16) {
                cal.set(Calendar.HOUR_OF_DAY, 16 + hourOffset);
                cal.set(Calendar.MINUTE, minuteOffset);
            } else if (currentHour < 19) {
                cal.set(Calendar.HOUR_OF_DAY, 19 + hourOffset);
                cal.set(Calendar.MINUTE, minuteOffset);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 10 + hourOffset);
                cal.set(Calendar.MINUTE, minuteOffset);
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
