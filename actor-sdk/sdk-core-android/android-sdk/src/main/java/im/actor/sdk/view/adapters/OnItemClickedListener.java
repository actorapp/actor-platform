package im.actor.sdk.view.adapters;

public interface OnItemClickedListener<T> {

    void onClicked(T item);

    boolean onLongClicked(T item);
}
