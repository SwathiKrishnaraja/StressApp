package de.fachstudie.stressapp.score;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.fachstudie.stressapp.R;
import de.fachstudie.stressapp.model.Score;

/**
 * Created by Paul Kuznecov on 24.01.2017.
 */

public class ScoreAdapter extends ArrayAdapter<Score> {
    private final Score[] scores;
    private final Context context;

    public ScoreAdapter(Context context, Score[] scores) {
        super(context, -1, scores);
        this.scores = scores;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.score_item, parent, false);
        TextView username = (TextView) rowView.findViewById(R.id.username);
        TextView score = (TextView) rowView.findViewById(R.id.score);
        TextView rank = (TextView) rowView.findViewById(R.id.rank);
        rank.setText(String.valueOf(position + 1));
        username.setText(scores[position].getUsername());
        score.setText(String.valueOf(scores[position].getValue()));

        return rowView;
    }
}
