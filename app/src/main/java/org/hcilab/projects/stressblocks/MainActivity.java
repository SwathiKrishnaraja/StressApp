package org.hcilab.projects.stressblocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.model.StressLevel;
import org.hcilab.projects.stressblocks.model.StressNotification;
import org.hcilab.projects.stressblocks.networking.StressAppClient;

import java.util.List;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.HIGHSCORE;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.createNotification;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.isLastNotficationLongAgo;
import static org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils.loadJSONObject;
import static org.hcilab.projects.stressblocks.tetris.utils.ScoreUtils.addHighscoreToPreferences;

/**
 * Represents the main instance of the application.
 */
public class MainActivity extends AppCompatActivity {
    private DatabaseService dbService;
    private StressAppClient client;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate main", " ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.dbService = DatabaseService.getInstance(this);
        this.client = new StressAppClient(this);
        this.preferences = this.getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);

        this.setUsernameView();
        this.setHighscoreView();

        final Button btnStartGame = (Button) findViewById(R.id.btn_start_game);
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TetrisActivity.class);
                startActivity(i);
                finish();
            }
        });

        final Button btnViewScoreboard = (Button) findViewById(R.id.btn_view_scoreboard);
        btnViewScoreboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ScoreActivity.class);
                i.putExtra("activity", "main");
                startActivity(i);
                finish();
            }
        });

        final Button btnRateStressLevel = (Button) findViewById(R.id.btn_rate_stress_level);
        btnRateStressLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RatingActivity.class);
                i.putExtra("activity", "main");
                startActivity(i);
                finish();

            }
        });

        final Button btnExitApp = (Button) findViewById(R.id.btn_exit_stress_blocks);
        btnExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        this.sendNotifications();
        this.sendStressLevels();

        if (isLastNotficationLongAgo(preferences)) {
            createNotification(getApplicationContext());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume main", " ");
    }

    private void setUsernameView() {
        final TextView usernameView = (TextView) findViewById(R.id.text_view_username);
        if (preferences != null) {
            String localUsername = preferences.getString("username", "");
            if (localUsername != null && !localUsername.isEmpty()) {
                usernameView.setText("USER: " + localUsername);
            }
        }
    }

    private void setHighscoreView() {
        final TextView highscoreView = (TextView) findViewById(R.id.text_view_highscore);
        if (dbService != null) {
            if (preferences.getInt(HIGHSCORE, 0) == 0) {
                addHighscoreToPreferences(client, preferences);
            }

            int prefsHighscore = preferences.getInt(HIGHSCORE, 0);
            int dbHighscore = dbService.getHighScore();
            int highscore = (dbHighscore >= prefsHighscore) ? dbHighscore : prefsHighscore;
            highscoreView.setText("HIGHSCORE: " + highscore);
        }
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

    private void sendStressLevels() {
        if (dbService != null && client != null) {
            List<StressLevel> results = dbService.getNotSentStressLevels();
            if (!results.isEmpty()) {
                for (final StressLevel result : results) {
                    client.sendStressLevel(result.getValue(), "", new Handler.Callback() {
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
}
