package im.actor.messenger.app.fragment.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import im.actor.model.modules.Auth;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by korka on 23.06.15.
 */
public class OAuthDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView wv = new WebView(this);
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
                    Uri u = Uri.parse(url);
                    String code = u.getQueryParameter("code");
                    setResult(RESULT_OK, new Intent().putExtra("code", code));
                    finish();
                } else {
                    super.onLoadResource(view, url);
                }
            }
        });

        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(wv, llp);

        setContentView(ll);
    }
}
