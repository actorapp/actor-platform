package im.actor.model.mvvm;

/**
 * Created by ex3ndr on 19.02.15.
 */
public abstract class BaseValueModel<T> {
    private T rawObj;

    public BaseValueModel(T rawObj) {
        this.rawObj = rawObj;
    }

    void update(T rawObj) {
        this.rawObj = rawObj;
        updateValues(rawObj);
    }

    protected abstract void updateValues(T rawObj);
}
