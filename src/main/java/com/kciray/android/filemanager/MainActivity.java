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

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.kciray.Q;
import com.kciray.android.Common;

public class MainActivity extends Activity {
    DirView activeDirView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.setContext(this);

        activeDirView = new DirView(this);
        setContentView(activeDirView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            activeDirView.goUp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem showFolderPropItem = menu.findItem(R.id.show_root_actions);
        showFolderPropItem.setOnMenuItemClickListener(new MenuItem.
                OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return false;
            }
        });

        MenuItem addNewFileItem = menu.findItem(R.id.add_new_file);
        addNewFileItem.setOnMenuItemClickListener(new MenuItem.
                OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activeDirView.addNewFile();
                return false;
            }
        });

        MenuItem addNewFolderItem = menu.findItem(R.id.add_new_folder);
        addNewFolderItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activeDirView.addNewFolder();
                return false;
            }
        });


       //MenuItem utilsItem = menu.findItem(R.id.utils);

        return super.onCreateOptionsMenu(menu);
    }

    public void addNewFolder(MenuItem item) {
        activeDirView.addNewFolder();
    }

    public void addNewFile(MenuItem item) {
        activeDirView.addNewFile();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Q.out(menu);
        super.onCreateContextMenu(menu, v, menuInfo);
        activeDirView.handleContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        FileMenu menuEnum = FileMenu.values()[menuItemIndex];
        int itemIndex = info.position;

        switch (menuEnum) {
            case DELETE:
                activeDirView.deleteItem(itemIndex);
                break;
            case PROPERTIES:
                activeDirView.showProp(itemIndex);
                break;
            case RENAME:
                activeDirView.renameItem(itemIndex);
                break;
            case CALC_SIZE:
                activeDirView.calcFolderSize(itemIndex);
                break;
        }

        return true;
    }
}
