package im.actor.model.mvvm;

/**
 * Created by ex3ndr on 25.02.15.
 */
public interface DoubleValueChangedListener<T, V> {
    public void onChanged(T val, ValueModel<T> valueModel, V val2, ValueModel<V> valueModel2);
}
