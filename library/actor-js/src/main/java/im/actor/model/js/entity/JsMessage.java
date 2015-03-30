package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.TextContent;
import im.actor.model.js.JsMessenger;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsMessage extends JavaScriptObject {

    public static final JsEntityConverter<Message, JsMessage> CONVERTER = new JsEntityConverter<Message, JsMessage>() {
        @Override
        public JsMessage convert(Message value, JsMessenger modules) {
            String rid = value.getRid() + "";
            UserVM userVM = modules.getUsers().get(value.getSenderId());
            JsPeerInfo sender = JsPeerInfo.create(JsPeer.create(Peer.user(value.getSenderId())), userVM.getName().get(),
                    null, Placeholders.getPlaceholder(value.getSenderId()));
            boolean isOut = value.getSenderId() == modules.myUid();
            String date = modules.getFormatter().formatTime(value.getDate());

            JsContent content;
            if (value.getContent() instanceof TextContent) {
                content = JsContentText.create(((TextContent) value.getContent()).getText());
            } else {
                content = JsContentUnsupported.create();
            }

            return create(rid, sender, isOut, date, content);
        }
    };

    public native static JsMessage create(String rid, JsPeerInfo sender, boolean isOut, String date, JsContent content)/*-{
        return {rid: rid, sender: sender, isOut: isOut, date: date, content: content};
    }-*/;

    protected JsMessage() {

    }
}