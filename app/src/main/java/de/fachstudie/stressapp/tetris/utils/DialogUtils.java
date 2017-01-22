package de.fachstudie.stressapp.tetris.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.fachstudie.stressapp.R;
import de.fachstudie.stressapp.tetris.constants.StringConstants;

/**
 * Created by Sanjeev on 22.01.2017.
 */

public class DialogUtils {


    public static AlertDialog getUserInfoDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(StringConstants.USER_INFORMATION);
        builder.setMessage(StringConstants.USER_INFORMATION_INFO);

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent settingsIntent = new Intent(StringConstants.ANDROID_SETTINGS_NOTIFICATION_LISTENER);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                activity.startActivityForResult(settingsIntent, 0);
                dialog.dismiss();
            }
        }).setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView view = (TextView) dialog.findViewById(android.R.id.message);
        view.setTextSize(15);

        return dialog;
    }

    public static AlertDialog getGameOverDialog(final Activity activity, LayoutInflater inflater) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = inflater.inflate(R.layout.dialog_gameover, null);
        builder.setView(view);
        builder.setTitle("GAME OVER");

        Button resumeGameBtn = (Button) view.findViewById(R.id.resume_game_btn);
        resumeGameBtn.setVisibility(View.GONE);

        Button finishGameBtn = (Button) view.findViewById(R.id.finish_game_btn);
        finishGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        AlertDialog gameOverDialog = builder.create();
        return gameOverDialog;
    }
}
