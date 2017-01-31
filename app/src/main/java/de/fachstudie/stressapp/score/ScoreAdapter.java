package de.fachstudie.stressapp.score;

import android.content.Context;
import android.content.SharedPreferences;
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
    private SharedPreferences preferences;

    public ScoreAdapter(Context context, Score[] scores) {
        super(context, -1, scores);
        this.scores = scores;
        this.context = context;
        this.preferences = this.context.getSharedPreferences("de.fachstudie.stressapp" +
                ".preferences", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.score_item, parent, false);
        TextView usernameView = (TextView) rowView.findViewById(R.id.username);
        TextView scoreView = (TextView) rowView.findViewById(R.id.score);
        TextView rankView = (TextView) rowView.findViewById(R.id.rank);

        usernameView.setText(scores[position].getUsername());
        scoreView.setText(String.valueOf(scores[position].getValue()));
        rankView.setText(String.valueOf(position + 1));

        highlightUser(usernameView, scoreView, rankView, scores[position].getUsername());
        return rowView;
    }

    private void highlightUser(TextView usernameView, TextView scoreView, TextView rankView,
                               String username) {
        // TODO check deviceid
        if (username.equals(preferences.getString("username", ""))) {
            usernameView.setTextColor(context.getResources().getColor(R.color.lightblue));
            scoreView.setTextColor(context.getResources().getColor(R.color.lightblue));
            rankView.setTextColor(context.getResources().getColor(R.color.lightblue));
        }
    }
}
