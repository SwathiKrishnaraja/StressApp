package org.hcilab.projects.stressblocks;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NOTIFICATION_TIMESTAMP;


public class RatingActivity extends AppCompatActivity {

    private StressAppClient client;
    private DatabaseService dbService;
    private SharedPreferences preferences;

    @Override
    public void onBackPressed() {
        if (isPredecessorMainActivity(this.getIntent())) {
            Intent nextActivity = new Intent(this, MainActivity.class);
            startActivity(nextActivity);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        this.preferences = getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String timestamp = dateFormat.format(new Date());

        setContentView(R.layout.activity_rating);

        client = new StressAppClient(this);
        dbService = DatabaseService.getInstance(this);

        final RadioGroup radioGroupStressLevel = (RadioGroup) findViewById(R.id.radio_group_stress_level);
        final String rating = (isPredecessorMainActivity(this.getIntent())) ? "manual" : "time";

        final LinearLayout layout = (LinearLayout) findViewById(R.id.stresslevel_text_layout);
        final Button btnBackToGame = (Button) findViewById(R.id.buttonPlayTetris);
        final Button btnExitApp = (Button) findViewById(R.id.buttonExitApp);

        btnBackToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TetrisActivity.class);
                i.putExtra("message", "stresslevel defined");
                startActivity(i);
                finish();
            }
        });

        btnExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

                if(NotificationUtils.isNLServiceRunning(manager)) {
                    Intent i = new Intent(getApplicationContext(), TetrisActivity.class);
                    i.putExtra("message", "stresslevel defined and exit app");
                    startActivity(i);
                }
                finish();
            }
        });


        radioGroupStressLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();

                if (isChecked) {

                    final int scale = getScale(checkedRadioButton.getText().toString());

                    client.sendStressLevel(scale, rating, new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            boolean sent = message.getData().getBoolean("sent");
                            dbService.saveStressLevel(scale, sent);
                            return false;
                        }
                    });

                    preferences.edit().putString(NOTIFICATION_TIMESTAMP, timestamp).commit();
                    disableRadioButtons(radioGroupStressLevel);

                    layout.setVisibility(View.VISIBLE);

                    if (!btnBackToGame.isEnabled()) {
                        btnBackToGame.setBackgroundColor(getResources().getColor(R.color.green));
                        btnBackToGame.setEnabled(true);
                    }

                    if (!btnExitApp.isEnabled()) {
                        btnExitApp.setBackgroundColor(getResources().getColor(R.color.red));
                        btnExitApp.setEnabled(true);
                    }
                }
            }
        });
    }

    private void disableRadioButtons(RadioGroup radioGroupStressLevel) {
        for (int index = 0; index < radioGroupStressLevel.getChildCount(); index++) {
            radioGroupStressLevel.getChildAt(index).setEnabled(false);
        }
    }

    private int getScale(String option) {
        int scale = 0;
        switch (option) {
            case "very slightly or not at all":
                scale = 1;
                break;
            case "slightly":
                scale = 2;
                break;
            case "somewhat":
                scale = 3;
                break;
            case "moderately":
                scale = 4;
                break;
            case "extremely":
                scale = 5;
                break;
        }
        return scale;
    }

    private boolean isPredecessorMainActivity(Intent predecessorIntent) {
        if (predecessorIntent != null && predecessorIntent.getStringExtra("activity") != null) {
            String predecessorActivity = predecessorIntent.getStringExtra("activity");
            if (predecessorActivity.equals("main")) {
                return true;
            }
        }
        return false;
    }
}
