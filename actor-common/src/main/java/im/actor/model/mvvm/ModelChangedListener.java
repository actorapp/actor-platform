package im.actor.model.mvvm;

/**
 * Created by ex3ndr on 23.02.15.
 */
public interface ModelChangedListener<T> {
    public void onChanged(T model);
}
