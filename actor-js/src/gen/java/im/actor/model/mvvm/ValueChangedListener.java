package im.actor.model.mvvm;

/**
 * Created by ex3ndr on 19.02.15.
 */
public interface ValueChangedListener<T> {
    public void onChanged(T val, ValueModel<T> valueModel);
}