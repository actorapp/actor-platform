package im.actor.messenger.app.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashSet;

/**
 * Created by ex3ndr on 07.10.14.
 */
public abstract class HolderAdapter<V> extends BaseAdapter {
    private HashSet<ViewHolder<V>> holders = new HashSet<ViewHolder<V>>();

    private Context context;

    protected HolderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public abstract V getItem(int position);

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {

        V obj = getItem(position);

        ViewHolder<V> holder;
        View view;
        if (convertView == null || convertView.getTag() == null) {
            holder = createHolder(obj);
            view = holder.init(obj, parent, context);
            view.setTag(holder);
            holders.add(holder);
        } else {
            holder = (ViewHolder<V>) convertView.getTag();
            view = convertView;
        }

        holder.bind(obj, position, context);

        return view;
    }

    public void onMovedToScrapHeap(View view) {
        if (view.getTag() instanceof ViewHolder) {
            ((ViewHolder) view.getTag()).unbind();
        }
    }

    public void dispose() {
        for (ViewHolder holder : holders) {
            holder.dispose();
        }
    }

    protected abstract ViewHolder<V> createHolder(V obj);
}
