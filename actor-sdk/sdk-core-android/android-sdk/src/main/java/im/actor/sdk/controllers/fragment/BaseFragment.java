package im.actor.sdk.controllers.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.R;
import im.actor.sdk.util.ViewUtils;

public class BaseFragment extends BinderCompatFragment {

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setHasOptionsMenu(true);
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

    public void elevateView(View view) {
        ViewUtils.elevateView(view);
    }

    public void elevateView(final View view, boolean isAnimated) {
        ViewUtils.elevateView(view, isAnimated);
    }

    public void demoteView(View view) {
        ViewUtils.demoteView(view);
    }

    public void demoteView(final View view, boolean isAnimated) {
        ViewUtils.demoteView(view, isAnimated);
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

    public <T> void executeSilent(Command<T> cmd, final CommandCallback<T> callback) {
        cmd.start(callback);
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

    public <T> void execute(Command<T> cmd) {
        execute(cmd, R.string.progress_common);
    }

    public <T> void execute(Command<T> cmd, int title) {

        final AppCompatDialog dialog = new AppCompatDialog(getActivity());
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.show();

        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                dialog.dismiss();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
            }
        });
    }
}
