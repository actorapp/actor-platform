package com.droidkit.mvvm.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.droidkit.mvvm.ValueChangeListener;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 16.09.14.
 */
public class BindedTextView extends TextView implements ValueChangeListener<String> {

    private ValueModel<String> value;

    public BindedTextView(Context context) {
        super(context);
    }

    public BindedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BindedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bind(ValueModel<String> value) {
        if (this.value == value) {
            return;
        }
        unbind();
        this.value = value;
        this.value.addUiSubscriber(this);
    }

    public void unbind() {
        if (this.value != null) {
            this.value.removeUiSubscriber(this);
        }
        this.value = null;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        unbind();
        super.setText(text, type);
    }

    @Override
    public void onChanged(String value) {
        setText(value);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbind();
    }
}
