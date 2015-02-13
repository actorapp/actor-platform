package im.actor.messenger.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import im.actor.messenger.util.Logger;

/**
 * Created by ex3ndr on 20.10.14.
 */
public class LogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        webView.loadData(Logger.rawLog(), "text/plain", "utf-8");
        setContentView(webView);
    }
}
