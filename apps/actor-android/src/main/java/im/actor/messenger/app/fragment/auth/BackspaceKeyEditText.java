package im.actor.messenger.app.fragment.auth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

public class BackspaceKeyEditText extends EditText {

    private BackspacePressListener listener;

    public BackspaceKeyEditText(Context context) {
        super(context);
    }

    public BackspaceKeyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackspaceKeyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BackspaceInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    public void setBackspaceListener(BackspacePressListener listener) {
        this.listener = listener;
    }

    public interface BackspacePressListener {
        boolean onBackspacePressed();
    }

    private class BackspaceInputConnection extends InputConnectionWrapper {

        public BackspaceInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

                if (listener != null) {
                    if (listener.onBackspacePressed()) {
                        return super.sendKeyEvent(event);
                    } else {
                        return false;
                    }
                } else {
                    return super.sendKeyEvent(event);
                }

                // Un-comment if you wish to pause the backspace:
                // return false;
            }
            return super.sendKeyEvent(event);
        }


        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }
}
