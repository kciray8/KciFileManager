package com.kciray.android.commons.gui;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;

import java.lang.reflect.Field;

public class Hacks {
    public static void setMarginForDrawerLayoutSubclass(DrawerLayout mDrawerLayout, int margin) {
        try {
            Field mDragger = mDrawerLayout.getClass().getSuperclass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(mDrawerLayout);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);

            mEdgeSize.setInt(draggerObj, edge * margin);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
