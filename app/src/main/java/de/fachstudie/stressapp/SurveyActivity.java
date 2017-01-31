package de.fachstudie.stressapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.networking.StressAppClient;

public class SurveyActivity extends AppCompatActivity {

    private static final int SURVEY_REQUEST = 1337;
    private DatabaseService dbService;
    private StressAppClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        client = new StressAppClient(this);
        dbService = DatabaseService.getInstance(SurveyActivity.this);

        Intent i_survey = new Intent(SurveyActivity.this, com.androidadvance.androidsurvey.SurveyActivity.class);
        //you have to pass as an extra the json string.
        i_survey.putExtra("json_survey", loadSurveyJson("survey.json"));
        startActivityForResult(i_survey, SURVEY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SURVEY_REQUEST) {
            if (resultCode == RESULT_OK) {

                String json_result = data.getExtras().getString("answers");
                final String answers = TextUtils.join(",", getSurveyAnswers(json_result));

                client.sendSurveyAnswers(answers, new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        boolean successful = message.getData().getBoolean("successful");
                        dbService.saveSurveyAnswers(answers, successful);

                        Intent intent = new Intent(SurveyActivity.this, MainActivity.class);
                        intent.putExtra("message", "survey answered");
                        startActivity(intent);
                        return false;
                    }
                });
            }
        }
        finish();
    }

    private List<String> getSurveyAnswers(String answers_json) {
        List<String> answers = new ArrayList<>();
        Pattern pattern = Pattern.compile(":([\"\\p{Alpha}\\s]+)(,|\\})");
        Matcher matcher = pattern.matcher(answers_json);
        while (matcher.find()) {
            answers.add(matcher.group(1).replaceAll("\"", ""));
        }
        return answers;
    }

    //json stored in the assets folder. but you can get it from wherever you like.

    private String loadSurveyJson(String filename) {
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
