package org.hcilab.projects.stressblocks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles the smartphone-screen-activity of an user.
 */
public class LockScreenReceiver extends BroadcastReceiver {

    DatabaseService dbService = null;
    public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private void sendNotificationEvent(JSONObject event, final String screenEvent, StressAppClient client) {
        client.sendNotificationEvent(event, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                boolean sent = message.getData().getBoolean("sent");
                dbService.saveScreenEvent(screenEvent, sent);
                return false;
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dbService = DatabaseService.getInstance(context);
        StressAppClient client = new StressAppClient(context);

        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // Screen is on but not unlocked (if any locking mechanism present)
                Log.i("LockScreenReceiver", "Screen is on but not unlocked");
                JSONObject event = new JSONObject();
                try {
                    event.put("event", "SCREEN_ON");
                    String timestamp = dateFormat.format(new Date());
                    event.put("timestamp", timestamp);
                    event.put("application", "");
                    event.put("title_length", "");
                    event.put("content_length", 0);
                    event.put("emoticons", "");
                } catch (JSONException e) {
                }
                sendNotificationEvent(event, "SCREEN_ON", client);

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Screen is locked
                Log.i("LockScreenReceiver", "Screen is locked");
                JSONObject event = new JSONObject();
                try {
                    event.put("event", "SCREEN_LOCK");
                    String timestamp = dateFormat.format(new Date());
                    event.put("timestamp", timestamp);
                    event.put("application", "");
                    event.put("title_length", "");
                    event.put("content_length", 0);
                    event.put("emoticons", "");
                } catch (JSONException e) {
                }
                sendNotificationEvent(event, "SCREEN_LOCK", client);

            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                // Screen is unlocked
                Log.i("LockScreenReceiver", "Screen is unlocked");
                JSONObject event = new JSONObject();
                try {
                    event.put("event", "SCREEN_UNLOCK");
                    String timestamp = dateFormat.format(new Date());
                    event.put("timestamp", timestamp);
                    event.put("application", "");
                    event.put("title_length", "");
                    event.put("content_length", 0);
                    event.put("emoticons", "");
                } catch (JSONException e) {
                }
                sendNotificationEvent(event, "SCREEN_UNLOCKED", client);
            }
        }
    }
}
