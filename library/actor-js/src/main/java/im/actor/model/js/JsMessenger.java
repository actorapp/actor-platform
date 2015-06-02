/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js;

import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.js.angular.AngularFilesModule;
import im.actor.model.js.angular.AngularList;
import im.actor.model.js.angular.AngularModule;
import im.actor.model.js.angular.AngularValue;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsGroup;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsPeer;
import im.actor.model.js.entity.JsPeerInfo;
import im.actor.model.js.entity.JsTyping;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.entity.Placeholders;
import im.actor.model.js.images.JsImageResize;
import im.actor.model.js.images.JsResizeListener;
import im.actor.model.js.providers.JsFileSystemProvider;
import im.actor.model.js.providers.fs.JsBlob;
import im.actor.model.js.providers.fs.JsFile;
import im.actor.model.js.providers.notification.JsChromePush;
import im.actor.model.js.providers.notification.PushSubscribeResult;
import im.actor.model.js.replacer.Replacer;
import im.actor.model.log.Log;
import im.actor.model.util.Base64Utils;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

public class JsMessenger extends Messenger {

    private AngularModule angularModule;
    private AngularFilesModule angularFilesModule;
    private Replacer replacer;
    private JsFileSystemProvider fileSystemProvider;

    public JsMessenger(Configuration configuration) {
        super(configuration);
        fileSystemProvider = (JsFileSystemProvider) configuration.getFileSystemProvider();
        replacer = new Replacer(this);
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
    }

    public void onMessageShown(Peer peer, Long sortKey) {
        modules.getMessagesModule().onInMessageShown(peer, sortKey);
    }

    public void sendNoHack(Peer peer, String text) {
        super.sendMessage(peer, text);
    }

    @Override
    public void sendMessage(Peer peer, String text) {
        if (replacer.canHack(peer, text)) {
            return;
        }
        super.sendMessage(peer, text);
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

    public AngularValue<JsUser> getJsUser(int uid) {
        return angularModule.getUser(uid);
    }

    public AngularValue<JsGroup> getJsGroup(int gid) {
        return angularModule.getGroup(gid);
    }

    public AngularValue<JsTyping> getTyping(Peer peer) {
        return angularModule.getTyping(peer);
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