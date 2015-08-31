package im.actor.messenger.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class SelectionListenerEditText extends EditText {

    private OnSelectedListener selectionCallback;

    public SelectionListenerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionListenerEditText(Context context) {
        super(context);
    }

    public SelectionListenerEditText(Context context, AttributeSet attrs, int i) {
        super(context, attrs, i);
    }

    public void setOnSelectionListener(OnSelectedListener callback) {
        selectionCallback = callback;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selectionCallback != null) {
            selectionCallback.onSelected(selStart, selEnd);
        }
    }

    public interface OnSelectedListener {
        void onSelected(int selStart, int selEnd);
    }
}
