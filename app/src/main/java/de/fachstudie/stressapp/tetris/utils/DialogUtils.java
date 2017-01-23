package de.fachstudie.stressapp.tetris.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

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
}
