package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueDoubleListener<T1, T2> {

    @ObjectiveCName("onChangedWithVal1:withVal2:")
    void onChanged(T1 val1, T2 val2);
}
