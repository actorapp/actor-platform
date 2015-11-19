package im.actor.core.js.entity;

import com.google.gwt.core.client.JsArrayString;

public class JsContentContact extends JsContent {

    public native static JsContentContact create(String name, String photo64, JsArrayString phones,
                                                 JsArrayString emails)/*-{
        return {content: "contact", name: name, photo64: photo64, pones: phones, emails: emails};
    }-*/;

    protected JsContentContact() {

    }
}
