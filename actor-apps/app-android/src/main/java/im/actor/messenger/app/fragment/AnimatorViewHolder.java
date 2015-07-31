/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ex3ndr on 16.05.15.
 */
public abstract class AnimatorViewHolder extends RecyclerView.ViewHolder {
    public AnimatorViewHolder(View itemView) {
        super(itemView);
    }

    public abstract boolean performAnimation();
}
