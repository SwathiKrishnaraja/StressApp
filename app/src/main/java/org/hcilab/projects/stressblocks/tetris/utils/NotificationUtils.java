package org.hcilab.projects.stressblocks.tetris.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.hcilab.projects.stressblocks.R;
import org.hcilab.projects.stressblocks.RatingActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NOTIFICATION_TIMESTAMP;

/**
 * Created by Sanjeev on 16.02.2017.
 */

public class NotificationUtils {

    public static boolean isLastNotficationLongAgo(SharedPreferences preferences){
        String timestamp = preferences.getString(NOTIFICATION_TIMESTAMP, "");

        if (!timestamp.isEmpty()) {
            Log.d("last notification date", timestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(dateFormat.parse(timestamp));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar currentCal = Calendar.getInstance();

            int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
            int previousHour = cal.get(Calendar.HOUR_OF_DAY);

            int diffMonths = currentCal.get(Calendar.MONTH) - cal.get(Calendar.MONTH);
            int diffDays = currentCal.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH);
            int diffHours = currentHour - previousHour;

            if (diffMonths > 0) {
                return true;
            } else if (diffDays > 0) {
                return true;
            } else if (diffDays == 1 && currentHour > 10) {
                return true;
            } else if (currentHour > 10 && diffHours > 3) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLastNotficationMoreThanOneHourAgo(SharedPreferences preferences){
        String timestamp = preferences.getString(NOTIFICATION_TIMESTAMP, "");

        if (!timestamp.isEmpty()) {
            Log.d("last notification date", timestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(dateFormat.parse(timestamp));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar currentCal = Calendar.getInstance();

            long diffMillis = currentCal.getTimeInMillis() - cal.getTimeInMillis();


            if (diffMillis >= 60 * 60 * 1000) {
                return true;
            }else{
                return false;
            }
        }
        return true;
    }


    public static void createNotification(Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Are you feeling stressed?")
                        .setContentText("Please take few seconds to rate your stress level and gain extra blocks!");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, RatingActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(RatingActivity.class);
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
    }

}
