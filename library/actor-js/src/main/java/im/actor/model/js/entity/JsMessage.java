/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.js.JsMessenger;
import im.actor.model.util.Base64Utils;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsMessage extends JavaScriptObject {

    public static final JsEntityConverter<Message, JsMessage> CONVERTER = new JsEntityConverter<Message, JsMessage>() {
        @Override
        public JsMessage convert(Message value, JsMessenger modules) {

            String rid = value.getRid() + "";

            JsPeerInfo sender = modules.buildPeerInfo(Peer.user(value.getSenderId()));
            boolean isOut = value.getSenderId() == modules.myUid();
            String date = modules.getFormatter().formatTime(value.getDate());

            JsContent content;
            if (value.getContent() instanceof TextContent) {
                content = JsContentText.create(((TextContent) value.getContent()).getText());
            } else if (value.getContent() instanceof ServiceContent) {
                content = JsContentService.create(modules.getFormatter().formatFullServiceMessage(value.getSenderId(), (ServiceContent) value.getContent()));
            } else if (value.getContent() instanceof PhotoContent) {
                PhotoContent photo = (PhotoContent) value.getContent();
                String thumb = null;
                if (photo.getFastThumb() != null) {
                    String thumbBase64 = Base64Utils.toBase64(photo.getFastThumb().getImage());
                    thumb = "data:image/jpg;base64," + thumbBase64;
                }
                content = JsContentPhoto.create(photo.getW(), photo.getH(), thumb, null);
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