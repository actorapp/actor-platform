package im.actor.runtime.markdown;

import im.actor.runtime.Log;
import im.actor.runtime.regexp.MatcherCompat;

import java.util.ArrayList;

public class MarkdownParser {

    public static final int MODE_FULL = 0;
    public static final int MODE_LITE = 1;
    public static final int MODE_ONLY_LINKS = 2;

    private static final String CODE_BLOCK = "```";

    private final int mode;

    public MarkdownParser(int mode) {
        this.mode = mode;
    }

    /**
     * Parsing markdown document
     *
     * @param text markdown text
     * @return parsed document
     */
    public MDDocument processDocument(String text) {
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
    private boolean handleCodeBlock(TextCursor cursor, ArrayList<MDSection> paragraphs) {
        if (mode != MODE_ONLY_LINKS) {
            int blockStart = findCodeBlockStart(cursor);
            if (blockStart >= 0) {
                int blockEnd = findCodeBlockEnd(cursor, blockStart);
                if (blockEnd >= 0) {
                    // Adding Text Block if there are some elements before code block
                    if (cursor.currentOffset < blockStart) {
                        handleTextBlock(cursor, blockStart, paragraphs);
                    }

                    String codeContent = cursor.text.substring(cursor.currentOffset + 3, blockEnd - 3).trim();

                    // TODO: Better removing of empty leading and tailing lines
                    // Required to remove only ONE line
                    if (codeContent.startsWith("\n")) {
                        codeContent = codeContent.substring(1);
                    }
                    if (codeContent.endsWith("\n")) {
                        codeContent = codeContent.substring(0, codeContent.length() - 1);
                    }

                    cursor.currentOffset = blockEnd;
                    paragraphs.add(new MDSection(new MDCode(codeContent)));
                    return true;
                }
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
    private void handleTextBlock(TextCursor cursor, int blockEnd, ArrayList<MDSection> paragraphs) {
        MDText[] spans = handleSpans(cursor, blockEnd);
        paragraphs.add(new MDSection(spans));
        cursor.currentOffset = blockEnd;
    }

    /**
     * Processing formatting spans
     *
     * @param cursor   text cursor
     * @param blockEnd code span search limit
     * @return built text segments
     */
    private MDText[] handleSpans(TextCursor cursor, int blockEnd) {
        ArrayList<MDText> elements = new ArrayList<MDText>();
        while (handleSpan(cursor, blockEnd, elements)) ;
        return elements.toArray(new MDText[elements.size()]);
    }

    /**
     * Handling span
     *
     * @param cursor   text cursor
     * @param blockEnd span search limit
     * @param elements current elements
     * @return is
     */
    private boolean handleSpan(TextCursor cursor, int blockEnd, ArrayList<MDText> elements) {
        if (mode != MODE_ONLY_LINKS) {
            int spanStart = findSpanStart(cursor, blockEnd);
            if (spanStart >= 0) {
                char span = cursor.text.charAt(spanStart);
                int spanEnd = findSpanEnd(cursor, spanStart, blockEnd, span);
                if (spanEnd >= 0) {

                    // Handling next elements before span
                    handleUrls(cursor, spanStart, elements);

                    // Increment offset before processing internal spans
                    cursor.currentOffset++;

                    // Building child spans
                    MDText[] spanElements = handleSpans(cursor, spanEnd - 1);

                    // End of search: move cursor after span
                    cursor.currentOffset = spanEnd;

                    MDSpan spanElement = new MDSpan(
                            span == '*' ? MDSpan.TYPE_BOLD : MDSpan.TYPE_ITALIC,
                            spanElements);

                    elements.add(spanElement);

                    return true;
                }
            }
        }

        handleUrls(cursor, blockEnd, elements);

        return false;
    }

    /**
     * Handling urls
     *
     * @param cursor
     * @param limit
     * @param elements
     */
    private void handleUrls(TextCursor cursor, int limit, ArrayList<MDText> elements) {

        if (mode == MODE_FULL || mode == MODE_ONLY_LINKS) {
            while (true) {
                TitledUrl url = findFormattedUrl(cursor, limit);
                if (url != null) {

                    // Handling text before url first
                    handleRawText(cursor, url.getStart(), elements);

                    // Adding url
                    String title = cursor.text.substring(url.getStart() + 1, url.getMiddle());
                    String urlVal = cursor.text.substring(url.getMiddle() + 2, url.getEnd());
                    elements.add(new MDUrl(title, urlVal));

                    // Adjusting offset
                    cursor.currentOffset = url.getEnd() + 1;
                } else {
                    break;
                }
            }
        }

        // Handling remaining text
        handleRawText(cursor, limit, elements);
    }

    /**
     * Handling raw text block
     *
     * @param cursor   text cursor
     * @param limit    text end
     * @param elements current elements
     */
    private void handleRawText(TextCursor cursor, int limit, ArrayList<MDText> elements) {
        while (true) {
            BasicUrl url = findUrl(cursor, limit);
            if (url != null) {
                String link = cursor.text.substring(url.getStart(), url.getEnd());

                // Handling text before url first
                addText(cursor, url.getStart(), elements);

                // Adding url
                elements.add(new MDUrl(link, link));

                // Adjusting offset
                cursor.currentOffset = url.getEnd();

                continue;
            }

            addText(cursor, limit, elements);

            return;
        }
    }

    /**
     * Adding raw simple text
     *
     * @param cursor   text cursor
     * @param limit    text end
     * @param elements current elements
     */
    private void addText(TextCursor cursor, int limit, ArrayList<MDText> elements) {
        if (cursor.currentOffset < limit) {
            elements.add(new MDRawText(cursor.text.substring(cursor.currentOffset, limit)));
            cursor.currentOffset = limit;
        }
    }

    /**
     * Searching for valid code block begin
     *
     * @param cursor text cursor
     * @return code block start, -1 if not found
     */
    private int findCodeBlockStart(TextCursor cursor) {
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

    /**
     * Searching for valid code block end
     *
     * @param cursor     text cursor
     * @param blockStart start of expected code block
     * @return code block end, -1 if not found
     */
    private int findCodeBlockEnd(TextCursor cursor, int blockStart) {
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

    /**
     * Searching for valid formatting span start
     *
     * @param cursor text cursor
     * @param limit  maximum index in cursor
     * @return span start, -1 if not found
     */
    private int findSpanStart(TextCursor cursor, int limit) {
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

    /**
     * Searching for valid formatting span end
     *
     * @param cursor    text cursor
     * @param spanStart expected span start
     * @param limit     maximum index in cursor
     * @param span      span control character
     * @return span end, -1 if not found
     */
    private int findSpanEnd(TextCursor cursor, int spanStart, int limit, char span) {
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
     * Searching for valid formatted url
     *
     * @param cursor current cursor
     * @param limit  search limit
     * @return found url, null if not found
     */
    private TitledUrl findFormattedUrl(TextCursor cursor, int limit) {
        start_loop:
        for (int start = cursor.currentOffset; start < limit; start++) {

            // Finding beginning of url
            if (cursor.text.charAt(start) == '[') {
                if (!isGoodAnchor(cursor.text, start - 1)) {
                    continue start_loop;
                }
            } else {
                continue start_loop;
            }

            // Finding middle part of url
            middle_loop:
            for (int middle = start + 1; middle < limit - 1; middle++) {
                if (cursor.text.charAt(middle) != ']' || cursor.text.charAt(middle + 1) != '(') {
                    continue middle_loop;
                }

                end_loop:
                for (int end = middle + 2; end < limit; end++) {
                    if (cursor.text.charAt(end) != ')') {
                        continue end_loop;
                    }
                    return new TitledUrl(start, middle, end);
                }
            }
        }

        return null;
    }

    /**
     * Finding non-formatted urls in texts
     *
     * @param cursor current text cursor
     * @param limit  end of cursor
     * @return founded url
     */
    private BasicUrl findUrl(TextCursor cursor, int limit) {
        for (int i = cursor.currentOffset; i < limit; i++) {
            if (!isGoodAnchor(cursor.text, i - 1)) {
                continue;
            }
            String currentText = cursor.text.substring(i, limit);
            MatcherCompat matcher = Patterns.WEB_URL_START.matcher(currentText);
            if (matcher.hasMatch()) {
                String url = matcher.group();
                int start = i + matcher.start();
                return new BasicUrl(start, start + url.length());
            }
        }
        return null;
    }

    /**
     * Test if symbol at index is space or out of string bounds
     *
     * @param text  text
     * @param index char to test
     * @return is good anchor
     */
    private boolean isGoodAnchor(String text, int index) {
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
    private boolean isNotSymbol(String text, int index, char c) {
        if (index >= 0 && index < text.length()) {
            return text.charAt(index) != c;
        }

        return true;
    }

    private static abstract class Url {
        public abstract int getStart();

        public abstract int getEnd();
    }

    private static class BasicUrl extends Url {
        private int start;
        private int end;

        public BasicUrl(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int getStart() {
            return start;
        }

        @Override
        public int getEnd() {
            return end;
        }
    }

    private static class TitledUrl extends Url {

        private int start;
        private int middle;
        private int end;

        public TitledUrl(int start, int middle, int end) {
            this.start = start;
            this.middle = middle;
            this.end = end;
        }

        @Override
        public int getStart() {
            return start;
        }

        public int getMiddle() {
            return middle;
        }

        @Override
        public int getEnd() {
            return end;
        }
    }

    private static class TextCursor {

        private String text;
        private int currentOffset;

        public TextCursor(String text) {
            this.text = text;
        }
    }
}