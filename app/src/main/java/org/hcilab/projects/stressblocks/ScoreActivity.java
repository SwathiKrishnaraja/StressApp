package org.hcilab.projects.stressblocks;

import android.content.Context;
import android.content.Intent;
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

import org.hcilab.projects.stressblocks.model.Score;
import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.hcilab.projects.stressblocks.score.ScoreAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.NO_INTERNET_MESSAGE;
import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.USER_SCORES;

public class ScoreActivity extends AppCompatActivity {

    private StressAppClient client;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
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
                        scores[i] = new Score(jsonUsersIDs.optString(i), jsonUsers.optString(i),
                                jsonScores.optInt(i));
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
                        scores[scores.length - 1] = new Score(localUserID, localUsername,
                                localUserScore);
                    }

                    cacheUserScores(scores);

                    ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, scores);
                    listView.setAdapter(adapter);
                    Log.d("Response", result.toString());
                } catch (JSONException e) {
                    Log.e("JSON exception", e.getClass().toString() + " " + e.getMessage());

                    String userScores = preferences.getString(USER_SCORES, "");
                    if (!userScores.isEmpty()) {
                        Score[] scores = getCachedScores(userScores);
                        if (scores != null) {
                            ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, scores);
                            listView.setAdapter(adapter);
                        }
                    } else {
                        TextView textView = getInfoTextView();
                        ScoreAdapter adapter = new ScoreAdapter(ScoreActivity.this, new Score[0]);
                        listView.setAdapter(adapter);
                        listView.addHeaderView(textView);
                    }
                }

                return false;
            }


        });
    }

    @Override
    public void onBackPressed() {
        Intent predecessorIntent = this.getIntent();
        Intent nextActivity = null;

        if (predecessorIntent != null && predecessorIntent.getStringExtra("activity") != null) {
            String predecessorActivity = predecessorIntent.getStringExtra("activity");

            if (predecessorActivity.equals("main")) {
                nextActivity = new Intent(this, MainActivity.class);
            }
        }else {
            nextActivity = new Intent(this, TetrisActivity.class);
        }

        if (nextActivity != null) {
            startActivity(nextActivity);
        }
        finish();
    }

    private void cacheUserScores(Score[] scores) {
        if (scores.length > 0) {
            StringBuilder builder = new StringBuilder("");
            for (Score score : scores) {
                if (!score.getUserID().isEmpty() && !score.getUsername().isEmpty()) {
                    builder.append(score.getUserID());
                    builder.append(",");
                    builder.append(score.getUsername());
                    builder.append(",");
                    builder.append(score.getValue());
                    builder.append(";");
                }
            }
            String userScores = builder.toString();
            if (!userScores.isEmpty()) {
                preferences.edit().putString(USER_SCORES, userScores).commit();
            }
        }
    }

    private Score[] getCachedScores(String userScores) {
        ArrayList<Score> scoreList = new ArrayList<>();
        Score[] scores = null;

        String[] tripleEntries = userScores.split(";");
        String[] singleEntries;
        for (String triple : tripleEntries) {
            singleEntries = triple.split(",");
            if (singleEntries != null && singleEntries.length == 3) {
                Score score = new Score(singleEntries[0], singleEntries[1],
                        Integer.valueOf(singleEntries[2]));
                scoreList.add(score);
            }
        }

        if (!scoreList.isEmpty()) {
            scores = new Score[scoreList.size()];
            scoreList.toArray(scores);
        }

        return scores;
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
