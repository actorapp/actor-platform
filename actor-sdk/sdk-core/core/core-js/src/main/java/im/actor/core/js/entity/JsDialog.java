/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.GroupType;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.js.JsMessenger;
import im.actor.runtime.js.mvvm.JsEntityConverter;

public class JsDialog extends JavaScriptObject {

    public static final JsEntityConverter<Dialog, JsDialog> CONVERTER = new JsEntityConverter<Dialog, JsDialog>() {

        @Override
        public JsDialog convert(Dialog src) {

            JsMessenger messenger = JsMessenger.getInstance();

            boolean showSender = false;
            Peer peer = src.getPeer();
            if (peer.getPeerType() == PeerType.GROUP) {
                if (src.getMessageType() != ContentType.SERVICE && src.getMessageType() != ContentType.NONE) {
                    showSender = src.getSenderId() != 0;
                }
            }

            String senderName = null;
            if (showSender) {
                senderName = messenger.getUsers().get(src.getSenderId()).getName().get();
            }

            String date = messenger.getFormatter().formatShortDate(src.getDate());

            String fileUrl = null;
            if (src.getDialogAvatar() != null && src.getDialogAvatar().getSmallImage() != null) {
                fileUrl = messenger.getFileUrl(src.getDialogAvatar().getSmallImage().getFileReference());
            }

            boolean isChannel = peer.getPeerType() == PeerType.GROUP && messenger.getGroups().get(peer.getPeerId()).getGroupType() == GroupType.CHANNEL;

            boolean highlightContent = src.getMessageType() != ContentType.TEXT;
            String messageText = messenger.getFormatter().formatContentText(src.getSenderId(),
                    src.getMessageType(), src.getText(), src.getRelatedUid(), false);

            JsPeerInfo peerInfo = JsPeerInfo.create(JsPeer.create(peer), src.getDialogTitle(), null, fileUrl,
                    Placeholders.getPlaceholder(peer.getPeerId()), isChannel);

            String state = "unknown";
            if (messenger.myUid() == src.getSenderId()) {
                if (src.isRead()) {
                    state = "read";
                } else if (src.isReceived()) {
                    state = "received";
                } else {
                    state = "sent";
                }
            }

            return JsDialog.create(peerInfo, date, senderName, showSender, messageText,
                    highlightContent, state, src.getUnreadCount());
        }

        @Override
        public boolean isSupportOverlays() {
            return false;
        }

        @Override
        public JavaScriptObject buildOverlay(Dialog prev, Dialog current, Dialog next) {
            return null;
        }
    };

    public static native JsDialog create(JsPeerInfo peer,
                                         String date,
                                         String sender, boolean showSender,
                                         String text, boolean isHighlighted,
                                         String state, int counter)/*-{
        return {peer: peer, text: text, date: date, sender: sender, showSender: showSender,
        isHighlighted: isHighlighted, state:state, counter:counter };
    }-*/;

    protected JsDialog() {
    }
}
