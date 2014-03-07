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

package com.kciray.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.kciray.android.filemanager.MainActivity;
import com.kciray.android.filemanager.R;

public class Common {
    private static Context context;

    public static void toast(String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context defaultContext) {
        Common.context = defaultContext;
    }

    public static ProgressDialog showProgressDialog(String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void showMessage(String title, String message) {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    /**
     * Adding shortcut for MainActivity
     * on Home screen
     * <p/>
     * need <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
     *
     * @param title
     */
    private void addShortcut(String title, Context context) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, R.drawable.icon));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }

    public static void askQuestion(String title, String message, final Runnable onYes){
        askQuestion(title, message, onYes,null);
    }

    public static void askQuestion(String title, String message, final Runnable onYes, final Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
