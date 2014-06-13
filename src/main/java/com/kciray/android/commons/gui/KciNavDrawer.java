package com.kciray.android.commons.gui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kciray.android.filemanager.MainActivity;
import com.kciray.android.filemanager.R;

import java.util.LinkedList;
import java.util.List;

public class KciNavDrawer<Category extends Enum> extends DrawerLayout {
    public OnItemClick onItemClickListener;

    public interface OnItemClick<Category extends Enum> {
        void onClickItem(int categoryId, Object data);
    }

    private FrameLayout contentLayout;
    private ListView listView;
    private Context context;
    DrawerMainAdapter adapter = new DrawerMainAdapter();

    public KciNavDrawer(Context context) {
        super(context);
        this.context = context;
        contentLayout = new FrameLayout(context);
        addView(contentLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        listView = new ListView(context);
        DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(
                Metric.dpToPx(240), LinearLayout.LayoutParams.MATCH_PARENT);

        lp.gravity = GravityCompat.START;

        listView.setLayoutParams(lp);
        listView.setBackgroundColor(0xffe0e0e0);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        addView(listView);

        Hacks.setMarginForDrawerLayoutSubclass(this, 5);
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

        DrawerMainElement categoryElement = DrawerMainElement.getCategory(context,
                nameCategory, category.ordinal());
        categoryElement.onItemClickListener = onItemClickListener;

        adapter.addElement(categoryElement);
    }

    public void addInfoViewToCategory(Category category, View infoView, Object data) throws IllegalArgumentException {
        DrawerSecondaryElement secondaryElement = DrawerSecondaryElement.getInfoItem(context, infoView, data, category);

        DrawerMainElement containerElement = null;
        for (DrawerMainElement mainElement : adapter.mainElements) {
            if (mainElement.categoryCode == category.ordinal()) {
                containerElement = mainElement;
                break;
            }
        }
        if (containerElement != null) {
            containerElement.secondaryAdapter.addElement(secondaryElement);
            containerElement.updateSize();

            adapter.notifyDataSetChanged();
            containerElement.secondaryAdapter.notifyDataSetChanged();
        } else {
            throw new IllegalArgumentException("Element for this category not exist!");
        }
    }

    public void addRawView(View rawView) {
        DrawerMainElement rawViewElement = DrawerMainElement.getRawView(context, rawView);
        adapter.addElement(rawViewElement);
    }
}

class DrawerMainAdapter extends BaseAdapter {
    List<DrawerMainElement> mainElements = new LinkedList<>();

    @Override
    public int getCount() {
        return mainElements.size();
    }

    @Override
    public Object getItem(int position) {
        return mainElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mainElements.get(position).view;
    }

    public void addElement(DrawerMainElement element) {
        mainElements.add(element);
    }

    @Override
    public boolean isEnabled(int position) {
        DrawerMainElement element = mainElements.get(position);
        if (element.isCategory()) {
            return false;
        } else {
            return true;
        }
    }
}

class DrawerMainElement {
    private boolean mCategory;
    ListView secondaryElementsList;
    DrawerSecondaryAdapter secondaryAdapter;
    int categoryCode = -1;
    View view;
    public KciNavDrawer.OnItemClick onItemClickListener;

    private DrawerMainElement() {

    }

    private DrawerMainElement(Context context, int categoryCode) {
        this.categoryCode = categoryCode;
        mCategory = true;
        secondaryElementsList = new ListView(context);
        secondaryElementsList.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        secondaryElementsList.setDivider(context.getResources().getDrawable(R.layout.list_divider));
        secondaryAdapter = new DrawerSecondaryAdapter();
        secondaryElementsList.setAdapter(secondaryAdapter);
        secondaryElementsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerSecondaryElement element = (DrawerSecondaryElement) secondaryAdapter.getItem(position);
                onItemClickListener.onClickItem(element.categoryId, element.data);
            }
        });
        updateSize();
    }

    public static DrawerMainElement getCategory(Context context, String title, int categoryCode) {
        LayoutInflater inflater = (LayoutInflater) MainActivity.getInstance().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setPadding(Metric.dpToPx(15), 0, 0, 0);
        mainLayout.addView(textView);

        View separatorView = new View(context);
        separatorView.setBackgroundColor(Color.LTGRAY);
        int someDP = Metric.dpToPx(2);
        separatorView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, someDP));
        mainLayout.addView(separatorView);

        DrawerMainElement element = new DrawerMainElement(context, categoryCode);
        element.updateSize();
        mainLayout.addView(element.secondaryElementsList);
        mainLayout.setPadding(0, 0, 0, Metric.dpToPx(25));
        element.view = mainLayout;

        return element;
    }

    public void updateSize() {
        ListViewUtils.fitHeight(secondaryElementsList);
    }

    public View getView() {
        return view;
    }

    boolean isCategory() {
        return mCategory;
    }

    public static DrawerMainElement getRawView(Context context, View rawView) {
        DrawerMainElement element = new DrawerMainElement();
        element.view = rawView;

        return element;
    }
}

class DrawerSecondaryAdapter extends BaseAdapter {
    List<DrawerSecondaryElement> secondaryElements = new LinkedList<>();

    @Override
    public int getCount() {
        return secondaryElements.size();
    }

    @Override
    public Object getItem(int position) {
        return secondaryElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return secondaryElements.get(position).view;
    }

    public void addElement(DrawerSecondaryElement element) {
        secondaryElements.add(element);

    }
}

class DrawerSecondaryElement {
    View view;
    Object data;
    int categoryId;

    private DrawerSecondaryElement() {
    }

    public static DrawerSecondaryElement getInfoItem(Context context, View infoView, Object data, Enum category) {
        DrawerSecondaryElement element = new DrawerSecondaryElement();
        element.view = infoView;
        element.data = data;
        element.categoryId = category.ordinal();

        return element;
    }

    public View getView() {
        return view;
    }
}