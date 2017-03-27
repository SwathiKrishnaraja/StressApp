package org.hcilab.projects.stressblocks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hcilab.projects.stressblocks.db.DatabaseService;
import org.hcilab.projects.stressblocks.tetris.utils.NotificationUtils;

public class MainActivity extends AppCompatActivity {
    private DatabaseService dbService;
    private SharedPreferences preferences;
    private AlertDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.dbService = DatabaseService.getInstance(this);
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
                if (NotificationUtils.isLastNotficationMoreThanOneHourAgo(preferences)) {
                    Intent i = new Intent(getApplicationContext(), RatingActivity.class);
                    i.putExtra("activity", "main");
                    startActivity(i);
                    finish();
                } else {
                    infoDialog = getInfoDialog();
                    infoDialog.setMessage("You rated the stress level recently." + "\n" +
                            "Please try it later.");
                    infoDialog.show();
                }
            }
        });

    }

    private AlertDialog getInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);

        final LinearLayout usernameLayout = (LinearLayout) view.findViewById(R.id.username_layout);
        usernameLayout.setVisibility(View.GONE);

        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setVisibility(View.VISIBLE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoDialog != null) {
                    infoDialog.dismiss();
                }
            }
        });

        return builder.create();
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
            int highscore = dbService.getHighScore();
            highscoreView.setText("HIGHSCORE: " + highscore);
        }
    }
}
