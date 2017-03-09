package org.hcilab.projects.stressblocks;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class ScreenStatusReceiverService extends Service {
    private BroadcastReceiver lockScreenReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startIs) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        lockScreenReceiver = new LockScreenReceiver();

        registerReceiver(lockScreenReceiver, filter);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        if (lockScreenReceiver != null) {
            super.onDestroy();
            unregisterReceiver(lockScreenReceiver);
        }
    }
}
