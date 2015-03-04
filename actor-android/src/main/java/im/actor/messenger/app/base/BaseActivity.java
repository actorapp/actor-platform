package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.DoubleValueChangedListener;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupTypingVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserTypingVM;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.AppStateBroker.stateBroker;

/**
 * Created by ex3ndr on 29.12.14.
 */
public class BaseActivity extends ActionBarActivity {
    private final ActorBinder BINDER = new ActorBinder();

    @Override
    protected void onResume() {
        super.onResume();
        stateBroker().onActivityOpen();
    }

    public void bind(final TextView textView, ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public void bind(final AvatarView avatarView, final int id, final float size,
                     final ValueModel<Avatar> avatar, final ValueModel<String> name) {
        BINDER.bind(avatarView, id, size, avatar, name);
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        BINDER.bind(textView, container, user);
    }

    public void bind(final TextView textView, GroupVM value) {
        BINDER.bind(textView, value);
    }

    public void bind(final TextView textView, final View container, final View titleContainer, final UserTypingVM typing) {
        BINDER.bind(textView, container, titleContainer, typing);
    }

    public void bind(final TextView textView, final View container, final View titleContainer, final GroupTypingVM typing) {
        BINDER.bind(textView, container, titleContainer, typing);
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T, V> void bind(final ValueModel<T> value1, final ValueModel<V> value2,
                            final DoubleValueChangedListener<T, V> listener) {
        BINDER.bind(value1, value2, listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stateBroker().onActivityClose();
        BINDER.unbindAll();
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
                progressDialog.dismiss();
            }
        });
    }
}
