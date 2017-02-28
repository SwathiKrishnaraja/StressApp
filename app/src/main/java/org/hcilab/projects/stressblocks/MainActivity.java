package org.hcilab.projects.stressblocks;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.model.StressLevel;
import org.hcilab.projects.stressblocks.model.StressNotification;
import org.hcilab.projects.stressblocks.model.SurveyResult;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.hcilab.projects.stressblocks.tetris.TetrisView;
import org.hcilab.projects.stressblocks.tetris.constants.StringConstants;
import org.hcilab.projects.stressblocks.tetris.utils.DialogUtils;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.GOLD_BLOCKS;
import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NOTIFICATION_TIMESTAMP;
import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.USER_SCORES;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.createNotification;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.isLastNotficationLongAgo;

public class MainActivity extends AppCompatActivity {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private NotificationReceiver notificationReceiver;
    private LockScreenReceiver lockScreenReceiver;
    private IntentFilter filter;
    private IntentFilter filterLock;
    private TetrisView tetrisView;
    private AlertDialog userInfoDialog;
    private AlertDialog exitDialog;
    private AlertDialog gameOverDialog;
    private StressAppClient client;
    private int highScore;
    private boolean receiversCreated = false;
    private DatabaseService dbService;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = new StressAppClient(this);
        dbService = DatabaseService.getInstance(this);

        exitDialog = getCustomDialog(false, false, true, true);
        gameOverDialog = getCustomDialog(true, true, false, false);
        Handler handler = createGameOverHandler();

        tetrisView = (TetrisView) findViewById(R.id.tetrisview);
        tetrisView.setHandler(handler);

        preferences = getSharedPreferences("de.fachstudie.stressapp.preferences",
                Context.MODE_PRIVATE);

        if (preferences.getInt(GOLD_BLOCKS, -1) == -1) {
            preferences.edit().putInt(GOLD_BLOCKS, 0).commit();
        }

        if (preferences.getString(NOTIFICATION_TIMESTAMP, "-1").equals("-1")) {
            preferences.edit().putString(NOTIFICATION_TIMESTAMP, "").commit();
        }

        if (preferences.getString(USER_SCORES, "-1").equals("-1")) {
            preferences.edit().putString(USER_SCORES, "").commit();
        }

        if (!isNLServiceRunning()) {
            if (userInfoDialog == null)
                userInfoDialog = DialogUtils.getUserInfoDialog(this);
        } else {
            createReceivers();
        }

        // get a Calendar object with current time
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

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("de.fachstudie.stressapp.notification", "Test");

        // In reality, you would want to have a static variable for the request code instead of
        // 192837
        PendingIntent sender = PendingIntent.getBroadcast(this, 10, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        // am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }

    private void createReceivers() {
        notificationReceiver = new NotificationReceiver();
        filter = new IntentFilter();
        filter.addAction("de.fachstudie.stressapp.notification");
        registerReceiver(notificationReceiver, filter);

        lockScreenReceiver = new LockScreenReceiver();
        filterLock = new IntentFilter();
        filterLock.addAction(Intent.ACTION_SCREEN_ON);
        filterLock.addAction(Intent.ACTION_SCREEN_OFF);
        filterLock.addAction(Intent.ACTION_USER_PRESENT);

        registerReceiver(lockScreenReceiver, filterLock);
        receiversCreated = true;
    }

    private boolean isNLServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer
                .MAX_VALUE)) {
            if (NotificationRegisterService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private Handler createGameOverHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                highScore = data.getInt("highscore");
                gameOverDialog.setTitle("GAME OVER");
                gameOverDialog.setMessage("HIGHSCORE: " + highScore + "\n" + "\n" +
                        "SCORE: " + data.getInt("score"));
                if (!isFinishing()) {
                    gameOverDialog.show();
                }
            }
        };
    }

    private AlertDialog getCustomDialog(boolean newGame, boolean topScores, boolean exit,
                                        boolean cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);

        final LinearLayout usernameLayout = (LinearLayout) view.findViewById(R.id.username_layout);
        final Button newGameBtn = (Button) view.findViewById(R.id.start_new_game_btn);
        newGameBtn.setVisibility(View.GONE);

        if (newGame) {
            newGameBtn.setVisibility(View.VISIBLE);
            newGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tetrisView.startNewGame();
                    usernameLayout.setVisibility(View.GONE);
                    gameOverDialog.dismiss();
                }
            });
        }

        final Button scoreboardBtn = (Button) view.findViewById(R.id.scoreboard_btn);
        scoreboardBtn.setVisibility(View.GONE);
        if (topScores) {
            scoreboardBtn.setVisibility(View.VISIBLE);
            scoreboardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent score_activity = new Intent(MainActivity.this, ScoreActivity.class);
                    startActivity(score_activity);
                    usernameLayout.setVisibility(View.GONE);
                }
            });
        }

        Button exitBtn = (Button) view.findViewById(R.id.exit_btn);
        exitBtn.setVisibility(View.GONE);
        if (exit) {
            exitBtn.setVisibility(View.VISIBLE);
            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.exit(0);
                }
            });
        }

        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setVisibility(View.GONE);

        if (cancel) {
            cancelBtn.setVisibility(View.VISIBLE);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tetrisView.resumeGame();
                    exitDialog.dismiss();
                }
            });
        }

        final EditText usernameEdit = (EditText) view.findViewById(R.id.username_edit);
        final SharedPreferences preferences = this.getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
        if (preferences.getString("username", "").isEmpty() && (newGame || topScores)) {
            usernameLayout.setVisibility(View.VISIBLE);
            newGameBtn.setEnabled(false);
            newGameBtn.setBackgroundColor(getResources().getColor(R.color.lightgray));
            scoreboardBtn.setEnabled(false);
            scoreboardBtn.setBackgroundColor(getResources().getColor(R.color.lightgray));
            usernameEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!editable.toString().isEmpty()) {
                        newGameBtn.setEnabled(true);
                        newGameBtn.setBackgroundColor(getResources().getColor(R.color.green));
                        scoreboardBtn.setEnabled(true);
                        scoreboardBtn.setBackgroundColor(getResources().getColor(R.color.orange));
                        preferences.edit().putString("username", editable.toString()).commit();
                    } else {
                        newGameBtn.setEnabled(false);
                        newGameBtn.setBackgroundColor(getResources().getColor(R.color.lightgray));
                        scoreboardBtn.setEnabled(false);
                        scoreboardBtn.setBackgroundColor(getResources().getColor(R.color
                                .lightgray));
                    }
                }
            });
            usernameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        client.sendScore(getApplicationContext(), highScore,
                                getApplicationContext().getSharedPreferences("de.fachstudie.stressapp.preferences",
                                        Context.MODE_PRIVATE).getString("username", ""));
                    }
                }
            });
        } else {
            usernameLayout.setVisibility(View.GONE);
            Log.d("Invisible", "true");
        }

        return builder.create();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getString("message") != null && tetrisView != null) {
            String message = bundle.getString("message");
            Log.d("message", message);
            if (message.equals("stresslevel defined")) {
                tetrisView.increaseGoldBlockCount(1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        tetrisView.pauseGame();
        exitDialog.setMessage(StringConstants.EXIT_MESSAGE);
        exitDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        Log.d("onResume", " ");
        sendNotifications();
        sendSurveyResults();
        sendStressLevels();
        if (!isNLServiceRunning()) {
            if (userInfoDialog == null) {
                userInfoDialog = DialogUtils.getUserInfoDialog(this);
            }
            userInfoDialog.show();
            if (tetrisView != null) {
                tetrisView.pauseGame();
            }
        } else if (!receiversCreated) {
            createReceivers();
        } else if (!gameOverDialog.isShowing() && !tetrisView.isPause()) {
            tetrisView.resumeGame();
            if (isLastNotficationLongAgo(preferences)) {
                createNotification(getApplicationContext());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
        }

        if (lockScreenReceiver != null) {
            unregisterReceiver(lockScreenReceiver);
        }
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
        // no inspection SimplifiableIfStatement

        switch (item.getItemId()) {
            case R.id.action_score:
                Intent score_activity = new Intent(MainActivity.this, ScoreActivity.class);
                startActivity(score_activity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void sendNotifications() {
        if (dbService != null && client != null) {
            List<StressNotification> results = dbService.getNotSentNotifications();
            if (!results.isEmpty()) {
                for (final StressNotification result : results) {
                    client.sendNotificationEvent(loadJSONObject(result), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            boolean sent = message.getData().getBoolean("sent");
                            if (sent)
                                dbService.updateNotificationIsSent(result.getId());
                            return false;
                        }
                    });
                }
            }
        }
    }

    private JSONObject loadJSONObject(StressNotification notification) {
        JSONObject event = new JSONObject();
        try {
            event.put("event", notification.getEvent());
            event.put("application", notification.getApplication());
            event.put("title_length", notification.getTitleLength());
            event.put("content_length", notification.getContentLength());
            String timestamp = dateFormat.format(notification.getTimestamp());
            event.put("timestamp", timestamp);
            event.put("emoticons", notification.getEmoticons());
        } catch (JSONException e) {
        }

        return event;
    }

    private void sendSurveyResults() {
        if (dbService != null && client != null) {
            List<SurveyResult> results = dbService.getNotSentSurveyResults();
            if (!results.isEmpty()) {
                for (final SurveyResult result : results) {
                    client.sendSurveyAnswers(result.getEntireAnswer(), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            boolean sent = message.getData().getBoolean("sent");
                            if (sent)
                                dbService.updateAnswersSent(result.getId());
                            return false;
                        }
                    });
                }
            }
        }
    }

    private void sendStressLevels() {
        if (dbService != null && client != null) {
            List<StressLevel> results = dbService.getNotSentStressLevels();
            if (!results.isEmpty()) {
                for (final StressLevel result : results) {
                    client.sendStressLevel(result.getValue(), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            boolean sent = message.getData().getBoolean("sent");
                            if (sent)
                                dbService.updateStressLevelIsSent(result.getId());
                            return false;
                        }
                    });
                }
            }
        }
    }

    private class NotificationReceiver extends BroadcastReceiver {
        DatabaseService dbService = null;

        public NotificationReceiver() {
            dbService = DatabaseService.getInstance(MainActivity.this);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            tetrisView.notificationPosted();
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

    private class LockScreenReceiver extends BroadcastReceiver {
        DatabaseService dbService = null;

        public LockScreenReceiver() {
            dbService = DatabaseService.getInstance(MainActivity.this);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
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
                    sendNotificationEvent(event, "SCREEN_ON");

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
                    sendNotificationEvent(event, "SCREEN_LOCK");

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
                    sendNotificationEvent(event, "SCREEN_UNLOCKED");
                }
            }
        }

        private void sendNotificationEvent(JSONObject event, final String screenEvent) {
            client.sendNotificationEvent(event, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    boolean sent = message.getData().getBoolean("sent");
                    dbService.saveScreenEvent(screenEvent, sent);
                    return false;
                }
            });
        }
    }
}
