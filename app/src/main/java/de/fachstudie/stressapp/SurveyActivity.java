package de.fachstudie.stressapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurveyActivity extends AppCompatActivity {

    private static final int SURVEY_REQUEST = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Intent i_survey = new Intent(SurveyActivity.this, com.androidadvance.androidsurvey.SurveyActivity.class);
        //you have to pass as an extra the json string.
        i_survey.putExtra("json_survey", loadSurveyJson("example_survey"));
        startActivityForResult(i_survey, SURVEY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SURVEY_REQUEST) {
            if (resultCode == RESULT_OK) {

                String json_result = data.getExtras().getString("answers");
                Log.v("JSON RESULT", json_result);

                List<String> answers = getSurveyAnswers(json_result);

                // TODO save answers in DB
            }
        }
    }

    private List<String> getSurveyAnswers(String answers_json) {
        List<String> answers = new ArrayList<>();
        Pattern pattern = Pattern.compile(":(.*?)(,|\\})");
        Matcher matcher = pattern.matcher(answers_json);
        while(matcher.find()){
            answers.add(matcher.group(1));
        }
        return answers;
    }

    //json stored in the assets folder. but you can get it from wherever you like.
    private String loadSurveyJson(String filename) {
        try {
            InputStream is =  getAssets().open(filename);
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
