package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.IOException;

import im.actor.core.entity.Sticker;
import im.actor.core.js.JsMessenger;
import im.actor.runtime.crypto.Base64Utils;

public class JsSticker extends JavaScriptObject {

    public static JsSticker create(Sticker sticker) {
        String content = Base64Utils.toBase64(sticker.toByteArray());
        String url = JsMessenger.getInstance().getFileUrl(sticker.getImage256());
        return create(sticker.getId(), url, content);
    }

    public static native JsSticker create(int id, String url, String content)/*-{
        return { id: id, url: url, content: content };
    }-*/;

    protected JsSticker() {

    }

    public final native int getId()/*-{
        return this.id;
    }-*/;

    public final native String getUrl()/*-{
        return this.url;
    }-*/;

    public final Sticker getSticker() {
        try {
            return new Sticker(Base64Utils.fromBase64(getContent()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected final native String getContent()/*-{
        return this.content;
    }-*/;
}
