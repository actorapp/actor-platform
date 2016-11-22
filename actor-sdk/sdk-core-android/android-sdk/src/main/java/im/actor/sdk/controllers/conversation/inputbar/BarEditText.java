package im.actor.sdk.controllers.conversation.inputbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.concurrent.CopyOnWriteArrayList;

public class BarEditText extends EditText {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int start, int length);
    }

    private CopyOnWriteArrayList<OnSelectionChangedListener> listeners = new CopyOnWriteArrayList<>();

    public BarEditText(Context context) {
        super(context);
    }

    public BarEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BarEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        // Constructor automatically calls it before our constructor is called
        if (listeners != null) {
            for (OnSelectionChangedListener l : listeners) {
                l.onSelectionChanged(selStart, selStart - selEnd);
            }
        }
    }

    public void addSelectionListener(OnSelectionChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeSelectionListener(OnSelectionChangedListener listener) {
        listeners.remove(listener);
    }
}
