package org.hcilab.projects.stressblocks.score;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.hcilab.projects.stressblocks.R;
import org.hcilab.projects.stressblocks.model.Score;

import static org.hcilab.projects.stressblocks.R.id.username;

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

        if (scores.length > 0) {
            View rowView = inflater.inflate(R.layout.score_item, parent, false);
            TextView usernameView = (TextView) rowView.findViewById(username);
            TextView scoreView = (TextView) rowView.findViewById(R.id.score);
            TextView rankView = (TextView) rowView.findViewById(R.id.rank);

            usernameView.setText(scores[position].getUsername());
            scoreView.setText(String.valueOf(scores[position].getValue()));
            rankView.setText(String.valueOf(position + 1));

            highlightUser(usernameView, scoreView, rankView, getContext(), scores[position]);
            return rowView;
        }

        return null;
    }

    private void highlightUser(TextView usernameView, TextView scoreView, TextView rankView,
                               Context context, Score score) {
        String localUserID = Settings.Secure.getString(context
                        .getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String localUsername = preferences.getString("username", "");

        if (localUserID.equals(score.getUserID()) && localUsername.equals(score.getUsername())) {
            usernameView.setTextColor(context.getResources().getColor(R.color.lightblue));
            scoreView.setTextColor(context.getResources().getColor(R.color.lightblue));
            rankView.setTextColor(context.getResources().getColor(R.color.lightblue));
        }
    }
}
