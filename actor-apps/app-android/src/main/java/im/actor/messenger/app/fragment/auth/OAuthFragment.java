package im.actor.messenger.app.fragment.auth;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import im.actor.messenger.R;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by korka on 02.07.15.
 */
public class OAuthFragment extends BaseAuthFragment {
    WebView wv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        wv = new WebView(getActivity());
        wv.getSettings().setJavaScriptEnabled(true);
        // TODO: Fix URL
        wv.loadUrl(messenger().getAuthEmail());
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
