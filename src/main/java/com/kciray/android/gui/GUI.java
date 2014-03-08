/****************************************************************************
 **
 ** KciFileManager is open file manager for Android, quick and convenient
 ** Copyright (C) 2014 Yaroslav (aka KciRay).
 ** Contact: Yaroslav (kciray8@gmail.com)
 **
 ** This program is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU General Public License
 ** along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **
 *****************************************************************************/

package com.kciray.android.gui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.kciray.android.Common;
import com.kciray.android.L;
import com.kciray.android.OnInputListener;
import com.kciray.android.filemanager.R;

public class GUI {
    public static void inputString(String title, final OnInputListener okListener) {
        inputString(title,null,okListener);
    }

    public static void inputString(String title,String defaultStr, final OnInputListener okListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Common.getContext());

        alert.setTitle(title);

        final EditText input = new EditText(Common.getContext());
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
        Toast toast = Toast.makeText(Common.getContext(), str, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static ProgressDialog showProgressDialog(String message) {
        ProgressDialog dialog = new ProgressDialog(Common.getContext());
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void showMessage(String title, String message) {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(Common.getContext()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    public static void askQuestion(String title, String message, final Runnable onYes){
        askQuestion(title, message, onYes,null);
    }

    public static void askQuestion(String title, String message, final Runnable onYes, final Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Common.getContext());

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(L.tr(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (onYes != null) {
                    onYes.run();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(L.tr(R.string.no), new DialogInterface.OnClickListener() {
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

