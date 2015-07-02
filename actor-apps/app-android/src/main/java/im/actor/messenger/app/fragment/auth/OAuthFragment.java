package im.actor.messenger.app.fragment.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.messenger.R;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.model.modules.Auth;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by korka on 02.07.15.
 */
public class OAuthFragment extends BaseAuthFragment {
    WebView wv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        wv = new WebView(getActivity());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(messenger().getPreferences().getString(Auth.KEY_OAUTH_REDIRECT_URL));
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.startsWith("https://actor.im/auth/oauth2callback")) {
                    goneView(wv);
                    Uri u = Uri.parse(url);
                    String code = u.getQueryParameter("code");
                    executeAuth(messenger().requestCompleteOAuth(code), "Sign in");
                } else {
                    super.onLoadResource(view, url);
                }
            }
        });
        return wv;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_email_title);
    }
}
