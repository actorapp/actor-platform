package im.actor.runtime.markdown;

import java.util.ArrayList;

public class MarkdownProcessor {

    public static final String CODE_BLOCK = "```";

    public static MDDocument processDocument(String text) {
        TextCursor cursor = new TextCursor(text);
        ArrayList<MDSection> sections = new ArrayList<MDSection>();
        while (handleCodeBlock(cursor, sections)) ;
        return new MDDocument(sections.toArray(new MDSection[sections.size()]));
    }

    /**
     * Outer parsing method: Processing code blocks first
     *
     * @param cursor     text cursor
     * @param paragraphs current paragraphs
     * @return is code block found
     */
    private static boolean handleCodeBlock(TextCursor cursor, ArrayList<MDSection> paragraphs) {
        int blockStart = findCodeBlockStart(cursor);
        if (blockStart >= 0) {
            int blockEnd = findCodeBlockEnd(cursor, blockStart);
            if (blockEnd >= 0) {
                // Adding Text Block if there are some elements before code block
                if (cursor.currentOffset < blockStart) {
                    handleTextBlock(cursor, blockStart, paragraphs);
                }

                String codeContent = cursor.text.substring(cursor.currentOffset + 3, blockEnd - 3);
                cursor.currentOffset = blockEnd;
                paragraphs.add(new MDSection(new MDCode(codeContent)));
                return true;
            }
        }

        // Adding remaining text blocks
        if (cursor.currentOffset < cursor.text.length()) {
            handleTextBlock(cursor, cursor.text.length(), paragraphs);
        }

        return false;
    }

    /**
     * Processing text blocks between code blocks
     *
     * @param cursor     text cursor
     * @param blockEnd   text block end
     * @param paragraphs current paragraphs
     */
    private static void handleTextBlock(TextCursor cursor, int blockEnd, ArrayList<MDSection> paragraphs) {
        MDText[] spans = handleSpans(cursor, blockEnd);
        paragraphs.add(new MDSection(spans));
        cursor.currentOffset = blockEnd;
    }

    /**
     * Processing formatting spans
     *
     * @param cursor   text cursor
     * @param blockEnd code span search limit
     * @return builded tree
     */
    private static MDText[] handleSpans(TextCursor cursor, int blockEnd) {
        ArrayList<MDText> elements = new ArrayList<MDText>();
        while (handleSpan(cursor, blockEnd, elements)) ;
        return elements.toArray(new MDText[elements.size()]);
    }

    /**
     * Handling span
     *
     * @param cursor
     * @param blockEnd
     * @param elements
     * @return
     */
    private static boolean handleSpan(TextCursor cursor, int blockEnd, ArrayList<MDText> elements) {
        int spanStart = findSpanStart(cursor, blockEnd);
        if (spanStart >= 0) {
            char span = cursor.text.charAt(spanStart);
            int spanEnd = findSpanEnd(cursor, spanStart, blockEnd, span);
            if (spanEnd >= 0) {
                if (cursor.currentOffset < spanStart) {
                    elements.add(new MDRawText(cursor.text.substring(cursor.currentOffset, spanStart)));
                }

                cursor.currentOffset = spanStart + 1;
                MDText[] spanElements = handleSpans(cursor, spanEnd - 1);
                cursor.currentOffset = spanEnd;

                MDSpan spanElement = new MDSpan(
                        span == '*' ? MDSpan.TYPE_BOLD : MDSpan.TYPE_ITALIC,
                        spanElements);

                elements.add(spanElement);
            }
        }

        if (cursor.currentOffset < blockEnd) {
            elements.add(new MDRawText(cursor.text.substring(cursor.currentOffset, blockEnd)));
        }

        return false;
    }

    // Searching method


    private static int findCodeBlockStart(TextCursor cursor) {
        int offset = cursor.currentOffset;
        int index;
        while ((index = cursor.text.indexOf(CODE_BLOCK, offset)) >= 0) {
            if (isGoodAnchor(cursor.text, index - 1)) {
                return index;
            }
            offset = index + 3;
        }
        return -1;
    }

    private static int findCodeBlockEnd(TextCursor cursor, int blockStart) {
        int offset = blockStart + 3;
        int index;
        while ((index = cursor.text.indexOf(CODE_BLOCK, offset)) >= 0) {
            if (isGoodAnchor(cursor.text, index + 3)) {
                return index + 3;
            }
            offset = index + 1;
        }
        return -1;
    }

    private static int findSpanStart(TextCursor cursor, int limit) {
        for (int i = cursor.currentOffset; i < limit; i++) {
            char c = cursor.text.charAt(i);
            if (c == '*' || c == '_') {
                // Check prev and next symbols
                if (isGoodAnchor(cursor.text, i - 1) && isNotSymbol(cursor.text, i + 1, c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findSpanEnd(TextCursor cursor, int spanStart, int limit, char span) {
        for (int i = spanStart + 1; i < limit; i++) {
            char c = cursor.text.charAt(i);
            if (c == span) {
                // Check prev and next symbols
                if (isGoodAnchor(cursor.text, i + 1) && isNotSymbol(cursor.text, i - 1, span)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    /**
     * Test if symbol at index is space or out of string bounds
     *
     * @param text  text
     * @param index char to test
     * @return is good anchor
     */
    private static boolean isGoodAnchor(String text, int index) {
        // Check if there is space after block
        if (index >= 0 && index < text.length()) {
            char postfix = text.charAt(index);
            if (postfix != ' ' && postfix != '\t' && postfix != '\n') {
                return false;
            }
        }

        return true;
    }

    /**
     * Checking if symbol is not eq to c
     *
     * @param text
     * @param index
     * @param c
     * @return
     */
    private static boolean isNotSymbol(String text, int index, char c) {
        if (index >= 0 && index < text.length()) {
            return text.charAt(index) != c;
        }

        return true;
    }

    private static String debugElements(MarkdownElement[] elements) {
        String res = "";
        for (MarkdownElement e : elements) {
            if (e.getText() != null) {
                if (e.getType() == MarkdownElement.TYPE_BOLD) {
                    res += "[bold]" + debugElements(e.getChild()) + "[/bold]";
                } else if (e.getType() == MarkdownElement.TYPE_ITALIC) {
                    res += "[italic]" + debugElements(e.getChild()) + "[/italic]";
                } else {
                    res += "[text]" + e.getText() + "[/text]";
                }
            }
        }
        return res;
    }


    private static class TextCursor {

        private String text;
        private int currentOffset;

        public TextCursor(String text) {
            this.text = text;
        }
    }
}