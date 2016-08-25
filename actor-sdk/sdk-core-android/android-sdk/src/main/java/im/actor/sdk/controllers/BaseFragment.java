package im.actor.sdk.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.tools.MediaPickerCallback;
import im.actor.sdk.util.ViewUtils;

public class BaseFragment extends BinderCompatFragment implements MediaPickerCallback {

    protected final ActorStyle style = ActorSDK.sharedActor().style;

    private boolean isRootFragment;
    private String title;
    private int titleRes;
    private String subtitle;
    private boolean showTitle = true;
    private boolean homeAsUp = false;
    private boolean showHome = false;
    private boolean showCustom = false;

    private ArrayList<WrappedPromise> pending = new ArrayList<>();

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

    public <T> Promise<T> execute(Promise<T> promise) {
        return execute(promise, R.string.progress_common);
    }

    public <T> Promise<T> execute(Promise<T> promise, int title) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(title), true, false);
        promise.then(new Consumer<T>() {
            @Override
            public void apply(T t) {
                dismissDialog(dialog);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                dismissDialog(dialog);
            }
        });
        return promise;
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
            drawable.mutate();
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
            Drawable drawable = getResources().getDrawable(resourceId);
            drawable.mutate().setColorFilter(style.getSettingsIconColor(), PorterDuff.Mode.SRC_IN);
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
            drawable.mutate();
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

    protected <T> Promise<T> wrap(Promise<T> p) {
        WrappedPromise<T> res = new WrappedPromise<>((PromiseFunc<T>) resolver -> p.pipeTo(resolver));
        pending.add(res);
        return res;
    }

    @Override
    public void onPause() {
        super.onPause();

        for (WrappedPromise w : pending) {
            w.kill();
        }
        pending.clear();
    }

    public void finishActivity() {
        Activity a = getActivity();
        if (a != null) {
            a.finish();
        }
    }

    @Override
    public void onUriPicked(Uri uri) {

    }

    @Override
    public void onFilesPicked(List<String> paths) {

    }

    @Override
    public void onPhotoPicked(String path) {

    }

    @Override
    public void onVideoPicked(String path) {

    }

    @Override
    public void onPhotoCropped(String path) {

    }

    @Override
    public void onContactPicked(String name, List<String> phones, List<String> emails, byte[] avatar) {

    }

    @Override
    public void onLocationPicked(double latitude, double longitude, String street, String place) {

    }

    private class WrappedPromise<T> extends Promise<T> {

        private boolean isKilled;

        public WrappedPromise(PromiseFunc<T> executor) {
            super(executor);
        }

        public void kill() {
            isKilled = true;
        }

        @Override
        protected void invokeDeliver() {
            if (isKilled) {
                return;
            }
            super.invokeDeliver();
        }
    }
}
