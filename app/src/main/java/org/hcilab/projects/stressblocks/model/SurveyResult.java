package org.hcilab.projects.stressblocks.model;

import android.provider.BaseColumns;

import java.util.List;

/**
 * Created by Sanjeev on 09.01.2017.
 */

public class SurveyResult {

    private int id;
    private String entireAnswer;
    private List<String> answers;
    private boolean sent;

    public SurveyResult(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getEntireAnswer() {
        return entireAnswer;
    }

    public void setEntireAnswer(String entireAnswer) {
        this.entireAnswer = entireAnswer;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public static class SurveyResultEntry implements BaseColumns {
        public static final String TABLE_NAME = "survey_result";
        public static final String ANSWERS = "answers";
        public static final String SENT = "sent";
    }
}
