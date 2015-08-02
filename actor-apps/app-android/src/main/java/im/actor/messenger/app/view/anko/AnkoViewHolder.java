package im.actor.messenger.app.view.anko;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ex3ndr on 31.07.15.
 */
public class AnkoViewHolder extends RecyclerView.ViewHolder {

    final protected Context context;

    public AnkoViewHolder(Context context) {
        super(new FrameLayout(context));
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void addView(View view, ViewGroup.LayoutParams params) {
        ((FrameLayout) itemView).addView(view, params);
    }
}
