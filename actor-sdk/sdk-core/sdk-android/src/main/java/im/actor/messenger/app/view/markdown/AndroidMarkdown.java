package im.actor.messenger.app.view.markdown;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;

import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.fragment.preview.CodePreviewActivity;
import im.actor.runtime.markdown.MDDocument;
import im.actor.runtime.markdown.MDRawText;
import im.actor.runtime.markdown.MDSection;
import im.actor.runtime.markdown.MDSpan;
import im.actor.runtime.markdown.MDText;
import im.actor.runtime.markdown.MDUrl;
import im.actor.runtime.markdown.MarkdownParser;

public class AndroidMarkdown {

    public static Spannable processOnlyLinks(String markdown) {
        return processtext(markdown, MarkdownParser.MODE_ONLY_LINKS);
    }

    public static Spannable processText(String markdown) {
        return processtext(markdown, MarkdownParser.MODE_FULL);
    }

    private static Spannable processtext(String markdown, int mode) {
        MDDocument doc = new MarkdownParser(mode).processDocument(markdown);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        boolean isFirst = true;
        for (MDSection s : doc.getSections()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append("\n");
            }
            if (s.getType() == MDSection.TYPE_CODE) {
                int start = builder.length();
                builder.append("View Source Code");
                final String text = s.getCode().getCode();
                builder.setSpan(new RelativeSizeSpan(1.1f), start, builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(Color.RED), start, builder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        AppContext.getContext().startActivity(
                                new Intent(AppContext.getContext(), CodePreviewActivity.class)
                                        .putExtra("source_code", text)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s.getType() == MDSection.TYPE_TEXT) {
                writeText(s.getText(), builder);
            } else {
                throw new RuntimeException("Unknown section type: " + s.getType());
            }
        }
        return builder;
    }

    private static void writeText(MDText[] texts, SpannableStringBuilder builder) {
        for (MDText text : texts) {
            if (text instanceof MDRawText) {
                builder.append(((MDRawText) text).getRawText());
            } else if (text instanceof MDSpan) {
                MDSpan span = (MDSpan) text;
                int start = builder.length();
                writeText(span.getChild(), builder);
                Object spanObj;
                if (span.getSpanType() == MDSpan.TYPE_BOLD) {
                    spanObj = new StyleSpan(Typeface.BOLD);
                } else {
                    spanObj = new StyleSpan(Typeface.ITALIC);
                }
                builder.setSpan(spanObj, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (text instanceof MDUrl) {
                final MDUrl url = (MDUrl) text;
                int start = builder.length();
                builder.append(url.getUrlTitle());
                builder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        AppContext.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse(url.getUrl()))
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                throw new RuntimeException("Unknown text type: " + text);
            }
        }
    }
}