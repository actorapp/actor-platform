package im.actor.messenger.app.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.core.Core;
import im.actor.messenger.app.fragment.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.Value;

public class BaseActivity extends AppCompatActivity {
    private final ActorBinder BINDER = new ActorBinder();

    private boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateToolbar();
        notifyOnResume();
    }

    protected void onCreateToolbar() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        onPerformBind();
        notifyOnResume();
    }

    protected void onPerformBind() {

    }

    public void bind(final TextView textView, Value<String> value) {
        BINDER.bind(textView, value);
    }

    public void bind(final AvatarView avatarView, final int id,
                     final Value<Avatar> avatar, final Value<String> name) {
        BINDER.bind(avatarView, id, avatar, name);
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        BINDER.bind(textView, container, user);
    }

    public void bind(final TextView textView, View titleContainer, GroupVM value) {
        BINDER.bind(textView, titleContainer, value);
    }

    public void bindGroupTyping(final TextView textView, final View container, final View titleContainer, final Value<int[]> typing) {
        BINDER.bindGroupTyping(textView, container, titleContainer, typing);
    }

    public void bindPrivateTyping(final TextView textView, final View container, final View titleContainer, final Value<Boolean> typing) {
        BINDER.bindPrivateTyping(textView, container, titleContainer, typing);
    }

    public <T> void bind(Value<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T, V> void bind(final Value<T> value1, final Value<V> value2,
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

    protected void setToolbar(int text, boolean enableBack) {
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (enableBack) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(text);
    }

    protected void setToolbar(int text) {
        setToolbar(text, true);
    }

    protected void setToolbar(View view, ActionBar.LayoutParams params, boolean enableBack) {
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (enableBack) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setCustomView(view, params);
    }

    protected void setToolbar(View view, ActionBar.LayoutParams params) {
        setToolbar(view, params, true);
    }

    private void notifyOnResume() {
        if (isResumed) {
            return;
        }
        isResumed = true;

        Core.messenger().onActivityOpen();
    }

    private void notifyOnPause() {
        if (!isResumed) {
            return;
        }
        isResumed = false;
        Core.messenger().onActivityClosed();
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
