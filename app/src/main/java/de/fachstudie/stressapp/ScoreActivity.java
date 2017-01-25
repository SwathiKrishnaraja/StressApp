package de.fachstudie.stressapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.fachstudie.stressapp.model.Score;
import de.fachstudie.stressapp.networking.StressAppClient;
import de.fachstudie.stressapp.score.ScoreAdapter;

public class ScoreActivity extends AppCompatActivity {

    private StressAppClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        final ListView listView = (ListView) findViewById(android.R.id.list);
        client = new StressAppClient(this);

        TextView textView = getTextView();
        listView.addHeaderView(textView);

        client.getScores(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                String response = message.getData().getString("response");
                try {
                    JSONObject result = new JSONObject(response);

                    JSONArray jsonScores = result.getJSONArray("scores");
                    JSONArray jsonUsers = result.getJSONArray("users");
                    Score[] scores = new Score[jsonScores.length()];
                    for (int i = 0; i < jsonScores.length(); i++) {
                        scores[i] = new Score(0, jsonScores.optInt(i));
                        scores[i].setUsername(jsonUsers.optString(i));
                    }
                    ScoreAdapter s = new ScoreAdapter(ScoreActivity.this, scores);
                    listView.setAdapter(s);
                    Log.d("Response", result.toString());
                } catch (JSONException e) {
                }

                return false;
            }
        });
    }

    @NonNull
    private TextView getTextView() {
        TextView textView = new TextView(this);
        textView.setText("Scoreboard");
        textView.setPadding(0, 10, 0, 30);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(20);
        return textView;
    }
}
