/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.js.angular.AngularFilesModule;
import im.actor.core.js.angular.AngularList;
import im.actor.core.js.angular.AngularModule;
import im.actor.core.js.angular.AngularValue;
import im.actor.core.js.entity.JsContact;
import im.actor.core.js.entity.JsDialog;
import im.actor.core.js.entity.JsGroup;
import im.actor.core.js.entity.JsMessage;
import im.actor.core.js.entity.JsPeer;
import im.actor.core.js.entity.JsPeerInfo;
import im.actor.core.js.entity.JsTyping;
import im.actor.core.js.entity.JsUser;
import im.actor.core.js.entity.Placeholders;
import im.actor.core.js.images.JsImageResize;
import im.actor.core.js.images.JsResizeListener;
import im.actor.core.js.providers.JsFileSystemProvider;
import im.actor.core.js.providers.fs.JsBlob;
import im.actor.core.js.providers.fs.JsFile;
import im.actor.core.js.providers.notification.JsChromePush;
import im.actor.core.js.providers.notification.JsSafariPush;
import im.actor.core.js.providers.notification.PushSubscribeResult;
import im.actor.core.log.Log;
import im.actor.core.util.Base64Utils;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;

public class JsMessenger extends Messenger {

    private AngularModule angularModule;
    private AngularFilesModule angularFilesModule;
    private JsFileSystemProvider fileSystemProvider;

    public JsMessenger(Configuration configuration) {
        super(configuration);
        fileSystemProvider = (JsFileSystemProvider) configuration.getFileSystemProvider();
        angularFilesModule = new AngularFilesModule(modules);
        angularModule = new AngularModule(this, angularFilesModule, modules);

        if (JsChromePush.isSupported()) {
            Log.d("JsMessenger", "ChromePush Supported");
            JsChromePush.subscribe(new PushSubscribeResult() {

                @Override
                public void onSubscribedChrome(String token) {
                    Log.d("JsMessenger", "Subscribed: " + token);
                    registerGooglePush(209133700967L, token);
                }

                @Override
                public void onSubscriptionFailure() {
                    Log.d("JsMessenger", "Subscribe failure");
                }
            });
        } else {
            Log.d("JsMessenger", "ChromePush NOT Supported");
        }
        if (JsSafariPush.isSupported()) {
            Log.d("JsMessenger", "SafariPush Supported");
        } else {
            Log.d("JsMessenger", "SafariPush NOT Supported");
        }
    }

    public void onMessageShown(Peer peer, Long sortKey) {
        modules.getMessagesModule().onMessageShown(peer, sortKey);
    }

    public void sendPhoto(final Peer peer, final String fileName, final JsBlob blob) {
        JsImageResize.resize(blob, new JsResizeListener() {
            @Override
            public void onResized(String thumb, int thumbW, int thumbH, int fullW, int fullH) {
                int index = thumb.indexOf("base64,");
                if (index < 0) {
                    return;
                }
                String rawData = thumb.substring(index + "base64,".length());

                byte[] thumbData = Base64Utils.fromBase64(rawData);

                String descriptor = fileSystemProvider.registerUploadFile(blob);
                sendPhoto(peer, fileName, fullW, fullH,
                        new FastThumb(thumbW, thumbH, thumbData),
                        descriptor);
            }
        });
    }

    public void sendPhoto(final Peer peer, final JsFile file) {
        sendPhoto(peer, file.getName(), file);
    }

    public void sendClipboardPhoto(final Peer peer, final JsBlob file) {
        sendPhoto(peer, "clipboard.jpg", file);
    }

    public void loadMoreDialogs() {
        modules.getMessagesModule().loadMoreDialogs();
    }

    public void loadMoreHistory(Peer peer) {
        modules.getMessagesModule().loadMoreHistory(peer);
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        return angularModule.getDialogsList();
    }

    public AngularList<JsMessage, Message> getConversationList(Peer peer) {
        return angularModule.getMessagesList(peer);
    }

    public AngularList<JsContact, Contact> getContactsList() {
        return angularModule.getContactsList();
    }

    public AngularValue<JsUser> getJsUser(int uid) {
        return angularModule.getUser(uid);
    }

    public AngularValue<JsGroup> getJsGroup(int gid) {
        return angularModule.getGroup(gid);
    }

    public AngularValue<JsTyping> getTyping(Peer peer) {
        return angularModule.getTyping(peer);
    }

    public AngularValue<String> getOnlineStatus() {
        return angularModule.getOnlineStatus();
    }

    public JsPeerInfo buildPeerInfo(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            UserVM userVM = getUsers().get(peer.getPeerId());
            return JsPeerInfo.create(
                    JsPeer.create(peer),
                    userVM.getName().get(),
                    getSmallAvatarUrl(userVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = getGroups().get(peer.getPeerId());
            return JsPeerInfo.create(
                    JsPeer.create(peer),
                    groupVM.getName().get(),
                    getSmallAvatarUrl(groupVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()));
        } else {
            throw new RuntimeException();
        }
    }

    private String getSmallAvatarUrl(Avatar avatar) {
        if (avatar != null && avatar.getSmallImage() != null) {
            return getFileUrl(avatar.getSmallImage().getFileReference());
        }
        return null;
    }

    public String getFileUrl(FileReference fileReference) {
        return angularFilesModule.getFileUrl(fileReference.getFileId(), fileReference.getAccessHash());
    }
}