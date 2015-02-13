package im.actor.messenger.app.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.list.view.ListEngineAdapter;

import java.util.HashSet;

/**
 * Created by ex3ndr on 13.09.14.
 */
public abstract class EngineHolderAdapter<V> extends ListEngineAdapter<V> {

    private HashSet<ViewHolder<V>> holders = new HashSet<ViewHolder<V>>();

    private Context context;

    public EngineHolderAdapter(EngineUiList<V> engine, Context context) {
        super(engine);
        this.context = context;
    }

    public EngineHolderAdapter(EngineUiList<V> engine, boolean stackFromBottom, Context context) {
        super(engine, stackFromBottom);
        this.context = context;
    }

    public EngineHolderAdapter(EngineUiList<V> engine, boolean stackFromBottom, boolean autoLoad, Context context) {
        super(engine, stackFromBottom, autoLoad);
        this.context = context;
    }

    public EngineHolderAdapter(EngineUiList<V> engine, boolean stackFromBottom, int loadGap, boolean autoUpdate, Context context) {
        super(engine, stackFromBottom, loadGap, autoUpdate);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public final View getView(V object, int position, View convertView, ViewGroup parent) {
        ViewHolder<V> holder;
        View view;
        if (convertView == null || convertView.getTag() == null) {
            holder = createHolder(object);
            view = holder.init(object, parent, context);
            view.setTag(holder);
            holders.add(holder);
        } else {
            holder = (ViewHolder<V>) convertView.getTag();
            view = convertView;
        }

        holder.bind(object, position, context);

        afterItemLoaded(object, position);

        return view;
    }

    public void onMovedToScrapHeap(View view) {
        if (view.getTag() instanceof ViewHolder) {
            ((ViewHolder) view.getTag()).unbind();
        }
    }

    public void dispose() {
        super.dispose();
        for (ViewHolder holder : holders) {
            holder.dispose();
        }
        holders.clear();
    }

    protected abstract ViewHolder<V> createHolder(V obj);

    protected void afterItemLoaded(V object, int position) {
    }
}