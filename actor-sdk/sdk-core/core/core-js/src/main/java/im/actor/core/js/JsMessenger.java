/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Event;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.js.entity.JsBlockedUser;
import im.actor.core.js.entity.JsCall;
import im.actor.core.js.entity.JsCounter;
import im.actor.core.js.entity.JsDialogGroup;
import im.actor.core.js.entity.JsEventBusCallback;
import im.actor.core.js.entity.JsOnlineGroup;
import im.actor.core.js.entity.JsOnlineUser;
import im.actor.core.js.entity.JsSearchEntity;
import im.actor.core.js.entity.JsSticker;
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
import im.actor.core.js.modules.JsIdleModule;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.providers.electron.JsElectronListener;
import im.actor.core.js.providers.notification.JsChromePush;
import im.actor.core.js.providers.notification.JsSafariPush;
import im.actor.core.js.providers.notification.PushSubscribeResult;
import im.actor.core.network.RpcCallback;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.Storage;
import im.actor.runtime.crypto.Base64Utils;
import im.actor.runtime.js.JsFileSystemProvider;
import im.actor.runtime.js.fs.JsBlob;
import im.actor.runtime.js.fs.JsFile;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.threading.ThreadDispatcher;

public class JsMessenger extends Messenger {

    private static final String TAG = "JsMessenger";
    private static JsMessenger instance = null;

    public static JsMessenger getInstance() {
        return instance;
    }

    private JsIdleModule jsIdleModule;
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

        ThreadDispatcher.pushDispatcher(Runtime::postToMainThread);

        jsIdleModule = new JsIdleModule(this, modules);

        if (isElectron()) {
            JsElectronApp.subscribe("window", new JsElectronListener() {
                @Override
                public void onEvent(String content) {
                    if ("focus".equals(content)) {
                        jsIdleModule.onVisible();
                    } else if ("blur".equals(content)) {
                        jsIdleModule.onHidden();
                    }
                }
            });
        }

        if (isElectron) {
            getGlobalState().getGlobalTempCounter().subscribe(new ValueChangedListener<Integer>() {
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

        JsMessenger.instance = this;
    }

    public JsIdleModule getJsIdleModule() {
        return jsIdleModule;
    }

    public boolean isElectron() {
        return isElectron;
    }

    public void sendPhoto(final Peer peer, final String fileName, final JsBlob blob) {
        Log.d(TAG, "Resizing photo");
        JsImageResize.resize(blob, new JsResizeListener() {
            @Override
            public void onResized(String thumb, int thumbW, int thumbH, int fullW, int fullH) {

                Log.d(TAG, "Photo resized");

                int index = thumb.indexOf("base64,");
                if (index < 0) {
                    Log.d(TAG, "Unable to find base64");
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

    public void loadArchivedDialogs(boolean init, RpcCallback<ResponseLoadArchived> callback) {
        modules.getMessagesModule().loadMoreArchivedDialogs(init, callback);
    }

    public void loadMoreHistory(Peer peer) {
        modules.getMessagesModule().loadMoreHistory(peer);
    }

    public JsBindedValue<JsUser> getJsUser(int uid) {
        return jsBindingModule.getUser(uid);
    }

    public JsBindedValue<JsOnlineUser> getJsUserOnline(int gid) {
        return jsBindingModule.getUserOnline(gid);
    }

    public JsBindedValue<JsBlockedUser> getJsUserBlocked(int uid) {
        return jsBindingModule.getUserBlocked(uid);
    }

    public JsBindedValue<JsGroup> getJsGroup(int gid) {
        return jsBindingModule.getGroup(gid);
    }

    public JsBindedValue<JsOnlineGroup> getJsGroupOnline(int gid) {
        return jsBindingModule.getGroupOnline(gid);
    }

    public JsBindedValue<JsCall> getJsCall(String id) {
        return jsBindingModule.getCall(id);
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
                    userVM.getNick().get(),
                    getSmallAvatarUrl(userVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()),
                    userVM.getIsVerified().get());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = getGroups().get(peer.getPeerId());
            return JsPeerInfo.create(
                    JsPeer.create(peer),
                    groupVM.getName().get(),
                    null,
                    getSmallAvatarUrl(groupVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()),
                    false);
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

    public JsDisplayList<JsSearchEntity, SearchEntity> getSharedSearchList() {
        return jsBindingModule.getSharedSearchList();
    }

    public JsDisplayList<JsMessage, Message> getSharedChatList(Peer peer) {
        return jsBindingModule.getSharedMessageList(peer);
    }

    public JsBindedValue<JsArray<JsDialogGroup>> getDialogsGroupedList() {
        return jsBindingModule.getDialogsGroupedList();
    }

    public JsBindedValue<JsArray<JsSticker>> getStickers() {
        return jsBindingModule.getStickers();
    }

    public void broadcastEvent(String tag, JavaScriptObject obj) {
        jsBindingModule.broadcastEvent(tag, obj);
    }

    public void subscribeEventBus(JsEventBusCallback callback) {
        jsBindingModule.subscribeEventBus(callback);
    }

    public void unsubscribeEventBus(JsEventBusCallback callback) {
        jsBindingModule.unsubscribeEventBus(callback);
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