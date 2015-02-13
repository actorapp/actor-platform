package com.droidkit.mvvm.ui;

import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import com.droidkit.mvvm.ValueChangeListener;
import com.droidkit.mvvm.ValueModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class Binder {

    private HashMap<Object, BindContainer> binds = new HashMap<Object, BindContainer>();

    private BindContainer getContainer(Object view) {
        if (!binds.containsKey(view)) {
            binds.put(view, new BindContainer());
        }
        return binds.get(view);
    }

    public <V, T> void bind(final V obj, ValueModel<T> value, final Processor<V, T> p) {
        BindContainer container = getContainer(obj);

        container.customs.add(new Bind<T>(value, new ValueChangeListener<T>() {
            @Override
            public void onChanged(T value) {
                p.process(obj, value);
            }
        }));
    }

    public <T> void bind(ValueModel<T> value, final Listener<T> p) {
        BindContainer container = getContainer("<empty>");

        container.customs.add(new Bind<T>(value, new ValueChangeListener<T>() {
            @Override
            public void onChanged(T value) {
                p.onUpdated(value);
            }
        }));
    }

    public <T> void bind(Object bindObj, ValueModel<T> value, final Listener<T> p) {
        BindContainer container = getContainer(bindObj);

        container.customs.add(new Bind<T>(value, new ValueChangeListener<T>() {
            @Override
            public void onChanged(T value) {
                p.onUpdated(value);
            }
        }));
    }

    public void bindChecked(final Checkable checkable, ValueModel<Boolean> isChecked) {
        BindContainer container = getContainer(checkable);
        if (container.checked != null) {
            container.checked.release();
            container.checked = null;
        }
        container.checked = new Bind<Boolean>(isChecked, new ValueChangeListener<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                checkable.setChecked(value);
            }
        });
    }

    public void bindText(final TextView textView, ValueModel<String> textValue) {
        BindContainer container = getContainer(textView);
        if (container.text != null) {
            container.text.release();
            container.text = null;
        }
        container.text = new Bind<String>(textValue, new ValueChangeListener<String>() {
            @Override
            public void onChanged(String value) {
                textView.setText(value);
            }
        });
    }

    public <T> void bindValue(final Bindable<T> bindable, ValueModel<T> value) {
        BindContainer container = getContainer(bindable);
        if (container.value != null) {
            container.value.release();
            container.value = null;
        }
        container.value = new Bind<T>(value, new ValueChangeListener<T>() {
            @Override
            public void onChanged(T value) {
                bindable.bind(value);
            }
        });
    }

    public void bindOnClick(View view, final ValueModel<Boolean> value) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value.change(!value.getValue());
            }
        });
    }

    public void bindNotChecked(final Checkable checkable, ValueModel<Boolean> isChecked) {
        BindContainer container = getContainer(checkable);
        if (container.checked != null) {
            container.checked.release();
            container.checked = null;
        }
        container.checked = new Bind<Boolean>(isChecked, new ValueChangeListener<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                checkable.setChecked(!value);
            }
        });
    }

    public void bindEnabled(final View view, ValueModel<Boolean> value) {
        BindContainer container = getContainer(view);
        if (container.enabled != null) {
            container.enabled.release();
            container.enabled = null;
        }
        container.enabled = new Bind<Boolean>(value, new ValueChangeListener<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                view.setEnabled(value);
            }
        });
    }

    public void bindNotEnabled(final View view, ValueModel<Boolean> value) {
        BindContainer container = getContainer(view);
        if (container.enabled != null) {
            container.enabled.release();
            container.enabled = null;
        }
        container.enabled = new Bind<Boolean>(value, new ValueChangeListener<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                view.setEnabled(!value);
            }
        });
    }

    public void unbindEnabled(Object view) {
        BindContainer container = getContainer(view);
        if (container.enabled != null) {
            container.enabled.release();
            container.enabled = null;
        }
    }

    public void unbindChecked(Object view) {
        BindContainer container = getContainer(view);
        if (container.checked != null) {
            container.checked.release();
            container.checked = null;
        }
    }

    public void unbind(Object view) {
        BindContainer container = getContainer(view);
        container.release();
    }

    public void unbindAll() {
        for (BindContainer container : binds.values()) {
            container.release();
        }
        binds.clear();
    }

    private class BindContainer {
        private Bind visible;
        private Bind enabled;
        private Bind checked;
        private Bind value;
        private Bind text;

        private ArrayList<Bind> customs = new ArrayList<Bind>();

        public void release() {
            if (enabled != null) {
                enabled.release();
                enabled = null;
            }
            if (checked != null) {
                checked.release();
                checked = null;
            }

            if (visible != null) {
                visible.release();
                visible = null;
            }

            if (text != null) {
                text.release();
                text = null;
            }

            if (value != null) {
                value.release();
                value = null;
            }

            for (Bind b : customs) {
                b.release();
            }
            customs.clear();
        }
    }

    private class Bind<T> {
        private ValueModel<T> value;
        private ValueChangeListener<T> listener;

        private Bind(ValueModel<T> value, ValueChangeListener<T> listener) {
            this.value = value;
            this.listener = listener;
            this.value.addUiSubscriber(listener);
        }

        public void release() {
            value.removeUiSubscriber(listener);
        }
    }
}
