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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileScanner {
    private static Map<String, DirElement> dirToView = new HashMap<>();

    static boolean cachedView(String absPath) {
        return dirToView.containsKey(absPath);
    }

    static DirElement getCachedView(String absPath) {
        return dirToView.get(absPath);
    }

    static void addToCache(String absPath, DirElement view) {
        if (!view.isBackButton()) {
            dirToView.put(absPath, view);
        }
    }

    static void deleteFromCache(String absPath){
        dirToView.remove(absPath);
    }

    static void clear(){
        dirToView.clear();
    }
}
