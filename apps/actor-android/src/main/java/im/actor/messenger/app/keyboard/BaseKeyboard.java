package im.actor.messenger.app.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.model.log.Log;

/**
 * Created by Jesus Christ. Amen.
 */
public class BaseKeyboard implements
        ViewTreeObserver.OnGlobalLayoutListener {

    protected Activity activity;
    private View decorView;
    private boolean softKeyboardListeningEnabled = true;
    private boolean emojiKeyboardIsOpening;
    private InputMethodManager inputMethodManager;
    private View emojiKeyboardView;
    protected EditText messageBody;

    Boolean pendingOpen = false;

    private KeyboardStatusListener keyboardStatusListener;

    final WindowManager windowManager;
    int keyboardHeight = 0;
    private boolean showingPending;

    private boolean showing;
    private boolean dismissed;
    private boolean softwareKeyboardShowing;

    public BaseKeyboard(Activity activity) {
        this.activity = activity;
        this.windowManager = activity.getWindowManager();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        //setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //default size
        keyboardHeight = (int) activity.getResources().getDimension(R.dimen.keyboard_height);
    }


    public int getHeight() {
        return keyboardHeight;
    }

    public void setKeyboardStatusListener(KeyboardStatusListener keyboardStatusListener) {
        this.keyboardStatusListener = keyboardStatusListener;
    }


    public void show(EditText messageBody) {
        this.messageBody = messageBody;
        showing = true;
        dismissed = false;
        if(softwareKeyboardShowing){
            showInternal();
        } else {
            messageBody.setFocusableInTouchMode(true);
            messageBody.requestFocus();
            inputMethodManager.showSoftInput(messageBody, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    private void showInternal() {
        if(showing == (emojiKeyboardView!=null)){
            return;
        }
        emojiKeyboardView = createView();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                (keyboardHeight),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        windowManager.addView(emojiKeyboardView, params);
        if(keyboardStatusListener!=null)
            keyboardStatusListener.onShow();
    }

    private void update() {
        if(emojiKeyboardView!=null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    (keyboardHeight),
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            windowManager.updateViewLayout(emojiKeyboardView, params);
        } else {

        }
    }

    public void dismiss() {
        dismissed = true;
        showing = false;
        dismissInternally();
    }
    private void dismissInternally(){
        if (dismissed && emojiKeyboardView != null) {
            windowManager.removeView(emojiKeyboardView);
            emojiKeyboardView = null;
            if(keyboardStatusListener!=null)
                keyboardStatusListener.onDismiss();
        }
    }


    public void toggle(EditText messageBody) {
        if (isShowing()) {
            dismiss();
        } else {
            show(messageBody);
        }
    }

    public boolean isShowing() {
        return emojiKeyboardView!=null;
    }



    @Override
    public void onGlobalLayout() {
        Log.d("EmojiKeyboard", "onGlobalLayout");
        if (!softKeyboardListeningEnabled) {
            return;
        }
        Rect r = new Rect();
        decorView.getWindowVisibleDisplayFrame(r);

        int screenHeight = decorView.getRootView()
                .getHeight();
        int heightDifference = screenHeight
                - (r.bottom - r.top);
        int resourceId = activity.getResources()
                .getIdentifier("status_bar_height",
                        "dimen", "android");
        if (resourceId > 0) {
            heightDifference -= activity.getResources()
                    .getDimensionPixelSize(resourceId);
        }
        int orientation = activity.getResources().getConfiguration().orientation;

        int id = activity.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            if (activity.getResources().getBoolean(id)) {
                int navbarResId = activity.getResources()
                        .getIdentifier(
                                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                                "dimen", "android");
                if (navbarResId > 0) {
                    heightDifference -= activity.getResources()
                            .getDimensionPixelSize(navbarResId);
                }
            }
        }

        if (heightDifference > 100) {
            Log.d("EmojiKeyboard", "onGlobalLayout: " + heightDifference);
            softwareKeyboardShowing = true;
            keyboardHeight = heightDifference;
            Log.d("EmojiKeyboard", "onGlobalLayout: " +  "showing");

            showInternal();
        } else {
            Log.d("EmojiKeyboard", "onGlobalLayout: " + heightDifference);
            Log.d("EmojiKeyboard", "onGlobalLayout: " +  "dismiss?");
            // dismiss not wirk
            softwareKeyboardShowing = false;
            // keyboard showing or not?
            dismissed = true;
            dismissInternally();
        }
    }

    protected View createView(){
        TextView view = new TextView(activity);
        view.setText("Example of keyboard");
        view.setGravity(Gravity.CENTER);
        view.setBackgroundColor(0xffdadddf);

        return view;
    }
}
