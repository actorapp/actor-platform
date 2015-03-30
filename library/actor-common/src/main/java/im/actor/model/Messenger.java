package im.actor.model;

import im.actor.model.concurrency.Command;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.droidkit.actors.debug.TraceInterface;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Group;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.files.FileSystemReference;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.AppStateVM;
import im.actor.model.viewmodel.DownloadCallback;
import im.actor.model.viewmodel.FileVM;
import im.actor.model.viewmodel.FileVMCallback;
import im.actor.model.viewmodel.GroupAvatarVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.OwnAvatarVM;
import im.actor.model.viewmodel.UploadCallback;
import im.actor.model.viewmodel.UploadFileVM;
import im.actor.model.viewmodel.UploadFileVMCallback;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Messenger {
    private static final String TAG = "CORE_INIT";
    protected Modules modules;

    public Messenger(Configuration configuration) {
        // Init Log
        Log.setLog(configuration.getLog());

        long start = configuration.getThreadingProvider().getActorTime();

        // Init internal actor system
        Environment.setThreadingProvider(configuration.getThreadingProvider());
        Environment.setDispatcherProvider(configuration.getDispatcherProvider());

        Log.d(TAG, "Loading stage1 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        // Init Crypto
        CryptoUtils.init(configuration.getCryptoProvider());

        Log.d(TAG, "Loading stage2 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        // Init MVVM
        MVVMEngine.init(configuration.getMainThreadProvider());

        Log.d(TAG, "Loading stage3 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        ActorSystem.system().setTraceInterface(new TraceInterface() {
            @Override
            public void onEnvelopeDelivered(Envelope envelope) {

            }

            @Override
            public void onEnvelopeProcessed(Envelope envelope, long duration) {
                if (duration > 300) {
                    Log.w("ACTOR_SYSTEM", "Too long " + envelope.getScope().getPath() + " {" + envelope.getMessage() + "}");
                }
            }

            @Override
            public void onDrop(ActorRef sender, Object message, Actor actor) {
                Log.w("ACTOR_SYSTEM", "Drop: " + message);
            }

            @Override
            public void onDeadLetter(ActorRef receiver, Object message) {
                Log.w("ACTOR_SYSTEM", "Dead Letter: " + message);
            }

            @Override
            public void onActorDie(ActorRef ref, Exception e) {
                Log.w("ACTOR_SYSTEM", "Die: " + e);
                e.printStackTrace();
            }
        });
        ActorSystem.system().addDispatcher("db", 1);

        Log.d(TAG, "Loading stage4 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        this.modules = new Modules(configuration);

        Log.d(TAG, "Loading stage5 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

        this.modules.run();

        Log.d(TAG, "Loading stage6 in " + (configuration.getThreadingProvider().getActorTime() - start) + " ms");
        start = configuration.getThreadingProvider().getActorTime();

    }

    // Auth

    public AuthState getAuthState() {
        return modules.getAuthModule().getAuthState();
    }

    public boolean isLoggedIn() {
        return getAuthState() == AuthState.LOGGED_IN;
    }

    public Command<AuthState> requestSms(final long phone) {
        return modules.getAuthModule().requestSms(phone);
    }

    public Command<AuthState> sendCode(final int code) {
        return modules.getAuthModule().sendCode(code);
    }

    public Command<AuthState> signUp(final String firstName, String avatarPath, final boolean isSilent) {
        return modules.getAuthModule().signUp(firstName, avatarPath, isSilent);
    }

    public long getAuthPhone() {
        return modules.getAuthModule().getPhone();
    }

    public void resetAuth() {
        modules.getAuthModule().resetAuth();
    }

    public int myUid() {
        return modules.getAuthModule().myUid();
    }

    public I18nEngine getFormatter() {
        return modules.getI18nEngine();
    }

    public MVVMCollection<User, UserVM> getUsers() {
        if (modules.getUsersModule() == null) {
            return null;
        }
        return modules.getUsersModule().getUsersCollection();
    }

    public MVVMCollection<Group, GroupVM> getGroups() {
        if (modules.getGroupsModule() == null) {
            return null;
        }
        return modules.getGroupsModule().getGroupsCollection();
    }

    public ValueModel<Boolean> getTyping(int uid) {
        if (modules.getTypingModule() == null) {
            return null;
        }
        return modules.getTypingModule().getTyping(uid).getTyping();
    }

    public ValueModel<int[]> getGroupTyping(int gid) {
        if (modules.getTypingModule() == null) {
            return null;
        }
        return modules.getTypingModule().getGroupTyping(gid).getActive();
    }

    public void onAppVisible() {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().onAppVisible();
            modules.getNotifications().onAppVisible();
        }
    }

    public void onAppHidden() {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().onAppHidden();
            modules.getNotifications().onAppHidden();
        }
    }

    public void onConversationOpen(Peer peer) {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().subscribe(peer);
            modules.getNotifications().onConversationOpen(peer);
            modules.getMessagesModule().onConversationOpen(peer);
        }
    }

    public void onConversationClosed(Peer peer) {
        if (modules.getPresenceModule() != null) {
            modules.getNotifications().onConversationClose(peer);
        }
    }

    public void onDialogsOpen() {
        if (modules.getNotifications() != null) {
            modules.getNotifications().onDialogsOpen();
        }
    }

    public void onDialogsClosed() {
        if (modules.getNotifications() != null) {
            modules.getNotifications().onDialogsClosed();
        }
    }

    public void onProfileOpen(int uid) {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().subscribe(Peer.user(uid));
        }
    }

    public void onProfileClosed(int uid) {

    }

    public void onTyping(Peer peer) {
        modules.getTypingModule().onTyping(peer);
    }

    public void onPhoneBookChanged() {
        if (modules.getContactsModule() != null) {
            modules.getContactsModule().onPhoneBookChanged();
        }
    }

    public void saveDraft(Peer peer, String draft) {
        modules.getMessagesModule().saveDraft(peer, draft);
    }

    public void deleteMessages(Peer peer, long[] rids) {
        modules.getMessagesModule().deleteMessages(peer, rids);
    }

    public String loadDraft(Peer peer) {
        return modules.getMessagesModule().loadDraft(peer);
    }

    public void sendMessage(Peer peer, String text) {
        modules.getMessagesModule().sendMessage(peer, text);
    }

    public void sendPhoto(Peer peer, String fileName,
                          int w, int h, FastThumb fastThumb,
                          FileSystemReference fileSystemReference) {
        modules.getMessagesModule().sendPhoto(peer, fileName, w, h, fastThumb, fileSystemReference);
    }

    public void sendVideo(Peer peer, String fileName, int w, int h, int duration,
                          FastThumb fastThumb, FileSystemReference fileSystemReference) {
        modules.getMessagesModule().sendVideo(peer, fileName, w, h, duration, fastThumb, fileSystemReference);
    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FileSystemReference fileSystemReference) {
        sendDocument(peer, fileName, mimeType, fileSystemReference, null);
    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FileSystemReference fileSystemReference,
                             FastThumb fastThumb) {
        modules.getMessagesModule().sendDocument(peer, fileName, mimeType, fastThumb, fileSystemReference);
    }

    public Command<Boolean> editMyName(final String newName) {
        return modules.getUsersModule().editMyName(newName);
    }

    public Command<Boolean> editName(final int uid, final String name) {
        return modules.getUsersModule().editName(uid, name);
    }

    public Command<Integer> createGroup(String title, int[] uids) {
        return modules.getGroupsModule().createGroup(title, uids);
    }

    public Command<Boolean> editGroupTitle(final int gid, final String title) {
        return modules.getGroupsModule().editTitle(gid, title);
    }

    public Command<Boolean> leaveGroup(final int gid) {
        return modules.getGroupsModule().leaveGroup(gid);
    }

    public Command<Boolean> addMemberToGroup(int gid, int uid) {
        return modules.getGroupsModule().addMemberToGroup(gid, uid);
    }

    public Command<Boolean> kickMember(int gid, int uid) {
        return modules.getGroupsModule().kickMember(gid, uid);
    }

    public Command<Boolean> removeContact(int uid) {
        return modules.getContactsModule().removeContact(uid);
    }

    public Command<Boolean> addContact(int uid) {
        return modules.getContactsModule().addContact(uid);
    }

    public Command<UserVM[]> findUsers(String query) {
        return modules.getContactsModule().findUsers(query);
    }

    public Command<Boolean> deleteChat(Peer peer) {
        return modules.getMessagesModule().deleteChat(peer);
    }

    public Command<Boolean> clearChat(Peer peer) {
        return modules.getMessagesModule().clearChat(peer);
    }

    // File operations

    public FileVM bindFile(FileReference fileReference, boolean isAutoStart, FileVMCallback callback) {
        return new FileVM(fileReference, isAutoStart, modules, callback);
    }

    public UploadFileVM bindUpload(long rid, UploadFileVMCallback callback) {
        return new UploadFileVM(rid, callback, modules);
    }

    public void bindRawFile(FileReference fileReference, boolean isAutoStart, DownloadCallback callback) {
        modules.getFilesModule().bindFile(fileReference, isAutoStart, callback);
    }

    public void unbindRawFile(long fileId, boolean isAutoCancel, DownloadCallback callback) {
        modules.getFilesModule().unbindFile(fileId, callback, isAutoCancel);
    }

    public void bindRawUploadFile(long rid, UploadCallback callback) {
        modules.getFilesModule().bindUploadFile(rid, callback);
    }

    public void unbindRawUploadFile(long rid, UploadCallback callback) {
        modules.getFilesModule().unbindUploadFile(rid, callback);
    }

    public void requestState(long fileId, final DownloadCallback callback) {
        modules.getFilesModule().requestState(fileId, callback);
    }

    public void requestUploadState(long rid, UploadCallback callback) {
        modules.getFilesModule().requestUploadState(rid, callback);
    }

    public void cancelDownloading(long fileId) {
        modules.getFilesModule().cancelDownloading(fileId);
    }

    public void startDownloading(FileReference location) {
        modules.getFilesModule().startDownloading(location);
    }

    public void resumeUpload(long rid) {
        modules.getFilesModule().resumeUpload(rid);
    }

    public void pauseUpload(long rid) {
        modules.getFilesModule().pauseUpload(rid);
    }

    public String getDownloadedDescriptor(long fileId) {
        return modules.getFilesModule().getDownloadedDescriptor(fileId);
    }

    // Settings operations

    public boolean isConversationTonesEnabled() {
        return modules.getSettings().isConversationTonesEnabled();
    }

    public void changeConversationTonesEnabled(boolean val) {
        modules.getSettings().changeConversationTonesEnabled(val);
    }

    public boolean isNotificationSoundEnabled() {
        return modules.getSettings().isNotificationSoundEnabled();
    }

    public void changeNotificationSoundEnabled(boolean val) {
        modules.getSettings().changeNotificationSoundEnabled(val);
    }

    public boolean isNotificationVibrationEnabled() {
        return modules.getSettings().isVibrationEnabled();
    }

    public void changeNotificationVibrationEnabled(boolean val) {
        modules.getSettings().changeNotificationVibrationEnabled(val);
    }

    public boolean isShowNotificationsText() {
        return modules.getSettings().isShowNotificationsText();
    }

    public void changeShowNotificationTextEnabled(boolean val) {
        modules.getSettings().changeShowNotificationTextEnabled(val);
    }

    public boolean isSendByEnterEnabled() {
        return modules.getSettings().isSendByEnterEnabled();
    }

    public void changeSendByEnter(boolean val) {
        modules.getSettings().changeSendByEnter(val);
    }

    public boolean isNotificationsEnabled(Peer peer) {
        return modules.getSettings().isNotificationsEnabled(peer);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        modules.getSettings().changeNotificationsEnabled(peer, val);
    }

    public OwnAvatarVM getOwnAvatarVM() {
        return modules.getProfile().getOwnAvatarVM();
    }

    public GroupAvatarVM getGroupAvatarVM(int gid) {
        return modules.getGroupsModule().getAvatarVM(gid);
    }

    public void changeGroupAvatar(int gid, String descriptor) {
        modules.getGroupsModule().changeAvatar(gid, descriptor);
    }

    public void changeAvatar(String descriptor) {
        modules.getProfile().changeAvatar(descriptor);
    }

    public void removeAvatar() {
        modules.getProfile().removeAvatar();
    }

    public AppStateVM getAppState() {
        return modules.getAppStateModule().getAppStateVM();
    }
}