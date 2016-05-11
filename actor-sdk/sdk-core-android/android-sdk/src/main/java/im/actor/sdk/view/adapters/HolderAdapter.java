package im.actor.sdk.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashSet;

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

        onBindViewHolder(holder, obj, position, context);

        return view;
    }

    public void onMovedToScrapHeap(View view) {
        if (view.getTag() instanceof ViewHolder) {
            ((ViewHolder) view.getTag()).unbind(false);
        }
    }

    public void dispose() {
        for (ViewHolder holder : holders) {
            holder.unbind(true);
        }
    }

    protected void onBindViewHolder(ViewHolder<V> holder, V obj, int position, Context context) {
        holder.bind(obj, position, context);
    }

    protected abstract ViewHolder<V> createHolder(V obj);
}
