package de.fachstudie.stressapp;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Paul Kuznecov on 01.11.2016.
 */

public class NotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        CharSequence text = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence title = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);

        Log.d("Notification Text: ", text.toString());
        Log.d("Notification Title: ", title.toString());
    }
}
