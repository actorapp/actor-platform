package im.actor.sdk.discover;

import android.content.DialogInterface;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.SelectorFactory;
import im.actor.sdk.view.markdown.AndroidMarkdown;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class StoreFragment extends BaseFragment {

    private RecyclerView.OnScrollListener onScrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout fl = new FrameLayout(getActivity());
        fl.setPadding(0, Screen.dp(56), 0, 0);
        fl.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        final TextView placeHolder = new TextView(getActivity());
        placeHolder.setGravity(Gravity.CENTER);
        placeHolder.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        placeHolder.setText(R.string.progress_common);
        placeHolder.setTextSize(18);
        placeHolder.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());

        final WebView wv = new WebView(getActivity());
        wv.setVisibility(View.INVISIBLE);

        final FrameLayout buttonRetry = new FrameLayout(getActivity());
        buttonRetry.setBackgroundResource(R.drawable.shadow_square_3);
        final TextView retryBtnText = new TextView(getActivity());
        buttonRetry.addView(retryBtnText, FrameLayout.LayoutParams.WRAP_CONTENT, Screen.dp(48));
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        retryBtnText.setBackgroundDrawable(states);
        retryBtnText.setText(R.string.dialog_try_again);
        retryBtnText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        retryBtnText.setTypeface(Fonts.medium());
        retryBtnText.setTextSize(17);
        retryBtnText.setGravity(Gravity.CENTER);
        retryBtnText.setAllCaps(true);
        retryBtnText.setPadding(Screen.dp(24), 0, Screen.dp(24), 0);
        buttonRetry.setVisibility(View.INVISIBLE);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showView(placeHolder);
                hideView(buttonRetry);
                hideView(wv);
                wv.loadUrl(wv.getUrl());
            }
        });

        fl.addView(placeHolder, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        fl.addView(wv, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        fl.addView(buttonRetry, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {

            boolean errorHandled = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (wv.getVisibility() != View.VISIBLE && !errorHandled) {
                    showView(wv);
                    hideView(placeHolder);
                    hideView(buttonRetry);
                }
                errorHandled = false;
                super.onPageFinished(view, url);
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                errorHandled = true;
                hideView(wv);
                showView(buttonRetry);
                hideView(placeHolder);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("group://")) {
                    final String token = url.substring(8);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Join")
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    joinGroup(token);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                    return true;
                } else if (url.startsWith("user://")) {
                    String id = url.substring(7);
                    execute(messenger().findUsers(id));
                    return true;
                }

                AndroidMarkdown.buildChromeIntent().launchUrl(getActivity(), Uri.parse(url));
                return true;
            }
        });
        wv.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        wv.loadUrl("http://actorapp.github.io/store/");
        return fl;
    }

    private void joinGroup(String token) {
        Command<Integer> cmd = messenger().joinGroupViaToken(token);
        if (cmd != null) {
            execute(cmd, im.actor.sdk.R.string.invite_link_title, new CommandCallback<Integer>() {
                @Override
                public void onResult(Integer res) {
                    getActivity().startActivity(Intents.openGroupDialog(res, true, getActivity()));
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }
}
