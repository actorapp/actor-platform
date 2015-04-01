package im.actor.model.mvvm;

/**
 * Created by ex3ndr on 25.02.15.
 */
public interface ValueTripleChangedListener<T, V, S> {
    public void onChanged(T val, ValueModel<T> valueModel, V val2, ValueModel<V> valueModel2,
                          S val3, ValueModel<S> valueModel3);
}
