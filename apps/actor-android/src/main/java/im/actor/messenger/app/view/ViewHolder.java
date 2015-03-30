package im.actor.messenger.app.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewHolder<T> {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private View view;

    @Deprecated
    public abstract View init(T data, ViewGroup viewGroup, Context context);

    public abstract void bind(T data, int position, Context context);

    public void unbind() {

    }

    public void dispose() {

    }

    @Deprecated
    public View getView(View recycleView, T data, int pos, ViewGroup viewGroup, Context context) {
        view = recycleView;
        if (view == null) {
            view = init(data, viewGroup, context);
            view.setTag(this);
        }

        bind(data, pos, context);

        return view;
    }
}
