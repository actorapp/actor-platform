package im.actor.messenger.app.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.mvvm.ui.Binder;

import im.actor.messenger.R;
import im.actor.messenger.app.view.ViewUtils;
import im.actor.messenger.core.actors.base.UiActorAsk;
import im.actor.messenger.core.actors.base.UiAskCallback;

import static im.actor.messenger.core.actors.AppStateBroker.stateBroker;

public class BaseActivity extends Activity {

    private final Binder BINDER = new Binder();

    private UiActorAsk ACTOR_ACK = new UiActorAsk();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
    }

    protected Binder getBinder() {
        return BINDER;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ACTOR_ACK.resume();
        stateBroker().onActivityOpen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stateBroker().onActivityClose();
        ACTOR_ACK.pause();
        BINDER.unbindAll();
    }

    public void goneView(View view) {
        ViewUtils.goneView(view);
    }

    public void goneView(final View view, boolean isAnimated) {
        ViewUtils.goneView(view, isAnimated);
    }

    public void goneView(final View view, boolean isAnimated, boolean isSlow) {
        ViewUtils.goneView(view, isAnimated, isSlow);
    }

    public void hideView(View view) {
        ViewUtils.hideView(view);
    }

    public void hideView(final View view, boolean isAnimated) {
        ViewUtils.hideView(view, isAnimated);
    }

    public void hideView(final View view, boolean isAnimated, boolean isSlow) {
        ViewUtils.hideView(view, isAnimated, isSlow);
    }

    public void showView(View view) {
        ViewUtils.showView(view);
    }

    public void showView(final View view, boolean isAnimated) {
        ViewUtils.showView(view, isAnimated);
    }

    public void showView(final View view, boolean isAnimated, boolean isSlow) {
        ViewUtils.showView(view, isAnimated, isSlow);
    }

    public <T> void ask(Future<T> future, final UiAskCallback<T> callback) {
        ACTOR_ACK.ask(future, callback);
    }

    public <T> void ask(Future<T> future, final String progress, final UiAskCallback<T> callback) {
        ACTOR_ACK.ask(future, new UiAskCallback<T>() {
            private ProgressDialog progressDialog;

            @Override
            public void onPreStart() {
                progressDialog = new ProgressDialog(BaseActivity.this);
                progressDialog.setMessage(progress);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                callback.onPreStart();
            }

            @Override
            public void onCompleted(T res) {
                progressDialog.dismiss();
                callback.onCompleted(res);
            }

            @Override
            public void onError(Throwable t) {
                progressDialog.dismiss();
                callback.onError(t);
            }
        });
    }
}