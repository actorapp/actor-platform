package im.actor.sdk.view.emoji.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import im.actor.sdk.R;
import im.actor.runtime.Log;
import im.actor.sdk.controllers.conversation.KeyboardLayout;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.util.Screen;

public class BaseKeyboard implements
        ViewTreeObserver.OnGlobalLayoutListener {


    private static final String TAG = "BaseKeyboard";
    protected Activity activity;
    private View decorView;
    private boolean softKeyboardListeningEnabled = true;
    private boolean showRequested = false;
    private boolean emojiKeyboardIsOpening;
    private InputMethodManager inputMethodManager;
    private View emojiKeyboardView;
    protected EditText messageBody;
    protected KeyboardLayout root;
    protected RelativeLayout container;
    public static final int OVERLAY_PERMISSION_REQ_CODE = 735;

    Boolean pendingOpen = false;

    protected KeyboardStatusListener keyboardStatusListener;

    final WindowManager windowManager;
    int keyboardHeight;
    private boolean showingPending;

    private boolean showing = false;
    //    private boolean dismissed;
    private boolean softwareKeyboardShowing;
    private KeyboardHelper keyboardHelper;
    private boolean keyboardMeasured = false;
    private int keyboardTriggerHeight = Screen.dp(150);

    public BaseKeyboard(Activity activity, EditText messageBody) {
        this.activity = activity;
        this.windowManager = activity.getWindowManager();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        //setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //default size
        keyboardHeight = (int) activity.getResources().getDimension(R.dimen.keyboard_height);
        keyboardHelper = new KeyboardHelper(activity);
        this.messageBody = messageBody;

        messageBody.setOnClickListener(view -> {
            if (showing) {
                dismiss();
            }
        });
        messageBody.setOnFocusChangeListener((view, b) -> {
            if (b && showing) {
                dismiss();
            }
        });

    }


    public int getHeight() {
        return keyboardHeight;
    }

    public void setKeyboardStatusListener(KeyboardStatusListener keyboardStatusListener) {
        this.keyboardStatusListener = keyboardStatusListener;
    }


    public void show() {
        if (isShowing()) {
            return;
        }

        softKeyboardListeningEnabled = true;
        this.root = (KeyboardLayout) messageBody.getRootView().findViewById(R.id.container).getParent();
        this.container = (RelativeLayout) messageBody.getRootView().findViewById(R.id.container);

        root.showInternal(keyboardHeight);
        showRequested = true;
        if (softwareKeyboardShowing) {
            keyboardHelper.setImeVisibility(messageBody, false);
        } else {
//            messageBody.setFocusableInTouchMode(true);
//            messageBody.requestFocus();
//            inputMethodManager.showSoftInput(messageBody, InputMethodManager.SHOW_IMPLICIT);
            container.setPadding(0, 0, 0, keyboardHeight);
            showInternal();
        }

    }

    protected void showInternal() {
        if (isShowing()) {
            return;
        }

        showRequested = false;
        showing = true;

        emojiKeyboardView = createView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
        params.gravity = Gravity.BOTTOM;
        root.addView(emojiKeyboardView, params);

        if (keyboardStatusListener != null) {
            keyboardStatusListener.onShow();
        }
        onShow();
    }

    private void update() {
        if (emojiKeyboardView != null) {
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
        dismissInternally(false);
    }

    public void dismiss(boolean dismissAll) {
        dismissInternally(dismissAll);
    }

    private void dismissInternally(boolean dismissAll) {
        showing = false;
        if (messageBody != null) {
            if (dismissAll) {
                keyboardHelper.setImeVisibility(messageBody, false);
            } else if (!softwareKeyboardShowing) {
                keyboardHelper.setImeVisibility(messageBody, true);
            }
        }
        if (root != null) {
            root.dismissInternal();
        }
        if (emojiKeyboardView != null && root != null && keyboardHelper != null) {
            final View emojiKeyboardViewCopy = emojiKeyboardView;
//            emojiKeyboardView
//                    .animate()
//                    .y(140)
//                    .alpha(0.2f)
//                    .setDuration(200)
//                    .setStartDelay(0)
//                    .setInterpolator(new AccelerateInterpolator(1.5f))
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            emojiKeyboardViewCopy.setVisibility(View.GONE);
//                            windowManager.removeView(emojiKeyboardViewCopy);
//                        }
//                    })
//                    .start();
            emojiKeyboardViewCopy.setVisibility(View.GONE);
            root.removeView(emojiKeyboardViewCopy);
            emojiKeyboardView = null;
            if (keyboardStatusListener != null) {
                keyboardStatusListener.onDismiss();
            }
            if (dismissAll) {
                container.setPadding(0, 0, 0, 0);
            }
            onDismiss();
        }
    }


    public void toggle() {
        if (isShowing()) {
            dismiss();
        } else {
            show();
        }
    }

    public boolean isShowing() {
        return showing && emojiKeyboardView != null;
    }


    public void destroy() {
        dismiss(true);

        if (keyboardStatusListener != null) {
            keyboardStatusListener.onDismiss();
        }
    }

    @Override
    public void onGlobalLayout() {

        Log.d(TAG, "onGlobalLayout");
        if (!softKeyboardListeningEnabled) {
            return;
        }
        Rect r = new Rect();
        messageBody.getWindowVisibleDisplayFrame(r);

        int screenHeight = messageBody.getRootView()
                .getHeight();


//        int widthDiff = decorView.getRootView().getWidth() - (r.right - r.left);
//        if (Math.abs(widthDiff) > 0) {
//            return;
//        }
        int resourceId = activity.getResources()
                .getIdentifier("status_bar_height",
                        "dimen", "android");
        int statusBarHeight = 0;

        if (resourceId > 0) {
            statusBarHeight = activity.getResources()
                    .getDimensionPixelSize(resourceId);
            screenHeight -= statusBarHeight;
        }


        screenHeight -= getViewInset(root, statusBarHeight);

        int heightDifference = screenHeight - (r.bottom - r.top);

        boolean changed = softwareKeyboardShowing;

        if (heightDifference > keyboardTriggerHeight) {

            softwareKeyboardShowing = true;

            Log.d(TAG, "onGlobalLayout: " + heightDifference);

            keyboardHeight = heightDifference;

            dismiss();

        } else {

            softwareKeyboardShowing = false;

            if (showRequested) {
                root.showInternal(keyboardHeight);
                showInternal();
            } else if (changed) {
                if (root != null) {
                    root.dismissInternal();
                }
            }
            Log.d(TAG, "onGlobalLayout: " + heightDifference);
            Log.d(TAG, "onGlobalLayout: " + "dismiss?");
            // dismiss not wirk
//            softwareKeyboardShowing = false;
            // keyboard showing or not?
//            dismissed = true;
//            dismissInternally();
        }

        changed = changed != softwareKeyboardShowing;
        Log.d(TAG, "keyboard state change:  " + changed);

        // FIXME verify root view applied new padding after keyboard state change
        // workaround for [some of android versions] bug, when keyboard closing not causing relayout, or causing it with delay
        if (changed && root != null) {

            root.postDelayed(() -> {
                if (!root.isSync()) {
                    root.requestLayout();
                }
            }, 30);
        }



    }

    public Activity getActivity() {
        return activity;
    }

    protected void onDismiss() {
        // override it
    }

    protected void onShow() {
        // override it
    }

    protected View createView() {
        TextView view = new TextView(activity);
        view.setText("Example of keyboard");
        view.setGravity(Gravity.CENTER);
        view.setBackgroundColor(0xffdadddf);

        return view;
    }

    public void onConfigurationChange() {
//        dismiss(true);
//        softwareKeyboardShowing = false;
    }

    public boolean onBackPressed() {
        if (showing) {
            dismiss(true);
            return true;
        }
        return false;
    }

    public static int getViewInset(View view, int statusBarHeight) {
        if (view == null || view.getRootView() == null) {
            return 0;
        }

        view = view.getRootView();

        if (Build.VERSION.SDK_INT < 21 || view.getHeight() == Screen.getHeight() || view.getHeight() == Screen.getHeight() - statusBarHeight) {
            return 0;
        }

        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
