package im.actor.gwt.app.storage;

/**
 * Created by ex3ndr on 22.02.15.
 */
public interface JsListEngineCallback<T> {
    public void onItemAddedOrUpdated(T item);

    public void onItemRemoved(long id);

    public void onClear();
}