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

package com.kciray.android.filemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kciray.Q;
import com.kciray.android.L;
import com.kciray.android.OnInputListener;
import com.kciray.android.gui.GUI;

import java.io.File;

public class DirElement{
    private File file;
    private View view;
    private boolean backButton;
    private TextView pathText;

    public DirElement(Context context) {
    }

    public DirElement(Context context, File file) {
        this.file = file;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String text = "";
        text+=file.getName();

        view = inflater.inflate(R.layout.direlement,null);

        pathText = (TextView)view.findViewById(R.id.path);
        pathText.setText(text);

        if (file.isDirectory()) {
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            imageView.setImageResource(R.drawable.folder);
        }
    }

    public void rename(){
        GUI.inputString("Введите новое имя",file.getName(),new OnInputListener() {
            @Override
            public void onInput(String str) {
                Q.out(file.getParent());
                Q.out(str);

                boolean success = file.renameTo(new File(file.getParent(),str));
                if(success){
                    pathText.setText(str);
                }else{
                    GUI.toast("Ошибка при переименовании файла");
                }
            }
        });
    }

    public boolean isDir(){
        return file.isDirectory();
    }

    /**
     * Create element for navigation to parent directory
     * @return
     */
    public static DirElement getBackNavElement(Context context){
        DirElement dirElement = new DirElement(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dirElement.view = inflater.inflate(R.layout.direlement, null);

        TextView pathText = (TextView) dirElement.view.findViewById(R.id.path);
        pathText.setText("(...) "+ L.tr(R.string.up));

        dirElement.backButton = true;

        return dirElement;
    }

    public View getView() {
        return view;
    }

    public File getFile() {
        return file;
    }

    public boolean isBackButton() {
        return backButton;
    }
}
