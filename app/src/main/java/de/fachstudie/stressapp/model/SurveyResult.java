package de.fachstudie.stressapp.model;

import android.provider.BaseColumns;

import java.util.List;

/**
 * Created by Sanjeev on 09.01.2017.
 */

public class SurveyResult {

    private int id;
    private List<String> answers;

    public SurveyResult(int id, List<String> answers) {
        this.id = id;
        this.answers = answers;
    }

    public int getId() {
        return id;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public static class SurveyResultEntry implements BaseColumns {
        public static final String TABLE_NAME = "survey_result";
        public static final String ANSWERS = "answers";

    }
}
