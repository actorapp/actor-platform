package com.droidkit.mvvm.ui;

import android.app.Fragment;
import android.view.View;
import android.widget.Checkable;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class BinderFragment extends Fragment {

    private Binder binder = new Binder();

    public Binder getBinder() {
        return binder;
    }

    public <T> void bind(ValueModel<T> value, final Listener<T> p) {
        binder.bind(value, p);
    }

    public <V, T> void bind(final V obj, ValueModel<T> value, final Processor<V, T> p) {
        binder.bind(obj, value, p);
    }

    public void bindChecked(final Checkable checkable, ValueModel<Boolean> isChecked) {
        binder.bindChecked(checkable, isChecked);
    }

    public void bindNotChecked(final Checkable checkable, ValueModel<Boolean> isChecked) {
        binder.bindNotChecked(checkable, isChecked);
    }

    public void bindEnabled(final View view, ValueModel<Boolean> value) {
        binder.bindEnabled(view, value);
    }

    public void bindNotEnabled(final View view, ValueModel<Boolean> value) {
        binder.bindNotEnabled(view, value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder.unbindAll();
    }
}
