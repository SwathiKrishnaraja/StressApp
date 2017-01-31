package de.fachstudie.stressapp;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.Arrays;

import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.model.Score;
import de.fachstudie.stressapp.networking.StressAppClient;
import de.fachstudie.stressapp.score.ScoreAdapter;

public class ScoreActivity extends AppCompatActivity {

    private StressAppClient client;
    private SharedPreferences preferences;
    private DatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
        this.dbService = DatabaseService.getInstance(getApplicationContext());
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
                    String username = preferences.getString("username", "");
                    boolean userAvailable = true;

                    for (int i = 0; i < jsonScores.length(); i++) {
                        scores[i] = new Score(0, jsonScores.optInt(i));
                        scores[i].setUsername(jsonUsers.optString(i));

                        // TODO check deviceid
                        if (!username.isEmpty() && !username.equals(scores[i].getUsername())) {
                            userAvailable = false;
                        }
                    }

                    if (!userAvailable) {
                        scores = Arrays.copyOf(scores, scores.length + 1);
                        scores[scores.length - 1] = new Score(scores.length - 1,
                                dbService.getHighScore());
                        scores[scores.length - 1].setUsername(username);
                    }

                    ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, scores);
                    listView.setAdapter(adapter);
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
