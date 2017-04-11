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

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.hcilab.projects.stressblocks.tetris.TetrisView;
import org.hcilab.projects.stressblocks.tetris.utils.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.GOLD_BLOCKS;
import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NOTIFICATION_TIMESTAMP;
import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.USER_SCORES;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.isNLServiceRunning;
import static org.hcilab.projects.stressblocks.tetris.utils.ScoreUtils.addHighscoreToPreferences;

public class TetrisActivity extends AppCompatActivity {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private NotificationReceiver notificationReceiver;
    private IntentFilter filter;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("onCreate tetris", " ");
        setContentView(R.layout.activity_tetris);

        client = new StressAppClient(this);
        dbService = DatabaseService.getInstance(this);

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

        exitDialog = getCustomDialog(false, false, true, true);
        gameOverDialog = getCustomDialog(true, true, false, false);
        Handler handler = createGameOverHandler();

        tetrisView = (TetrisView) findViewById(R.id.tetrisview);
        tetrisView.setHandler(handler);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (!isNLServiceRunning(manager)) {
            if (userInfoDialog == null)
                userInfoDialog = DialogUtils.getUserInfoDialog(this);
        } else {
            createReceivers();
        }

        Intent serviceIntent = new Intent(this, ScreenStatusReceiverService.class);
        startService(serviceIntent);

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

        doActionAfterRatingStressLevel(getIntent());

    }

    private void createReceivers() {
        notificationReceiver = new NotificationReceiver();
        filter = new IntentFilter();
        filter.addAction("de.fachstudie.stressapp.notification");
        registerReceiver(notificationReceiver, filter);
        receiversCreated = true;
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

                if (preferences != null) {
                    String localUsername = preferences.getString("username", "");
                    if (localUsername != null && !localUsername.isEmpty()) {
                        gameOverDialog.setMessage("USER: " + localUsername + "\n" + "\n" +
                                "HIGHSCORE: " + highScore + "\n" + "\n" +
                                "SCORE: " + data.getInt("score"));
                    }
                }

                if (!isFinishing()) {
                    gameOverDialog.show();
                }
            }
        };
    }

    private AlertDialog getCustomDialog(boolean newGame, boolean topScores, boolean exit,
                                        boolean cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TetrisActivity.this);
        View view = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);

        final LinearLayout usernameLayout = (LinearLayout) view.findViewById(R.id.username_layout);
        final Button newGameBtn = (Button) view.findViewById(R.id.start_new_game_btn);
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

        final Button scoreboardBtn = (Button) view.findViewById(R.id.btn_view_scoreboard);
        if (topScores) {
            scoreboardBtn.setVisibility(View.VISIBLE);
            scoreboardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent score_activity = new Intent(TetrisActivity.this, ScoreActivity.class);
                    startActivity(score_activity);
                    usernameLayout.setVisibility(View.GONE);
                }
            });
        }

        Button exitBtn = (Button) view.findViewById(R.id.btn_start_game);
        if (exit) {
            exitBtn.setVisibility(View.VISIBLE);
            exitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exitDialog.dismiss();
                }
            });
        }

        Button cancelBtn = (Button) view.findViewById(R.id.btn_ok);
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
                    if (!editable.toString().isEmpty() && editable.toString().length() < 25) {
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
                                getApplicationContext().getSharedPreferences("de.fachstudie" +
                                                ".stressapp.preferences",
                                        Context.MODE_PRIVATE).getString("username", ""));
                        addHighscoreToPreferences(client, preferences);
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
        doActionAfterRatingStressLevel(intent);
    }

    private void doActionAfterRatingStressLevel(Intent intent) {
        Bundle bundle = intent.getExtras();
        String message = getMessage(bundle);
        Log.d("message", message);
        if (message.equals("stresslevel defined")) {
            if (tetrisView != null) {
                tetrisView.increaseGoldBlockCount(2);
            }
        } else if (message.equals("stresslevel defined and exit app")) {
            if (tetrisView != null) {
                tetrisView.increaseGoldBlockCount(2);
                moveTaskToBack(true);
            }
        } else if (message.equals("exit app")) {
            moveTaskToBack(true);
        }
    }

    private String getMessage(Bundle bundle) {
        if (bundle != null && bundle.getString("message") != null) {
            return bundle.getString("message");
        }
        return "";
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        Log.d("onResume tetris", " ");

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (!isNLServiceRunning(manager)) {
            if (userInfoDialog == null) {
                userInfoDialog = DialogUtils.getUserInfoDialog(this);
            }
            userInfoDialog.show();
            if (tetrisView != null) {
                tetrisView.pauseGame();
            }
        } else if (!receiversCreated) {
            createReceivers();
        } else if (gameOverDialog != null && !gameOverDialog.isShowing() && !tetrisView.isPause()) {
            tetrisView.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
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
                Intent score_activity = new Intent(TetrisActivity.this, ScoreActivity.class);
                startActivity(score_activity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            tetrisView.notificationPosted();
        }
    }
}
