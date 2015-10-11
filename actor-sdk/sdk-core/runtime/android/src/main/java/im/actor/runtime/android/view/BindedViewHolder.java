/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public abstract class BindedViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {

    protected final View contentView;
    protected final FrameLayout contentFrame;

    public BindedViewHolder(Context context, RootViewType rootViewType) {
        this(rootViewType == RootViewType.FRAME_LAYOUT
                ? new FrameLayout(context)
                : new LinearLayout(context));
    }

    public BindedViewHolder(View itemView) {
        super(itemView);

        this.contentView = itemView;

        if (itemView instanceof FrameLayout) {
            this.contentFrame = (FrameLayout) itemView;
        } else {
            this.contentFrame = null;
        }
    }
}
