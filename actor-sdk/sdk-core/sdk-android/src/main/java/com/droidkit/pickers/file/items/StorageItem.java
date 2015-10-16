package com.droidkit.pickers.file.items;

import android.view.View;

import com.droidkit.pickers.file.ExploreItemViewHolder;

import java.io.File;

import im.actor.messenger.R;

public class StorageItem extends ExplorerItem {
    private final String name;

    public StorageItem(String name) {
        super(new File("/"), false, "", R.drawable.picker_memory, true);
        this.name = name;
    }

    @Override
    public String getTitle() {
        return name;
    }


    @Override
    public void bindData(View itemView) {
    }

    @Override
    public void bindData(ExploreItemViewHolder holder) {
        holder.setTitle(getTitle());
        holder.disableSubtitle();
        holder.disableDivider();
    }
}
