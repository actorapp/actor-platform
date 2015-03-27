package im.actor.model.js.entity;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsContentText extends JsContent {
    public native static JsContentText create(String text)/*-{
        return {content: "text", text: text};
    }-*/;

    protected JsContentText(){

    }
}
