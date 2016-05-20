package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsBotCommand extends JavaScriptObject {

    public native static JsBotCommand create(String command, String description)/*-{
        return {command: command, description: description};
    }-*/;

    protected JsBotCommand() {

    }
}
