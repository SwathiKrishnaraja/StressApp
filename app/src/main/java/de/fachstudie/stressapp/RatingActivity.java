package de.fachstudie.stressapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.networking.StressAppClient;


public class RatingActivity extends AppCompatActivity {

    private StressAppClient client;
    private DatabaseService dbService;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                startActivity(i);
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
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
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
