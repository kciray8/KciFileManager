package com.kciray.android.commons.gui;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;

import java.lang.reflect.Constructor;

public class MenuUtils {
    public static Menu getDefaultMenuInstance(Context context) {
        try {
            Class<?> menuBuilderClass = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Constructor<?> constructor = menuBuilderClass.getDeclaredConstructor(Context.class);

            return (Menu) constructor.newInstance(context);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ContextMenu getDefaultContextMenuInstance(Context context) {
        try {
            Class<?> menuBuilderClass = Class.forName("com.android.internal.view.menu.ContextMenuBuilder");
            Constructor<?> constructor = menuBuilderClass.getDeclaredConstructor(Context.class);

            return (ContextMenu) constructor.newInstance(context);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
