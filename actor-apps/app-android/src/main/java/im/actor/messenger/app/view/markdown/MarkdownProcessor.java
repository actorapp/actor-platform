package im.actor.messenger.app.view.markdown;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import im.actor.runtime.markdown.MarkdownDocument;
import in.uncod.android.bypass.Bypass;
import in.uncod.android.bypass.Element;

public class MarkdownProcessor {

    private Bypass bypass;

    public MarkdownProcessor() {
        bypass = new Bypass();
    }

    public Spannable processText(String markdown) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

//        Document document = bypass.processMarkdown(markdown);
//
//        ArrayList<QueueItem> elementQueue = new ArrayList<QueueItem>();
//        for (int i = 0; i < document.getElementCount(); i++) {
//            elementQueue.add(i, new QueueItem(document.getElement(i)));
//        }
//
//        while (!elementQueue.isEmpty()) {
//            QueueItem item = elementQueue.get(0);
//            Element element = item.element;
//            if (!item.isExpanded) {
//                builder.append(element.toString());
//                item.isExpanded = true;
//                for (int i = 0; i < element.size(); i++) {
//                    elementQueue.add(i, new QueueItem(element.getChildren(i)));
//                }
//                continue;
//            }
//            elementQueue.remove(0);
//
//
//            // Process Element
//            builder.append(element.getText() + "[/" + element.getType() + "]");
//        }

        MarkdownDocument doc = im.actor.runtime.markdown.MarkdownProcessor.processDocument(markdown);

        return new SpannableStringBuilder(doc.getRootElement().getText());
    }

    private class QueueItem {
        private Element element;
        private boolean isExpanded;

        public QueueItem(Element element) {
            this.element = element;
            this.isExpanded = false;
        }

        public void markExpanded() {
            isExpanded = true;
        }
    }
}