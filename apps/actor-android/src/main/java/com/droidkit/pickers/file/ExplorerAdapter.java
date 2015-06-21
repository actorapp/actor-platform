package com.droidkit.pickers.file;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.droidkit.pickers.file.items.ExplorerItem;

import java.util.ArrayList;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 14/09/2014.
 */
public class ExplorerAdapter extends BaseAdapter {

    private final ArrayList<? extends ExplorerItem> items;
    protected final Context context;

    public ExplorerAdapter(Context context, ArrayList<? extends ExplorerItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public ExplorerItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Timer.start();
        View itemView;
        ExploreItemViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.picker_item_file, null);
            holder = new ExploreItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ExploreItemViewHolder) convertView.getTag();
        }
        itemView = convertView;

        ExplorerItem item = getItem(position);


        item.bindImage(holder);
        item.bindData(holder);
        if (getCount() == 1) {
            holder.disableDivider();
        }
        //Timer.stop("Item created");
        return itemView;
    }
}