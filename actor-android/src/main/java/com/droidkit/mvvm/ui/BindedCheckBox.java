package com.droidkit.mvvm.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import com.droidkit.mvvm.ValueChangeListener;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class BindedCheckBox extends CheckBox {

    private ValueChangeListener<Boolean> valueListener = new ValueChangeListener<Boolean>() {
        @Override
        public void onChanged(Boolean value) {
            BindedCheckBox.super.setChecked(value);
        }
    };
    private ValueModel<Boolean> valueViewModel;

    private ValueChangeListener<Boolean> enabledListener = new ValueChangeListener<Boolean>() {
        @Override
        public void onChanged(Boolean value) {
            BindedCheckBox.super.setEnabled(value);
        }
    };
    private ValueModel<Boolean> enabledViewModel;

    public BindedCheckBox(Context context) {
        super(context);
    }

    public BindedCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BindedCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bindValue(ValueModel<Boolean> value) {
        if (this.valueViewModel == value) {
            return;
        }
        unbindValue();
        this.valueViewModel = value;
        this.valueViewModel.addUiSubscriber(valueListener);
    }

    public void unbindValue() {
        if (this.valueViewModel != null) {
            this.valueViewModel.removeUiSubscriber(valueListener);
        }
        this.valueViewModel = null;
    }

    public void bindEnabled(ValueModel<Boolean> value) {
        if (this.enabledViewModel == value) {
            return;
        }
        unbindEnabled();
        this.enabledViewModel = value;
        this.enabledViewModel.addUiSubscriber(enabledListener);
    }

    public void unbindEnabled() {
        if (this.enabledViewModel != null) {
            this.enabledViewModel.removeUiSubscriber(enabledListener);
        }
        this.enabledViewModel = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        unbindEnabled();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        unbindValue();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbindValue();
    }
}
