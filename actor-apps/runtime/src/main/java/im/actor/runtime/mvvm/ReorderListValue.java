package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

public class ReorderListValue<T> extends LockableValue<ArrayList<T>> {

    public ReorderListValue(String name, Value<ArrayList<T>> baseValue) {
        super(name, baseValue);
    }

    @Override
    @ObjectiveCName("get")
    public ArrayList<T> get() {
        return super.get();
    }

    public void move(int from, int to) {
        if (from == to) {
            return;
        }
        ArrayList<T> src = getModifiedValue();
        ArrayList<T> res = new ArrayList<T>(src);
        T item = res.remove(from);
        res.add(to, item);
        change(res);
    }
}
