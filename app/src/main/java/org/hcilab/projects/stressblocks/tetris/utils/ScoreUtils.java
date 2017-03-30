package org.hcilab.projects.stressblocks.tetris.utils;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.hcilab.projects.stressblocks.networking.StressAppClient;
import org.json.JSONException;
import org.json.JSONObject;

import static org.hcilab.projects.stressblocks.tetris.constants.StringConstants.HIGHSCORE;

/**
 * Created by Sanjeev on 30.03.2017.
 */

public class ScoreUtils {

    public static void addHighscoreToPreferences(StressAppClient client, final SharedPreferences preferences) {
        client.getScores(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                String response = message.getData().getString("response");
                try {
                    JSONObject result = new JSONObject(response);
                    int highscore = result.getInt("userscore");
                    preferences.edit().putInt(HIGHSCORE, highscore).commit();
                    return true;

                } catch (JSONException e) {
                    Log.e("JSON exception", e.getClass().toString() + " " + e.getMessage());
                }
                return false;
            }
        }, preferences.getString("username", ""));
    }
}
