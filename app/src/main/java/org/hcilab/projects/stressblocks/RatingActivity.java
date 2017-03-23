package org.hcilab.projects.stressblocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.networking.StressAppClient;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NOTIFICATION_TIMESTAMP;


public class RatingActivity extends AppCompatActivity {

    private StressAppClient client;
    private DatabaseService dbService;
    private SharedPreferences preferences;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        this.preferences = getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        preferences.edit().putString(NOTIFICATION_TIMESTAMP, timestamp).commit();

        setContentView(R.layout.activity_rating);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

        client = new StressAppClient(this);
        dbService = DatabaseService.getInstance(this);

        final Button btnSurvey = (Button) findViewById(R.id.buttonStartSurvey);
        btnSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.sendStressLevel(seekBar.getProgress(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        boolean sent = message.getData().getBoolean("sent");
                        dbService.saveStressLevel(seekBar.getProgress(), sent);
                        return false;
                    }
                });
                Intent i = new Intent(getApplicationContext(), SurveyActivity.class);
                i.putExtra("message", "stresslevel defined");
                startActivity(i);
                finish();
            }
        });

        final Button btnBackToGame = (Button) findViewById(R.id.buttonResumeToGame);
        btnBackToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.sendStressLevel(seekBar.getProgress(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        boolean sent = message.getData().getBoolean("sent");
                        dbService.saveStressLevel(seekBar.getProgress(), sent);
                        return false;
                    }
                });

                Intent i = new Intent(getApplicationContext(), TetrisActivity.class);
                i.putExtra("message", "stresslevel defined");
                startActivity(i);
                finish();
            }
        });

        final Button btnExitApp = (Button) findViewById(R.id.buttonExitApp);
        btnExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.sendStressLevel(seekBar.getProgress(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        boolean sent = message.getData().getBoolean("sent");
                        dbService.saveStressLevel(seekBar.getProgress(), sent);
                        return false;
                    }
                });
                Intent i = new Intent(getApplicationContext(), TetrisActivity.class);
                i.putExtra("message", "stresslevel defined and exit app");
                startActivity(i);
                finish();
            }
        });

        final TextView seekBarValue = (TextView) findViewById(R.id.textViewStresslevel);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (!btnSurvey.isEnabled()) {
                    btnSurvey.setEnabled(true);
                }

                if (!btnBackToGame.isEnabled()) {
                    btnBackToGame.setEnabled(true);
                }

                if (!btnExitApp.isEnabled()) {
                    btnExitApp.setEnabled(true);
                }
                seekBarValue.setText("StressLevel: " + String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }
}
