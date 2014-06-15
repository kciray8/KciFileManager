package com.kciray.android.commons.gui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kciray.android.filemanager.R;

import java.util.LinkedList;
import java.util.List;

public class KciNavDrawer<Category extends Enum> extends DrawerLayout {
    private static int fullWidth = 240;//dp

    private OnItemClick onItemClickListener;
    private FrameLayout contentLayout;
    private ListView listView;
    private Context context;
    private DrawerMainAdapter adapter = new DrawerMainAdapter();

    public interface OnItemClick<Category extends Enum> {
        void onClickItem(int categoryId, Object data);
    }

    public KciNavDrawer(Context context) {
        super(context);
        this.context = context;
        contentLayout = new FrameLayout(context);
        addView(contentLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        listView = new ListView(context);
        DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(
                Metric.dpToPx(fullWidth), LinearLayout.LayoutParams.MATCH_PARENT);

        lp.gravity = GravityCompat.START;

        listView.setLayoutParams(lp);
        listView.setBackgroundColor(0xffe0e0e0);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerElement element = (DrawerElement) adapter.getItem(position);
                if (element.type == DrawerElement.Type.ELEMENT) {
                    onItemClickListener.onClickItem(element.categoryCode, element.data);
                }
            }
        });
        addView(listView);

        Hacks.setMarginForDrawerLayoutSubclass(this, 3);
    }

    public void registerOnClickItemListener(OnItemClick onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ActionBarDrawerToggle addButtonToActivity(ActionBarActivity activity) {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, this, R.drawable.ic_drawer, -1, -1);
        this.setDrawerListener(drawerToggle);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);

        return drawerToggle;
    }
    /* Not forget add:
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
     */

    public void setMainContent(View content) {
        contentLayout.addView(content);
    }

    public void addCategory(Category category, int stringNameId) {
        String nameCategory = context.getString(stringNameId);

        DrawerElement categoryElement = DrawerElement.getCategory(context,
                nameCategory, category.ordinal());
        categoryElement.onItemClickListener = onItemClickListener;

        adapter.addElement(categoryElement, context);
    }

    public void addInfoViewToCategory(Category category, View infoView, Object data) throws IllegalArgumentException {
        DrawerElement drawerElement = DrawerElement.getElement(category.ordinal(), infoView, data);

        adapter.addElement(drawerElement, context);
    }

    public void deleteViewFromCategory(Category category, Object data) {
        adapter.deleteElementEquData(data, category.ordinal());
    }

    public void addRawView(View rawView) {
        DrawerElement rawViewElement = DrawerElement.getRawView(context, rawView);
        adapter.addElement(rawViewElement, context);
    }
}

class DrawerMainAdapter extends BaseAdapter {
    List<DrawerElement> elements = new LinkedList<>();

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Object getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return elements.get(position).view;
    }

    public void addElement(DrawerElement element, Context context) {
        if (element.isCategory()) {
            if (elements.size() != 0) {
                DrawerElement empty = DrawerElement.getEmptyElement(25, context);
                elements.add(empty);
            }
            elements.add(element);
        } else {
            int categoryCode = element.categoryCode;
            int lastCategoryPos = -1;
            DrawerElement lastElement = null;
            for (DrawerElement drawerElement : elements) {
                if (drawerElement.categoryCode == categoryCode) {
                    lastCategoryPos = elements.indexOf(drawerElement);
                    lastElement = drawerElement;
                }
            }
            if (lastElement != null) {
                //Add after element - we need insert separator
                if (!lastElement.isCategory()) {
                    DrawerElement separator = DrawerElement.getSeparator(context, 0xFFCFCFCF, 0.5F, Metric.dpToPx(15));
                    element.topSeparator = separator;
                    separator.linkedElement = element;
                    elements.add(lastCategoryPos + 1, separator);
                    lastCategoryPos++;
                }

                elements.add(lastCategoryPos + 1, element);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        DrawerElement element = elements.get(position);
        if (element.isCategory()) {
            return false;
        } else {
            return true;
        }
    }

    public void deleteElementEquData(Object data, int categoryId) {
        for (DrawerElement element : elements) {
            if ((element.categoryCode == categoryId) && (element.data != null)) {
                if (element.data.equals(data)) {
                    elements.remove(element);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }
}

class DrawerElement {
    enum Type {CATEGORY, ELEMENT, EMPTY, SEPARATOR}

    Type type = Type.ELEMENT;

    int categoryCode = -1;
    View view;
    Object data;
    DrawerElement topSeparator;
    DrawerElement linkedElement;
    KciNavDrawer.OnItemClick onItemClickListener;

    private DrawerElement() {

    }

    private DrawerElement(int categoryCode) {
        this.categoryCode = categoryCode;
        type = Type.CATEGORY;
    }

    static DrawerElement getCategory(Context context, String title, int categoryCode) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setPadding(Metric.dpToPx(15), 0, 0, 0);
        mainLayout.addView(textView);
        mainLayout.addView(getSeparatorView(context, Color.LTGRAY, 2, 0));

        DrawerElement element = new DrawerElement(categoryCode);
        element.view = mainLayout;

        return element;
    }

    private static View getSeparatorView(Context context, int color, float height, int padding) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setBackgroundColor(color);
        int someDP = Metric.dpToPx(height);
        mainLayout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, someDP));

        return mainLayout;
    }

    static DrawerElement getSeparator(Context context, int color, float height, int padding) {
        DrawerElement element = new DrawerElement();
        element.type = Type.SEPARATOR;
        View sepView = getSeparatorView(context, color, height, padding);
        element.view = sepView;
        return element;
    }


    static DrawerElement getElement(int categoryCode, View view, Object data) {
        DrawerElement element = new DrawerElement();
        element.view = view;
        element.data = data;
        element.categoryCode = categoryCode;

        return element;
    }

    static DrawerElement getEmptyElement(int height, Context context) {
        DrawerElement element = new DrawerElement();
        element.type = Type.EMPTY;

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setPadding(0, 0, 0, Metric.dpToPx(height));
        element.view = mainLayout;

        return element;
    }

    View getView() {
        return view;
    }

    boolean isCategory() {
        return type == Type.CATEGORY;
    }

    static DrawerElement getRawView(Context context, View rawView) {
        DrawerElement element = new DrawerElement();
        element.view = rawView;

        return element;
    }
}