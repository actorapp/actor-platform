package im.actor.sdk.view.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class RecyclerListView extends ListView {
    public RecyclerListView(Context context) {
        super(context);
    }

    public RecyclerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecyclerListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof HolderAdapter) {
            setRecyclerListener(new RecyclerListener() {
                @Override
                public void onMovedToScrapHeap(View view) {
                    ((HolderAdapter) adapter).onMovedToScrapHeap(view);
                }
            });
        }
    }
}
