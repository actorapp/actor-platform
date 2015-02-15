package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import com.droidkit.mvvm.ui.BinderCompatFragment;

import im.actor.android.CallBarrier;
import im.actor.messenger.app.view.ViewUtils;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;

public class BaseCompatFragment extends BinderCompatFragment {

    private CallBarrier callBarrier = new CallBarrier();

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        callBarrier.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        callBarrier.pause();
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

    public void onClick(View view, int id, final View.OnClickListener listener) {
        onClick(view.findViewById(id), listener);
    }

    public void onClick(View view, final View.OnClickListener listener) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
            }
        });
    }

    public <T> void execute(Command<T> cmd, int title, final CommandCallback<T> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
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
}
