package im.actor.model.js.entity;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class JsContentPhoto extends JsContent {
    public native static JsContentPhoto create(String text, int w, int h, String preview, String fileId)/*-{
        return {content: "photo", w: w, h: h, preview: preview, fileId: fileId};
    }-*/;

    protected JsContentPhoto() {

    }
}
