package im.actor.core.js.utils;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import im.actor.runtime.markdown.*;

import java.util.ArrayList;

public class HtmlMarkdownUtils {
    public static String processText(String markdown, int mode) {
        MDDocument doc = new MarkdownParser(mode).processDocument(markdown);

        ArrayList<String> renderedSections = new ArrayList<String>();

        for (MDSection section : doc.getSections()) {
            renderedSections.add(renderSection(section));
        }

        StringBuilder builder = new StringBuilder();

        for (String section : renderedSections) {
            builder.append("<p>");
            builder.append(section);
            builder.append("</p>");
        }

        return builder.toString();
    }

    public static String renderSection(MDSection section) {
        if (section.getType() == MDSection.TYPE_CODE) {
            return renderCode(section.getCode());
        } else if (section.getType() == MDSection.TYPE_TEXT) {
            return renderText(section.getText());
        } else {
            return "";
        }
    }

    public static String renderCode(MDCode code) {
        return "<pre><code>" + SafeHtmlUtils.htmlEscape(code.getCode()) + "</pre></code>";
    }

    public static String renderText(MDText[] texts) {
        StringBuilder builder = new StringBuilder();

        for (MDText text : texts) {
            if (text instanceof MDRawText) {
                final MDRawText rawText = (MDRawText) text;
                builder.append(SafeHtmlUtils.htmlEscape(rawText.getRawText()).replace("\n", "<br/>"));
            } else if (text instanceof MDSpan) {
                final MDSpan span = (MDSpan) text;
                builder.append(spanElement(span.getSpanType(), renderText(span.getChild())));
            } else if (text instanceof MDUrl) {
                final MDUrl url = (MDUrl) text;
                builder.append(urlElement(url));
            }
        }

        return builder.toString();
    }

    private static String spanElement(int type, String innerHTML) {
        if (type == MDSpan.TYPE_BOLD) {
            return "<b>" + innerHTML + "</b>";
        } else if (type == MDSpan.TYPE_ITALIC) {
            return "<i>" + innerHTML + "</i>";
        } else {
            return innerHTML;
        }
    }

    private static String urlElement(MDUrl url) {
        String href = UriUtils.sanitizeUri(url.getUrl());

        if (href != "#" && !href.contains("://")) {
            href = "http://" + href;
        }

        return "<a " +
                "target=\"_blank\" " +
                "onClick=\"window.messenger.handleLinkClick(event)\" " +
                "href=\"" + href + "\">" +
                SafeHtmlUtils.htmlEscape(url.getUrlTitle()) +
                "</a>";
    }
}