package im.actor.sdk.controllers.fragment;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.ViewUtils;

public class BaseFragment extends BinderCompatFragment {

    protected final ActorStyle style = ActorSDK.sharedActor().style;

    private boolean isRootFragment;
    private String title;
    private int titleRes;
    private String subtitle;
    private boolean showTitle = true;
    private boolean homeAsUp = false;
    private boolean showHome = false;
    private boolean showCustom = false;

    public boolean isRootFragment() {
        return isRootFragment;
    }

    public void setRootFragment(boolean rootFragment) {
        isRootFragment = rootFragment;
        setHasOptionsMenu(rootFragment);
    }

    public void setTitle(String title) {
        this.title = title;
        this.titleRes = 0;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public void setTitle(int titleRes) {
        this.title = null;
        this.titleRes = titleRes;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(titleRes);
        }
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(showTitle);
        }
    }

    public void setHomeAsUp(boolean homeAsUp) {
        this.homeAsUp = homeAsUp;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(homeAsUp);
        }
    }

    public void setShowHome(boolean showHome) {
        this.showHome = showHome;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(showHome);
        }
    }

    public void setShowCustom(boolean showCustom) {
        this.showCustom = showCustom;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(showCustom);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isRootFragment) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                if (titleRes != 0) {
                    actionBar.setTitle(titleRes);
                } else {
                    actionBar.setTitle(title);
                }
                actionBar.setSubtitle(subtitle);
                actionBar.setDisplayShowCustomEnabled(showCustom);
                actionBar.setDisplayHomeAsUpEnabled(homeAsUp);
                actionBar.setDisplayShowHomeEnabled(showHome);
                actionBar.setDisplayShowTitleEnabled(showTitle);
                onConfigureActionBar(actionBar);
            }
        }
    }

    public void onConfigureActionBar(ActionBar actionBar) {

    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        // setHasOptionsMenu(true);
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

    public void wave(View[] layers, float scale, int duration, float offset) {
        ViewUtils.wave(layers, scale, duration, offset);
    }

    public void elevateView(View view) {
        ViewUtils.elevateView(view);
    }

    public void elevateView(View view, float scale) {
        ViewUtils.elevateView(view, scale);
    }

    public void elevateView(View view, boolean isAnamated, float scale) {
        ViewUtils.elevateView(view, isAnamated, scale);
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

    public <T> void executeSilent(Command<T> cmd) {
        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public <T> void execute(Command<T> cmd, int title, final CommandCallback<T> callback) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(title), true, false);
        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                dismissDialog(dialog);
                ;
                callback.onResult(res);
            }

            @Override
            public void onError(Exception e) {
                dismissDialog(dialog);
                ;
                callback.onError(e);
            }
        });
    }

    public <T> void execute(Command<T> cmd) {
        execute(cmd, R.string.progress_common);
    }

    public <T> void execute(Command<T> cmd, int title) {

        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(title), true, false);

        cmd.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                dismissDialog(dialog);
                ;
            }

            @Override
            public void onError(Exception e) {
                dismissDialog(dialog);
                ;
            }
        });
    }

    public <T> void execute(Promise<T> promise) {
        execute(promise, R.string.progress_common);
    }

    public <T> void execute(Promise<T> promise, int title) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(title), true, false);
        promise
                .then(new Consumer<T>() {
                    @Override
                    public void apply(T t) {
                        dismissDialog(dialog);
                    }
                })
                .failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        dismissDialog(dialog);
                    }
                });
    }

    public View buildRecord(String titleText, String valueText,
                            LayoutInflater inflater, ViewGroup container) {
        return buildRecord(titleText, valueText, 0, false, true, inflater, container);
    }

    public View buildRecord(String titleText, String valueText, boolean isLast,
                            LayoutInflater inflater, ViewGroup container) {
        return buildRecord(titleText, valueText, 0, false, isLast, inflater, container);
    }

    public View buildRecord(String titleText, String valueText, int resourceId, boolean showIcon, boolean isLast,
                            LayoutInflater inflater, ViewGroup container) {
        final View recordView = inflater.inflate(R.layout.contact_record, container, false);
        TextView value = (TextView) recordView.findViewById(R.id.value);
        TextView title = (TextView) recordView.findViewById(R.id.title);

        title.setText(titleText);
        title.setTextColor(style.getTextSecondaryColor());

        value.setTextColor(style.getTextPrimaryColor());
        value.setText(valueText);

        if (!isLast) {
            recordView.findViewById(R.id.divider).setVisibility(View.GONE);
        }

        if (resourceId != 0 && showIcon) {
            ImageView iconView = (ImageView) recordView.findViewById(R.id.recordIcon);
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(resourceId));
            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
            iconView.setImageDrawable(drawable);
        }

        container.addView(recordView);

        return recordView;
    }

    public View buildRecordBig(String valueText, int resourceId, boolean showIcon, boolean isLast,
                               LayoutInflater inflater, ViewGroup container) {
        final View recordView = inflater.inflate(R.layout.contact_record_big, container, false);
        TextView value = (TextView) recordView.findViewById(R.id.value);

        value.setTextColor(style.getTextPrimaryColor());
        value.setText(valueText);

        if (!isLast) {
            recordView.findViewById(R.id.divider).setVisibility(View.GONE);
        }

        if (resourceId != 0 && showIcon) {
            ImageView iconView = (ImageView) recordView.findViewById(R.id.recordIcon);
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(resourceId));
            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
            iconView.setImageDrawable(drawable);
        }

        container.addView(recordView);

        return recordView;
    }

    public View buildRecordAction(String valueText, int resourceId, boolean showIcon, boolean isLast,
                                  LayoutInflater inflater, ViewGroup container) {
        final View recordView = inflater.inflate(R.layout.contact_record_big, container, false);
        TextView value = (TextView) recordView.findViewById(R.id.value);

        value.setTextColor(style.getGroupActionAddTextColor());
        value.setText(valueText);

        if (!isLast) {
            recordView.findViewById(R.id.divider).setVisibility(View.GONE);
        }

        if (resourceId != 0 && showIcon) {
            ImageView iconView = (ImageView) recordView.findViewById(R.id.recordIcon);
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(resourceId));
            DrawableCompat.setTint(drawable, style.getGroupActionAddIconColor());
            iconView.setImageDrawable(drawable);
        }

        container.addView(recordView);

        return recordView;
    }

    public void dismissDialog(ProgressDialog progressDialog) {
        try {
            progressDialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    public ActionBar getSupportActionBar() {
        FragmentActivity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity compatActivity = (AppCompatActivity) activity;
            return compatActivity.getSupportActionBar();
        }
        return null;
    }
}
