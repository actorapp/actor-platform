package im.actor.messenger.app.view;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;


/**
 * Created by korka on 26.05.15.
 */
public class SelectionListenerEdittext extends EditText {
    OnSelectedListener selectionCallback;

    public SelectionListenerEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionListenerEdittext(Context context){
        super(context);
    }

    public SelectionListenerEdittext(Context context, AttributeSet attrs, int i){
        super(context, attrs, i);
    }


    public void setOnSelectionListener(OnSelectedListener callback){
        selectionCallback = callback;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if(selectionCallback!=null)selectionCallback.onSelected(selStart, selEnd);
    }

    public interface OnSelectedListener {
        void onSelected(int selStart, int selEnd);
    }
}
