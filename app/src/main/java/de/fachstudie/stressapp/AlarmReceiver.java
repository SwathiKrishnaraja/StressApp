package de.fachstudie.stressapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

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

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle("Are you feeling stressed?")
                            .setContentText("Please take a minute to answer our survey!");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, RatingActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());
            // 11 Uhr 14 Uhr 17 Uhr 20 Uhr

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
