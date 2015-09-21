package im.actor.config;

import java.io.StringWriter;

/**
*
* @author Elad Tabak
* @since 28-Nov-2011
* @version 0.1
*
*/
public class JSonWriter extends StringWriter {

    private int indent = 0;

    @Override
    public void write(int c) {
	if (((char)c) == '[' || ((char)c) == '{') {
	    super.write(c);
	    super.write('\n');
	    indent++;
	    writeIndentation();
	} else if (((char)c) == ',') {
	    super.write(c);
	    super.write('\n');
	    writeIndentation();
	} else if (((char)c) == ']' || ((char)c) == '}') {
	    super.write('\n');
	    indent--;
	    writeIndentation();
	    super.write(c);
	} else {
	    super.write(c);
	}

    }

    private void writeIndentation() {
	for (int i = 0; i < indent; i++) {
	    super.write("   ");
	}
    }
}
