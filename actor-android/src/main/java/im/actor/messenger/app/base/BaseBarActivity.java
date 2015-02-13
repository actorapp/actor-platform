package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.mvvm.ui.Binder;

import im.actor.messenger.core.actors.base.UiActorAsk;
import im.actor.messenger.core.actors.base.UiAskCallback;

import static im.actor.messenger.core.actors.AppStateBroker.stateBroker;

/**
 * Created by ex3ndr on 29.12.14.
 */
public class BaseBarActivity extends ActionBarActivity {
    private final Binder BINDER = new Binder();

    private UiActorAsk ACTOR_ACK = new UiActorAsk();

    public Binder getBinder() {
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

    public <T> void ask(Future<T> future, final UiAskCallback<T> callback) {
        ACTOR_ACK.ask(future, callback);
    }

    public <T> void ask(Future<T> future, final String progress, final UiAskCallback<T> callback) {
        ACTOR_ACK.ask(future, new UiAskCallback<T>() {
            private ProgressDialog progressDialog;

            @Override
            public void onPreStart() {
                progressDialog = new ProgressDialog(BaseBarActivity.this);
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
