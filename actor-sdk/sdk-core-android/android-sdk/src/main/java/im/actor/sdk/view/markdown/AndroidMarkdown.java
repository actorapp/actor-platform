package im.actor.sdk.view.markdown;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import im.actor.runtime.actors.ActorContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.ChatActivity;
import im.actor.sdk.controllers.fragment.preview.CodePreviewActivity;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.markdown.MDDocument;
import im.actor.runtime.markdown.MDRawText;
import im.actor.runtime.markdown.MDSection;
import im.actor.runtime.markdown.MDSpan;
import im.actor.runtime.markdown.MDText;
import im.actor.runtime.markdown.MDUrl;
import im.actor.runtime.markdown.MarkdownParser;
import im.actor.sdk.receivers.ChromeCustomTabReceiver;

import android.support.customtabs.CustomTabsIntent;


public class AndroidMarkdown {

    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    public static final String EXTRA_CUSTOM_TABS_BACK_BUTTON = "android.support.customtabs.extra.CLOSE_BUTTON_ICON";
    private static final String KEY_CUSTOM_TABS_ICON = "android.support.customtabs.customaction.ICON";
    public static final String KEY_CUSTOM_TABS_PENDING_INTENT = "android.support.customtabs.customaction.PENDING_INTENT";
    public static final String EXTRA_CUSTOM_TABS_ACTION_BUTTON_BUNDLE = "android.support.customtabs.extra.ACTION_BUNDLE_BUTTON";

    public static Spannable processOnlyLinks(String markdown) {
        return processText(markdown, MarkdownParser.MODE_ONLY_LINKS);
    }

    public static Spannable processText(String markdown) {
        return processText(markdown, MarkdownParser.MODE_FULL);
    }

    private static Spannable processText(String markdown, int mode) {
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
                        AndroidContext.getContext().startActivity(
                                new Intent(AndroidContext.getContext(), CodePreviewActivity.class)
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
                        Context ctx = view.getContext();
                        if (url.getUrl().startsWith("send:")) {
                            ctx = extractContext(ctx);
                            if (ctx instanceof ChatActivity) {
                                ActorSDK.sharedActor().getMessenger().sendMessage(((ChatActivity) ctx).getPeer(), url.getUrl().replace("send:", ""));
                            }
                        } else {
                            Intent intent = buildChromeIntent().intent;
                            intent.setData(Uri.parse(url.getUrl()));
                            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                                ctx.startActivity(
                                        intent);
                            } else {
                                intent.setData(Uri.parse("http://" + url.getUrl()));
                                if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                                    ctx.startActivity(
                                            intent);
                                } else {
                                    Toast.makeText(view.getContext(), "Unknown URL type", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    }
                }, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                throw new RuntimeException("Unknown text type: " + text);
            }
        }
    }

    private static Context extractContext(Context ctx) {
        if (ctx instanceof AppCompatActivity) {
            return ctx;
        } else if (ctx instanceof ContextWrapper) {
            return extractContext(((ContextWrapper) ctx).getBaseContext());
        }

        return ctx;
    }

    public static CustomTabsIntent buildChromeIntent() {
        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

//        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setType("*/*");
//        PendingIntent pi = PendingIntent.getActivity(AndroidContext.getContext()    , 0, sendIntent, 0);

        Intent actionIntent = new Intent(
                AndroidContext.getContext(), ChromeCustomTabReceiver.class);
        PendingIntent pi =
                PendingIntent.getBroadcast(AndroidContext.getContext(), 0, actionIntent, 0);

        customTabsIntent.setToolbarColor(ActorSDK.sharedActor().style.getMainColor())
                .setActionButton(BitmapFactory.decodeResource(AndroidContext.getContext().getResources(), R.drawable.ic_share_white_24dp), "Share", pi)
                .setCloseButtonIcon(BitmapFactory.decodeResource(AndroidContext.getContext().getResources(), R.drawable.ic_arrow_back_white_24dp));

        return customTabsIntent.build();
    }
}