package im.actor.runtime.markdown;

import junit.framework.TestCase;

public class MarkdownParserTest extends TestCase {
    final MarkdownParser parser = new MarkdownParser(MarkdownParser.MODE_FULL);

    public void testProcessBold() throws Exception {
        String message = "*bold* text";
        MDDocument doc = parser.processDocument(message);
    }

    public void testLinks() throws Exception {
        String text = "http://actor.im";
        MDDocument doc = parser.processDocument(text);
        assertEquals(doc.getSections().length, 1);

        MDSection section = doc.getSections()[0];
        assertEquals(section.getType(), MDSection.TYPE_TEXT);
    }
}