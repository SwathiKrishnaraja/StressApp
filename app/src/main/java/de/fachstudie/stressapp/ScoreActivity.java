package de.fachstudie.stressapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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

import de.fachstudie.stressapp.model.Score;
import de.fachstudie.stressapp.networking.StressAppClient;
import de.fachstudie.stressapp.score.ScoreAdapter;

import static de.fachstudie.stressapp.tetris.constants.StringConstants.NO_INTERNET_MESSAGE;

public class ScoreActivity extends AppCompatActivity {

    private StressAppClient client;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
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
                    JSONArray jsonUsersIDs = result.getJSONArray("userids");

                    String localUserID = Settings.Secure.getString(getApplicationContext()
                                    .getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String localUsername = preferences.getString("username", "");
                    int localUserScore = result.getInt("userscore");

                    Score[] scores = new Score[jsonScores.length()];
                    boolean userAvailable = false;

                    for (int i = 0; i < jsonScores.length(); i++) {
                        scores[i] = new Score(jsonUsersIDs.optString(i), jsonScores.optInt(i));
                        scores[i].setUsername(jsonUsers.optString(i));
                    }

                    if (!localUserID.isEmpty() && !localUsername.isEmpty()) {
                        for (Score score : scores) {
                            if (localUserID.equals(score.getUserID()) &&
                                    localUsername.equals(score.getUsername())) {
                                userAvailable = true;
                                break;
                            }
                        }
                    }

                    if (!userAvailable && !localUsername.isEmpty()) {
                        scores = Arrays.copyOf(scores, scores.length + 1);
                        scores[scores.length - 1] = new Score(localUserID,
                                localUserScore);
                        scores[scores.length - 1].setUsername(localUsername);
                    }

                    ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, scores);
                    listView.setAdapter(adapter);
                    Log.d("Response", result.toString());
                } catch (JSONException e) {
                    Log.e("JSON exception", e.getClass().toString() + " " + e.getMessage());

                    TextView textView = getInfoTextView();
                    ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, new Score[0]);
                    listView.setAdapter(adapter);
                    listView.addHeaderView(textView);
                }

                return false;
            }
        });
    }

    @NonNull
    private TextView getInfoTextView() {
        TextView textView = new TextView(getApplicationContext());
        textView.setText(NO_INTERNET_MESSAGE);
        textView.setPadding(0, 100, 0, 300);
        textView.setTextColor(Color.RED);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(20);
        return textView;
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
