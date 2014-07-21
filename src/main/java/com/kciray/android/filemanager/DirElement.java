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

import com.kciray.android.commons.gui.DialogUtils;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.gui.OnInputListener;

import java.io.File;
import java.util.Comparator;

public class DirElement {
    public void setFile(File file) {
        this.file = file;
    }

    private File file;
    private View view;
    private boolean backButton;
    private TextView pathText;
    private TextView sizeTextView;
    private DirView dirView;

    public DirElement(Context context) {
    }

    public DirElement(Context context, File file, DirView dirView) {
        this.file = file;
        this.dirView = dirView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String text = "";
        text += file.getName();

        view = inflater.inflate(R.layout.direlement, null);
        sizeTextView = (TextView) view.findViewById(R.id.size);
        sizeTextView.setText("");

        pathText = (TextView) view.findViewById(R.id.path);
        pathText.setText(text);

        if (file.isDirectory()) {
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            imageView.setImageResource(R.drawable.folder);
        } else {
            long fileSize = file.length();
            setFileSize(fileSize);
        }
    }

    public void setFileSize(long fileSize) {
        String formFileSize = String.format("%,d %s", fileSize, "[Byte]");
        sizeTextView.setText(formFileSize);
    }

    public void rename() {
        DialogUtils.inputString("Введите новое имя", file.getName(), str -> {
            File oldFile = file;
            File newFile = new File(file.getParent(), str);
            boolean success = file.renameTo(newFile);
            if (success) {
                file = newFile;
                if (!isBackButton()) {
                    pathText.setText(str);
                } else {
                    FileScanner.deleteFromCache(oldFile);
                    dirView.updateRootDirectory(file);
                }
            } else {
                DialogUtils.toast("Ошибка при переименовании файла");
            }
        });
    }

    public boolean isDir() {
        return file.isDirectory();
    }

    /**
     * Create element for navigation to parent directory
     *
     * @return
     */
    public static DirElement getBackNavElement(Context context, File file, DirView dirView) {
        DirElement dirElement = new DirElement(context, file, dirView);

        dirElement.pathText.setText("(...) " + L.tr(R.string.up));
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

    static Comparator<DirElement> comparator = new DirElementComparator();

    public static Comparator<DirElement> getComparator() {
        return comparator;
    }

    private static class DirElementComparator implements Comparator<DirElement> {
        @Override
        public int compare(DirElement lhs, DirElement rhs) {
            //Back button always in top
            if (lhs.isBackButton()) {
                return -1;
            }
            if (rhs.isBackButton()) {
                return 1;
            }

            //One of element - is directory
            if ((lhs.getFile().isDirectory()) && (!rhs.getFile().isDirectory())) {
                return -1;
            }
            if ((!lhs.getFile().isDirectory()) && (rhs.getFile().isDirectory())) {
                return 1;
            }

            return lhs.getFile().getName().compareTo(rhs.getFile().getName());
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }
}
