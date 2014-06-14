package com.kciray.android.commons.gui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.kciray.android.commons.sys.Global;
import com.kciray.android.commons.sys.L;
import com.kciray.android.filemanager.R;

public class DialogUtils {
    public static void inputString(String title, final OnInputListener okListener) {
        inputString(title,null,okListener);
    }

    public static void inputString(String title,String defaultStr, final OnInputListener okListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Global.context);

        alert.setTitle(title);

        final EditText input = new EditText(Global.context);
        if(defaultStr != null){
            input.setText(defaultStr);
        }
        alert.setView(input);
        alert.setPositiveButton(L.tr(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                okListener.onInput(input.getText().toString());
            }
        });

        alert.setNegativeButton(L.tr(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Canceled
            }
        });

        alert.show();
    }

    public static void toast(String str) {
        Toast toast = Toast.makeText(Global.context, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static ProgressDialog showProgressDialog(String message) {
        ProgressDialog dialog = new ProgressDialog(Global.context);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void showMessage(String title, String message) {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(Global.context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    public static void askQuestion(String title, String message, final Runnable onYes){
        askQuestion(title, message, onYes,null);
    }

    public static void askQuestion(String title, String message,
                                   final Runnable onYes, final Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Global.context);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(L.tr(R.string.yes),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (onYes != null) {
                    onYes.run();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(L.tr(R.string.no),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onNo != null) {
                    onNo.run();
                }
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
