package im.actor.runtime.markdown;

import java.util.ArrayList;

public class MarkdownProcessor {

    public static final String CODE_BLOCK = "```";

    public static final Character[] STOP_LETTERS = new Character[]{'*', '_', '`'};

    public static MarkdownDocument processDocument(String text) {

        ArrayList<MDParagraph> paragraphs = new ArrayList<MDParagraph>();
        TextCursor cursor = new TextCursor(text);

        while (processBlock(cursor, paragraphs)) ;

        for (MDParagraph p : paragraphs) {
            processSpans(p);
        }

        String processedText = "";
        for (MDParagraph paragraph : paragraphs) {
            if (processedText.length() > 0) {
                processedText += "\n";
            }
            if (paragraph.getType() == MDParagraph.TYPE_TEXT) {
                processedText += "[P]" + paragraph.getText() + "[/P]";
            } else if (paragraph.getType() == MDParagraph.TYPE_CODE) {
                processedText += "[CODE]\n" + paragraph.getText() + "\n[/CODE]";
            }
        }

        return new MarkdownDocument(new MarkdownElement(processedText));
    }

    private static void processSpans(MDParagraph paragraph) {
        String text = paragraph.getText();

        // Removing leading and tailing newlines
        if (text.startsWith("\n")) {
            text = text.substring(1, text.length());
        }
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        paragraph.setText(text);

        if (paragraph.getType() == MDParagraph.TYPE_TEXT) {

            String processedText = "";

            TextCursor cursor = new TextCursor(text);
            int stopLetter = findStopLetter(cursor);
            if (stopLetter >= 0) {
                processedText += cursor.text.substring(0, stopLetter);
            }
            while (stopLetter >= 0) {

                char c = text.charAt(stopLetter);
                cursor.currentOffset = stopLetter + 1;
                int letterEnd = findStopLetter(cursor, c);

                if (letterEnd >= 0) {
                    processedText += "!" + cursor.text.substring(stopLetter + 1, letterEnd) + "!";
                    cursor.currentOffset = letterEnd + 1;
                } else {
                    processedText += cursor.text.substring(stopLetter);
                    cursor.currentOffset = cursor.text.length();
                }

                stopLetter = findStopLetter(cursor);
            }

            processedText += cursor.text.substring(cursor.currentOffset);

            paragraph.setText(processedText);
        }
    }

    private static boolean isStopLetter(char c) {
        return STOP_LETTERS[0] == c || STOP_LETTERS[1] == c || STOP_LETTERS[2] == c;
    }

    private static int findStopLetter(TextCursor cursor) {
        for (int i = cursor.currentOffset; i < cursor.text.length(); i++) {
            if (isStopLetter(cursor.text.charAt(i))) {
                if (isGoodPrefix(cursor.text, i)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static int findStopLetter(TextCursor cursor, char letter) {
        for (int i = cursor.currentOffset; i < cursor.text.length(); i++) {
            if (cursor.text.charAt(i) == letter) {
                if (isGoodPostfix(cursor.text, i)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static boolean isGoodPrefix(String text, int index) {
        if (index == 0 || text.charAt(index - 1) == ' ' || text.charAt(index - 1) == '\t') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isGoodPostfix(String text, int index) {
        if (index >= text.length() - 1 || text.charAt(index + 1) == ' ' || text.charAt(index + 1) == '\t') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean processBlock(TextCursor cursor, ArrayList<MDParagraph> paragraphs) {

        if (cursor.currentOffset >= cursor.text.length()) {
            return false;
        }

        boolean isCodeBlock = cursor.text.startsWith(CODE_BLOCK, cursor.currentOffset);
        if (isCodeBlock) {
            cursor.currentOffset += CODE_BLOCK.length();
        }

        int blockEnd = cursor.text.indexOf(CODE_BLOCK, cursor.currentOffset);

        if (isCodeBlock) {
            if (blockEnd < 0 || blockEnd == cursor.currentOffset) {
                // Is mailformed code
                // Add ``` as text
                if (blockEnd < 0) {
                    paragraphs.add(new MDParagraph(MDParagraph.TYPE_TEXT, "```" + cursor.text.substring(cursor.currentOffset)));
                    cursor.currentOffset = cursor.text.length();
                    return false;
                } else {
                    paragraphs.add(new MDParagraph(MDParagraph.TYPE_TEXT, "``````"));
                    cursor.currentOffset += 3;
                    return true;
                }
            } else {
                paragraphs.add(new MDParagraph(MDParagraph.TYPE_CODE, cursor.text.substring(cursor.currentOffset,
                        blockEnd)));
                cursor.currentOffset = blockEnd + 3;
                return true;
            }
        } else {
            if (blockEnd < 0) {
                blockEnd = cursor.text.length();
                // Is End Reached
                // return false;
            }
            String paragraph = cursor.text.substring(cursor.currentOffset, blockEnd);
            cursor.currentOffset = blockEnd;
            paragraphs.add(new MDParagraph(MDParagraph.TYPE_TEXT, paragraph));
            return true;
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