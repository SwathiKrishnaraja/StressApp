package org.hcilab.projects.stressblocks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        final Button btnStartSurvey = (Button) findViewById(R.id.btn_start_survey);
        btnStartSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SurveyActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
