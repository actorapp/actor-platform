package im.actor.messenger.app.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.droidkit.mvvm.ui.BinderFragment;

import im.actor.android.CallBarrier;
import im.actor.messenger.app.view.ViewUtils;

public class BaseFragment extends BinderFragment {

    private boolean showActionBarBackButton;

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
}
