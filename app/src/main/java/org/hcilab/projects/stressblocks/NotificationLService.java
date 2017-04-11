package org.hcilab.projects.stressblocks;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Paul Kuznecov on 01.11.2016.
 */

public class NotificationLService extends NotificationListenerService {

    private NotificationReceiver notificationReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("onBind", "" + intent.getAction());
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("notification service", " created");
        IntentFilter filter = new IntentFilter();
        filter.addAction("de.fachstudie.stressapp.notification");

        notificationReceiver = new NotificationReceiver();
        registerReceiver(notificationReceiver, filter);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind", " " + intent.getAction());
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("onRebind", " ");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroyed", " ");
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("ongoing notification", " " + sbn.isOngoing());
        if (sbn.isOngoing()) return;

        super.onNotificationPosted(sbn);
        CharSequence sequenceText = sbn.getNotification().extras.getCharSequence(Notification
                .EXTRA_TEXT);
        String text = sequenceText != null ? sequenceText.toString() : "";
        CharSequence sequenceTitle = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
        String title = sequenceTitle != null ? sequenceTitle.toString() : "";
        String app = sbn.getPackageName();

        Log.d("Notification App: ", app);
        Log.d("Notification Text: ", text);
        Log.d("Notification Title: ", title);

        Intent i = new Intent("de.fachstudie.stressapp.notification");
        i.putExtra("application", app);
        i.putExtra("title_length", title.length() + "");
        i.putExtra("content", text);
        i.putExtra("event", "NOTIFICATION");

        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String app = sbn.getPackageName();
        CharSequence textSequence = sbn.getNotification().extras.getCharSequence(Notification
                .EXTRA_TEXT);
        String text = textSequence != null ? textSequence.toString() : "";
        CharSequence titleSequence = sbn.getNotification().extras.getCharSequence(Notification
                .EXTRA_TITLE);
        String title = titleSequence != null ? titleSequence.toString() : "";

        Log.d("Notification removed", app);

        Intent i = new Intent("de.fachstudie.stressapp.notification");
        i.putExtra("application", app);
        i.putExtra("title_length", title.length() + "");
        i.putExtra("content", text);
        i.putExtra("event", "NOTIFICATION_REMOVED");
        sendBroadcast(i);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d("listener connected", " ");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d("listener disconnected", " ");
    }

    private class NotificationReceiver extends BroadcastReceiver {
        DatabaseService dbService = null;
        public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public void onReceive(Context context, final Intent intent) {
            JSONObject event = new JSONObject();
            try {
                event.put("event", intent.getStringExtra("event"));
                event.put("application", intent.getStringExtra("application"));
                event.put("title_length", intent.getStringExtra("title_length"));
                event.put("content_length", intent.getStringExtra("content").length());
                String timestamp = dateFormat.format(new Date());
                event.put("timestamp", timestamp);
                event.put("emoticons", EmojiFrequency.getCommaSeparatedEmoticons(intent
                        .getStringExtra("content")));
            } catch (JSONException e) {
            }
            dbService = DatabaseService.getInstance(context);
            StressAppClient client = new StressAppClient(context);
            client.sendNotificationEvent(event, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    boolean sent = message.getData().getBoolean("sent");
                    dbService.saveNotification(intent, sent);
                    return false;
                }
            });
        }
    }
}
