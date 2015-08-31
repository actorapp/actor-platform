package im.actor.runtime.markdown;

import junit.framework.TestCase;

public class MarkdownProcessorTest extends TestCase {

    public void testProcessBold() throws Exception {
        String message = "*bold* text";
        MarkdownDocument doc = MarkdownProcessor.processDocument(message);
    }
}