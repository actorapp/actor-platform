package im.actor.sdk.controllers.fragment.preview;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.webkit.WebView;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.util.ViewUtils;
import im.actor.sdk.view.RTLUtils;

public class CodePreviewActivity extends BaseActivity {

    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        text = getIntent().getStringExtra("source_code");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);

        Drawable back_icon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        if(RTLUtils.isRTL(getApplicationContext())) {
            back_icon = ViewUtils.getRotateDrawable(back_icon, 180);
        }
        getSupportActionBar().setHomeAsUpIndicator(back_icon);

        actionBar.setTitle("Code Preview");

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        String data = "<html>\n" +
                "<header>\n" +
                "<link rel=\"stylesheet\" href=\"highlight-default.min.css\">\n" +
                "<script src=\"highlight.min.js\"></script>\n" +
                "<script>hljs.initHighlightingOnLoad();</script>\n" +
                "</header>\n" +
                "<body>\n" +
                "<pre><code>" +
                text.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("\n", "<br/>")
                + "</code></pre>" +
                "</body>\n" +
                "</html>";

        webView.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "utf-8", "");

        setContentView(webView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
