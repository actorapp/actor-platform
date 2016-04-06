package im.actor.sdk.controllers.fragment;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.ViewUtils;

public class BaseFragment extends BinderCompatFragment {

    protected final ActorStyle style = ActorSDK.sharedActor().style;

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
}
