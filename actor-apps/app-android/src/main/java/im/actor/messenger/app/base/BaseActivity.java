package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueDoubleChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.AppStateBroker.stateBroker;

/**
 * Created by ex3ndr on 29.12.14.
 */
public class BaseActivity extends AppCompatActivity {
    private final ActorBinder BINDER = new ActorBinder();

    private boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notifyOnResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyOnResume();
    }

    public void bind(final TextView textView, ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public void bind(final AvatarView avatarView, final int id,
                     final ValueModel<Avatar> avatar, final ValueModel<String> name) {
        BINDER.bind(avatarView, id, avatar, name);
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        BINDER.bind(textView, container, user);
    }

    public void bind(final TextView textView, View titleContainer, GroupVM value) {
        BINDER.bind(textView, titleContainer, value);
    }

    public void bindGroupTyping(final TextView textView, final View container, final View titleContainer, final ValueModel<int[]> typing) {
        BINDER.bindGroupTyping(textView, container, titleContainer, typing);
    }

    public void bindPrivateTyping(final TextView textView, final View container, final View titleContainer, final ValueModel<Boolean> typing) {
        BINDER.bindPrivateTyping(textView, container, titleContainer, typing);
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T, V> void bind(final ValueModel<T> value1, final ValueModel<V> value2,
                            final ValueDoubleChangedListener<T, V> listener) {
        BINDER.bind(value1, value2, listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BINDER.unbindAll();
        notifyOnPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        notifyOnPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyOnPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        notifyOnPause();
    }

    private void notifyOnResume() {
        if (isResumed) {
            return;
        }
        isResumed = true;
        stateBroker().onActivityOpen();
    }

    private void notifyOnPause() {
        if (!isResumed) {
            return;
        }
        isResumed = false;
        stateBroker().onActivityClose();
    }

    public <T> void execute(Command<T> cmd, int title, final CommandCallback<T> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(title));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                progressDialog.dismiss();
                callback.onResult(res);
            }

            @Override
            public void onError(Exception e) {
                progressDialog.dismiss();
                callback.onError(e);
            }
        });
    }

    public <T> void execute(Command<T> cmd) {
        execute(cmd, R.string.progress_common);
    }

    public <T> void execute(Command<T> cmd, int title) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(title));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Exception e) {
                try {
                    progressDialog.dismiss();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
