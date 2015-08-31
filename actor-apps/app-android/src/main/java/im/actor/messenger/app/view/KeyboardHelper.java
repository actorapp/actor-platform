package im.actor.messenger.app.view;

import android.content.Context;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeyboardHelper {

    private Context context;

    public KeyboardHelper(Context context) {
        this.context = context;
    }

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                //imm.showSoftInput(AutoFocusEditText.this, InputMethodManager.SHOW_FORCED);
                // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                Method showSoftInputUnchecked = null;
                try {
                    showSoftInputUnchecked = imm.getClass()
                            .getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
                } catch (NoSuchMethodException e) {
                    // Log something
                }

                if (showSoftInputUnchecked != null) {
                    try {
                        showSoftInputUnchecked.invoke(imm, 0, null);
                    } catch (IllegalAccessException e) {
                        // Log something
                    } catch (InvocationTargetException e) {
                        // Log something
                    }
                }
            }
        }
    };

    public void setImeVisibility(View view, final boolean visible) {
        if (visible) {
            //postDelayed(mShowImeRunnable, 100);
            view.post(mShowImeRunnable);
            view.postDelayed(mShowImeRunnable, 100);
        } else {
            view.removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
