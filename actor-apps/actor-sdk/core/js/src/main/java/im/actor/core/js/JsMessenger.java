/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import com.google.gwt.user.client.Event;
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
import im.actor.core.js.entity.JsCounter;
import im.actor.core.js.modules.JsFilesModule;
import im.actor.core.js.modules.JsBindingModule;
import im.actor.core.js.modules.JsBindedValue;
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
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.providers.notification.JsChromePush;
import im.actor.core.js.providers.notification.JsSafariPush;
import im.actor.core.js.providers.notification.PushSubscribeResult;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.crypto.Base64Utils;
import im.actor.runtime.js.JsFileSystemProvider;
import im.actor.runtime.js.fs.JsBlob;
import im.actor.runtime.js.fs.JsFile;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;

public class JsMessenger extends Messenger {

    private static JsMessenger instance = null;

    public static JsMessenger getInstance() {
        return instance;
    }

    private JsBindingModule jsBindingModule;
    private JsFilesModule filesModule;
    private JsFileSystemProvider fileSystemProvider;
    private boolean isElectron;

    public JsMessenger(Configuration configuration) {
        super(configuration);
        fileSystemProvider = (JsFileSystemProvider) Storage.getFileSystemRuntime();
        filesModule = new JsFilesModule(modules);
        jsBindingModule = new JsBindingModule(this, filesModule, modules);
        isElectron = JsElectronApp.isElectron();

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

        JsMessenger.instance = this;

        if (isElectron) {
            getAppState().getGlobalTempCounter().subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, Value<Integer> valueModel) {
                    if (val == null || val == 0) {
                        JsElectronApp.hideNewMessages();
                    } else {
                        JsElectronApp.updateBadge(val);
                    }
                }
            });
        }
    }

    public boolean isElectron() {
        return isElectron;
    }

    public void onMessageShown(Peer peer, Long sortKey) {
        // TODO: Implement uid
        modules.getMessagesModule().onMessageShown(peer, 0, sortKey);
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

    public JsBindedValue<JsUser> getJsUser(int uid) {
        return jsBindingModule.getUser(uid);
    }

    public JsBindedValue<JsGroup> getJsGroup(int gid) {
        return jsBindingModule.getGroup(gid);
    }

    public JsBindedValue<JsTyping> getTyping(Peer peer) {
        return jsBindingModule.getTyping(peer);
    }

    public JsBindedValue<String> getOnlineStatus() {
        return jsBindingModule.getOnlineStatus();
    }

    public JsBindedValue<JsCounter> getGlobalCounter() {
        return jsBindingModule.getGlobalCounter();
    }

    public JsBindedValue<JsCounter> getTempGlobalCounter() {
        return jsBindingModule.getTempGlobalCounter();
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

    public JsDisplayList<JsDialog, Dialog> getSharedDialogList() {
        return jsBindingModule.getSharedDialogList();
    }

    public JsDisplayList<JsContact, Contact> getSharedContactList() {
        return jsBindingModule.getSharedContactList();
    }

    public JsDisplayList<JsMessage, Message> getSharedChatList(Peer peer) {
        return jsBindingModule.getSharedMessageList(peer);
    }

    private String getSmallAvatarUrl(Avatar avatar) {
        if (avatar != null && avatar.getSmallImage() != null) {
            return getFileUrl(avatar.getSmallImage().getFileReference());
        }
        return null;
    }

    public String getFileUrl(FileReference fileReference) {
        return filesModule.getFileUrl(fileReference.getFileId(), fileReference.getAccessHash());
    }
}