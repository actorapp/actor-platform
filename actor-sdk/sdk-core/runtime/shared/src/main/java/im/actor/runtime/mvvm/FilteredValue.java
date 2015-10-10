package im.actor.runtime.mvvm;

public class FilteredValue<T, S> extends Value<S> {

    private Value<T> baseValue;
    private S value;
    private ValueConverter<T, S> converter;
    private ValueChangedListener<T> changedListener = new ValueChangedListener<T>() {
        @Override
        public void onChanged(T val, Value<T> valueModel) {
            FilteredValue.this.value = converter.convert(val);
            notifyInMainThread(FilteredValue.this.value);
        }
    };

    public FilteredValue(String name, Value<T> valueModel, ValueConverter<T, S> converter) {
        super(name);

        this.converter = converter;
        this.baseValue = valueModel;
        this.value = converter.convert(valueModel.get());

        baseValue.subscribe(changedListener, false);
    }

    @Override
    public S get() {
        return value;
    }

    public void destroy() {
        baseValue.unsubscribe(changedListener);
    }
}
