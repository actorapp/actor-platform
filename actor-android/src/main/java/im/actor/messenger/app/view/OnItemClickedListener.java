package im.actor.messenger.app.view;

/**
 * Created by ex3ndr on 22.10.14.
 */
public interface OnItemClickedListener<T> {
    public void onClicked(T item);

    public boolean onLongClicked(T item);
}
