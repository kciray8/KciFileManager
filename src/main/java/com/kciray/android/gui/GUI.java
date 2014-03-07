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
import android.content.DialogInterface;
import android.widget.EditText;

import com.kciray.android.Common;
import com.kciray.android.L;
import com.kciray.android.OnInputListener;
import com.kciray.android.filemanager.R;

public class GUI {
    public static void inputString(String title, final OnInputListener okListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Common.getContext());

        alert.setTitle(title);

        final EditText input = new EditText(Common.getContext());
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
}

