package com.droidkit.pickers.file.items;

import android.os.Environment;

import com.droidkit.pickers.file.ExploreItemViewHolder;

/**
 * Created by kiolt_000 on 22/09/2014.
 */
public class ExternalStorageItem extends ExplorerItem {
    private final String name;

    public ExternalStorageItem(String name, int imageId) {
        super(Environment.getExternalStorageDirectory(), false, "", imageId, true);
        this.name = name;
    }

    @Override
    public String getTitle() {
        return name;
    }


    @Override
    public void bindData(ExploreItemViewHolder holder) {
        holder.setTitle(getTitle());
        holder.disableSubtitle();
        holder.disableDivider();
    }
}
