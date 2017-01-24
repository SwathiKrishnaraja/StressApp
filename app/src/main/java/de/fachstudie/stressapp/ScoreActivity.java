package de.fachstudie.stressapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        ListView listView = (ListView)findViewById(android.R.id.list);

        TextView textView = getTextView();
        listView.addHeaderView(textView);

        String[] scores = {"Score1", "Score2", "Score3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(),
                android.R.layout.simple_list_item_1, scores);
        listView.setAdapter(adapter);
    }

    @NonNull
    private TextView getTextView() {
        TextView textView = new TextView(this);
        textView.setText("Scoreboard");
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(20);
        return textView;
    }
}
