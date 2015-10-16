package com.droidkit.pickers.file;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 09/10/2014.
 */
public class ExploreItemViewHolder {

    private final TextView titleView;
    private final TextView subTitleView;
    private final ImageView imageView;
    private final View selectedView;
    private final View divider;
    private final TextView typeView;
    private Context context;

    public ExploreItemViewHolder(View itemView) {
        context = itemView.getContext();
        titleView = (TextView) itemView.findViewById(R.id.title);
        subTitleView = (TextView) itemView.findViewById(R.id.subtitle);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        selectedView = itemView.findViewById(R.id.selected);
        divider = itemView.findViewById(R.id.divider);
        typeView = (TextView) itemView.findViewById(R.id.type);
    }

    public void setTitle(String title) {
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subTitleView.setVisibility(View.VISIBLE);
        subTitleView.setText(subtitle);
    }


    public Context getContext() {
        return context;
    }

    public void setSelected(boolean selected) {
        // selectedView.setSelected(selected);
    }

    public void disableSubtitle() {
        subTitleView.setVisibility(View.GONE);
    }

    public void disableDivider() {
        divider.setVisibility(View.GONE);
    }

    public void enableDivider() {
        divider.setVisibility(View.VISIBLE);
    }

    public void setIcon(int imageId) {
        imageView.setImageResource(imageId);
    }

    public void setType(String fileType) {
        typeView.setText(fileType);
    }
}
