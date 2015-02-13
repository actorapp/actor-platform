package im.actor.messenger.app.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.mvvm.ui.BinderFragment;

import im.actor.messenger.android.CallBarrier;
import im.actor.messenger.app.view.ViewUtils;
import im.actor.messenger.core.actors.base.UiActorAsk;
import im.actor.messenger.core.actors.base.UiAskCallback;

public class BaseFragment extends BinderFragment {

    private boolean showActionBarBackButton;

    private CallBarrier callBarrier = new CallBarrier();
    private UiActorAsk uiActorAsk = new UiActorAsk();

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        callBarrier.resume();
        uiActorAsk.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        callBarrier.pause();
        uiActorAsk.pause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (showActionBarBackButton == true) {
                    final Activity a = getActivity();
                    if (a != null) {
                        a.onBackPressed();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public BaseFragmentActivity getFragmentActivity() {
        final Activity a = getActivity();
        if (a == null || !(a instanceof BaseFragmentActivity)) {
            return null;
        } else {
            return (BaseFragmentActivity) a;
        }
    }

    public void setActionBarTitle(String title) {
        final BaseFragmentActivity a = getFragmentActivity();
        if (a != null) {
            a.getActionBar().setTitle(title);
        }
    }

    public void showActionBarBackButton(final boolean showBackButton) {
        final BaseFragmentActivity a = getFragmentActivity();
        if (a != null) {
            this.showActionBarBackButton = showBackButton;
            a.getActionBar().setDisplayHomeAsUpEnabled(showBackButton);
        }
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

    public <T> void ask(Future<T> future, final String progress, final UiAskCallback<T> callback) {
        ask(future, new UiAskCallback<T>() {
            private ProgressDialog progressDialog;

            @Override
            public void onPreStart() {
                progressDialog = new ProgressDialog(getActivity());
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

    public <T> void ask(Future<T> future, final UiAskCallback<T> callback) {
        uiActorAsk.ask(future, new UiAskCallback<T>() {
            @Override
            public void onPreStart() {
                callBarrier.call(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPreStart();
                    }
                });
            }

            @Override
            public void onCompleted(final T res) {
                callBarrier.call(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(res);
                    }
                });
            }

            @Override
            public void onError(final Throwable t) {
                callBarrier.call(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(t);
                    }
                });
            }
        });
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        } else {
            return 0;
        }
    }
}
