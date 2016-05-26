package im.actor.sdk.controllers.activity;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.function.BiFunction;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.ActorBinder;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class BaseActivity extends AppCompatActivity {

    public static final ActorStyle STYLE = ActorSDK.sharedActor().style;

    private final ActorBinder BINDER = new ActorBinder();

    private boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActorSDK.sharedActor().waitForReady();
        super.onCreate(savedInstanceState);

        notifyOnResume();

        if (getSupportActionBar() != null && STYLE.getToolBarColor() != 0) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(STYLE.getToolBarColor()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onPerformBind();
        notifyOnResume();
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
    protected void onStop() {
        super.onStop();
        notifyOnPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyOnPause();
    }


    // Binding

    protected void onPerformBind() {

    }

    public void bind(final TextView textView, Value<String> value) {
        BINDER.bind(textView, value);
    }

    public <T> void bind(TextView textView, Value<T> value, Function<T, CharSequence> bind) {
        BINDER.bind(value, (val, valueModel) -> {
            textView.setText(bind.apply(val));
        });
    }

    public <T1, T2> void bind(TextView textView, Value<T1> value1, Value<T2> value2, BiFunction<T1, T2, CharSequence> bind) {
        BINDER.bind(value1, value2, (val, valueModel, val2, valueModel2) -> {
            textView.setText(bind.apply(val, val2));
        });
    }

    public void bind(final AvatarView avatarView, final int id,
                     final Value<Avatar> avatar, final Value<String> name) {
        BINDER.bind(avatarView, id, avatar, name);
    }

    public void bind(final TextView textView, final UserVM user) {
        BINDER.bind(textView, user);
    }

    public void bind(final TextView textView, View titleContainer, GroupVM value) {
        BINDER.bind(textView, titleContainer, value);
    }

    public void bindGlobalCounter(ValueChangedListener<Integer> callback) {
        BINDER.bindGlobalCounter(callback);
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

    public <T> void bind(Value<T> value, ValueChangedListener<T> listener, boolean notify) {
        BINDER.bind(value, listener, notify);
    }

    public <T, V> void bind(final Value<T> value1, final Value<V> value2,
                            final ValueDoubleChangedListener<T, V> listener) {
        BINDER.bind(value1, value2, listener);
    }

    // Toolbar

    protected void setToolbar(int text, boolean enableBack) {
        if (getSupportActionBar() == null) {
            throw new RuntimeException("Action bar is not set!");
        }
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
        if (getSupportActionBar() == null) {
            throw new RuntimeException("Action bar is not set!");
        }
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setCustomView(view, params);
        if (enableBack) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            Toolbar parent = (Toolbar) view.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
    }

    protected void setToolbar(View view, ActionBar.LayoutParams params) {
        setToolbar(view, params, true);
    }


    // Executions

    public <T> void execute(Command<T> cmd, int title, final CommandCallback<T> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(title));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                dismissDialog(progressDialog);
                callback.onResult(res);
            }

            @Override
            public void onError(Exception e) {
                dismissDialog(progressDialog);
                callback.onError(e);
            }
        });
    }

    public <T> void execute(Command<T> cmd) {
        execute(cmd, R.string.progress_common);
    }

    public <T> void execute(Command<T> cmd, final CommandCallback<T> callback) {
        cmd.start(callback);
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
                dismissDialog(progressDialog);
            }

            @Override
            public void onError(Exception e) {
                dismissDialog(progressDialog);
            }
        });
    }

    public <T> void execute(Promise<T> promise) {
        execute(promise, R.string.progress_common);
    }

    public <T> void execute(Promise<T> promise, int title) {
        final ProgressDialog dialog = ProgressDialog.show(this, "", getString(title), true, false);
        promise.then(t -> dismissDialog(dialog))
                .failure(e -> dismissDialog(dialog));
    }


    // Tools

    public void dismissDialog(ProgressDialog progressDialog) {
        try {
            progressDialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyOnResume() {
        if (isResumed) {
            return;
        }
        isResumed = true;

        messenger().onActivityOpen();
    }

    private void notifyOnPause() {
        if (!isResumed) {
            return;
        }
        isResumed = false;
        messenger().onActivityClosed();
    }

    protected boolean getIsResumed() {
        return isResumed;
    }
}
