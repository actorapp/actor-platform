/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.model.api.AuthSession;
import im.actor.model.concurrency.Command;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Group;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.util.ActorTrace;
import im.actor.model.util.Timing;
import im.actor.model.viewmodel.AppStateVM;
import im.actor.model.viewmodel.FileCallback;
import im.actor.model.viewmodel.FileVM;
import im.actor.model.viewmodel.FileVMCallback;
import im.actor.model.viewmodel.GroupAvatarVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.OwnAvatarVM;
import im.actor.model.viewmodel.UploadFileCallback;
import im.actor.model.viewmodel.UploadFileVM;
import im.actor.model.viewmodel.UploadFileVMCallback;
import im.actor.model.viewmodel.UserVM;

/**
 * Entry point to Actor Messaging
 * Before using Messenger you need to create Configuration object by using ConfigurationBuilder.
 */
public class Messenger {

    protected Modules modules;

    /**
     * Construct messenger
     *
     * @param configuration configuration of messenger
     */
    @ObjectiveCName("initWithConfiguration:")
    public Messenger(@NotNull Configuration configuration) {
        // We assume that configuration is valid and all configuration verification
        // Must be implemented in Configuration object

        // Init Log
        Log.setLog(configuration.getLog());
        // Init Execution environment
        Environment.setThreadingProvider(configuration.getThreadingProvider());
        Environment.setDispatcherProvider(configuration.getDispatcherProvider());

        // Start Messenger initialization

        Timing timing = new Timing("MESSENGER_INIT");

        // Init Crypto
        timing.section("Crypto");
        CryptoUtils.init(configuration.getCryptoProvider());

        // Init MVVM
        timing.section("MVVM");
        MVVMEngine.init(configuration.getMainThreadProvider());

        // Actor system
        timing.section("Actors");
        ActorSystem.system().setTraceInterface(new ActorTrace());
        if (!configuration.getMainThreadProvider().isSingleThread()) {
            ActorSystem.system().addDispatcher("db", 1);
        }

        timing.section("Modules:Create");
        this.modules = new Modules(this, configuration);

        timing.section("Modules:Run");
        this.modules.run();

        timing.end();
    }

    //////////////////////////////////////
    //         Authentication
    //////////////////////////////////////

    /**
     * Get current Authentication state
     *
     * @return current Authentication state
     */
    @NotNull
    public AuthState getAuthState() {
        return modules.getAuthModule().getAuthState();
    }

    /**
     * Convenience method for checking if user logged in
     *
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return getAuthState() == AuthState.LOGGED_IN;
    }

    /**
     * Request sms activation code
     *
     * @param phone phone number in international format
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("requestSmsCommandWithPhone:")
    public Command<AuthState> requestSms(final long phone) {
        return modules.getAuthModule().requestSms(phone);
    }

    /**
     * Sending activation code
     *
     * @param code activation code
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("sendCodeCommand:")
    public Command<AuthState> sendCode(final int code) {
        return modules.getAuthModule().sendCode(code);
    }

    /**
     * Perform signup
     *
     * @param name       Name of User
     * @param avatarPath File descriptor of avatar (may be null if not set)
     * @param isSilent   is silent registration (disable notification about registration)
     * @return Comand for execution
     */
    @NotNull
    @ObjectiveCName("signUpCommandWithName:withAvatar:silently:")
    public Command<AuthState> signUp(String name, String avatarPath, boolean isSilent) {
        return modules.getAuthModule().signUp(name, avatarPath, isSilent);
    }

    /**
     * Get current Authentication phone.
     * Value is valid only for SIGN_UP or CODE_VALIDATION states.
     *
     * @return phone number in international format
     */
    @ObjectiveCName("getAuthPhone")
    public long getAuthPhone() {
        return modules.getAuthModule().getPhone();
    }

    /**
     * Resetting authentication process
     */
    @ObjectiveCName("resetAuth")
    public void resetAuth() {
        modules.getAuthModule().resetAuth();
    }

    public void onLoggedIn() {

    }

    //////////////////////////////////////
    //        Authenticated state
    //////////////////////////////////////

    /**
     * Get ViewModel of application state
     *
     * @return view model of application state
     */
    @NotNull
    @ObjectiveCName("getAppState")
    public AppStateVM getAppState() {
        return modules.getAppStateModule().getAppStateVM();
    }

    /**
     * Get authenticated User Id
     *
     * @return current User Id
     */
    @ObjectiveCName("myUid")
    public int myUid() {
        return modules.getAuthModule().myUid();
    }


    //////////////////////////////////////
    //           View Models
    //////////////////////////////////////

    /**
     * Get User View Model Collection
     *
     * @return User ViewModel Collection
     */
    @Nullable
    @ObjectiveCName("getUsers")
    public MVVMCollection<User, UserVM> getUsers() {
        if (modules.getUsersModule() == null) {
            return null;
        }
        return modules.getUsersModule().getUsersCollection();
    }

    /**
     * Get User Value Model by UID
     *
     * @param uid uid
     * @return User Value Model
     */
    @NotNull
    @ObjectiveCName("getUserWithUid:")
    public UserVM getUser(int uid) {
        //noinspection ConstantConditions
        return getUsers().get(uid);
    }

    /**
     * Get Group View Model Collection
     *
     * @return Group ViewModel Collection
     */
    @Nullable
    @ObjectiveCName("getGroups")
    public MVVMCollection<Group, GroupVM> getGroups() {
        if (modules.getGroupsModule() == null) {
            return null;
        }
        return modules.getGroupsModule().getGroupsCollection();
    }

    /**
     * Get Group Value Model by GID
     *
     * @param gid gid
     * @return Group Value Model
     */
    @NotNull
    @ObjectiveCName("getGroupWithGid:")
    public GroupVM getGroup(int gid) {
        //noinspection ConstantConditions
        return getGroups().get(gid);
    }

    /**
     * Get private chat ViewModel
     *
     * @param uid chat's User Id
     * @return ValueModel of Boolean for typing state
     */
    @Nullable
    @ObjectiveCName("getTypingWithUid:")
    public ValueModel<Boolean> getTyping(int uid) {
        if (modules.getTypingModule() == null) {
            return null;
        }
        return modules.getTypingModule().getTyping(uid).getTyping();
    }

    /**
     * Get group chat ViewModel
     *
     * @param gid chat's Group Id
     * @return ValueModel of int[] for typing state
     */
    @Nullable
    @ObjectiveCName("getGroupTypingWithGid:")
    public ValueModel<int[]> getGroupTyping(int gid) {
        if (modules.getTypingModule() == null) {
            return null;
        }
        return modules.getTypingModule().getGroupTyping(gid).getActive();
    }

    /**
     * Get Own avatar ViewModel
     * Used for displaying avatar change progress
     *
     * @return the OwnAvatarVM
     */
    @Nullable
    @ObjectiveCName("getOwnAvatarVM")
    public OwnAvatarVM getOwnAvatarVM() {
        return modules.getProfile().getOwnAvatarVM();
    }

    /**
     * Get Group avatar ViewModel
     * Used for displaying group avatar change progress
     *
     * @param gid group's ID
     * @return the GroupAvatarVM
     */
    @Nullable
    @ObjectiveCName("getGroupAvatarVMWithGid:")
    public GroupAvatarVM getGroupAvatarVM(int gid) {
        return modules.getGroupsModule().getAvatarVM(gid);
    }


    //////////////////////////////////////
    //         Application events
    //////////////////////////////////////

    /**
     * MUST be called on app became visible
     */
    @ObjectiveCName("onAppVisible")
    public void onAppVisible() {
        modules.onAppVisible();
    }

    /**
     * MUST be called on app became hidden
     */
    @ObjectiveCName("onAppHidden")
    public void onAppHidden() {
        modules.onAppHidden();
    }

    /**
     * MUST be called on dialogs open
     */
    @ObjectiveCName("onDialogsOpen")
    public void onDialogsOpen() {
        if (modules.getNotifications() != null) {
            modules.getNotifications().onDialogsOpen();
        }
    }

    /**
     * MUST be called on dialogs closed
     */
    @ObjectiveCName("onDialogsClosed")
    public void onDialogsClosed() {
        if (modules.getNotifications() != null) {
            modules.getNotifications().onDialogsClosed();
        }
    }

    /**
     * MUST be called on conversation open
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onConversationOpenWithPeer:")
    public void onConversationOpen(Peer peer) {
        modules.getAnalytics().trackChatOpen(peer);
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().subscribe(peer);
            modules.getNotifications().onConversationOpen(peer);
            modules.getMessagesModule().onConversationOpen(peer);
        }
    }

    /**
     * MUST be called on conversation closed
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onConversationClosedWithPeer:")
    public void onConversationClosed(Peer peer) {
        modules.getAnalytics().trackChatClosed(peer);
        if (modules.getPresenceModule() != null) {
            modules.getNotifications().onConversationClose(peer);
        }
    }

    /**
     * MUST be called on profile open
     *
     * @param uid user's Id
     */
    @ObjectiveCName("onProfileOpenWithUid:")
    public void onProfileOpen(int uid) {
        modules.getAnalytics().trackProfileOpen(uid);
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().subscribe(Peer.user(uid));
        }
    }

    /**
     * MUST be called on profile closed
     *
     * @param uid user's Id
     */
    @ObjectiveCName("onProfileClosedWithUid:")
    public void onProfileClosed(int uid) {
        modules.getAnalytics().trackProfileClosed(uid);
    }

    /**
     * MUST be called on typing in chat.
     * Can be called with any frequency
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onTypingWithPeer:")
    public void onTyping(Peer peer) {
        modules.getTypingModule().onTyping(peer);
    }


    //////////////////////////////////////
    //         Technical events
    //////////////////////////////////////

    /**
     * MUST be called when phone book change detected
     */
    @ObjectiveCName("onPhoneBookChanged")
    public void onPhoneBookChanged() {
        if (modules.getContactsModule() != null) {
            modules.getContactsModule().onPhoneBookChanged();
        }
    }

    /**
     * MUST be called when network status change detected
     */
    @ObjectiveCName("onNetworkChanged")
    public void onNetworkChanged() {
        modules.getActorApi().onNetworkChanged();
    }

    /**
     * MUST be called when external push received
     *
     * @param seq sequence number of update
     */
    @ObjectiveCName("onPushReceivedWithSeq:")
    public void onPushReceived(int seq) {
        if (modules.getUpdatesModule() != null) {
            modules.getUpdatesModule().onPushReceived(seq);
        }
    }

    //////////////////////////////////////
    //      Messaging operations
    //////////////////////////////////////

    /**
     * Send Text Message
     *
     * @param peer destination peer
     * @param text message text
     */
    @ObjectiveCName("sendMessageWithPeer:withText:")
    public void sendMessage(Peer peer, String text) {
        modules.getMessagesModule().sendMessage(peer, text);
    }

    /**
     * Send Photo message
     *
     * @param peer       destination peer
     * @param fileName   File name (without path)
     * @param w          photo width
     * @param h          photo height
     * @param fastThumb  Fast thumb of photo
     * @param descriptor File Descriptor
     */
    @ObjectiveCName("sendPhotoWithPeer:withName:withW:withH:withThumb:withDescriptor:")
    public void sendPhoto(Peer peer, String fileName,
                          int w, int h, FastThumb fastThumb,
                          String descriptor) {
        modules.getMessagesModule().sendPhoto(peer, fileName, w, h, fastThumb, descriptor);
    }

    /**
     * Send Video message
     *
     * @param peer       destination peer
     * @param fileName   File name (without path)
     * @param w          video width
     * @param h          video height
     * @param duration   video duration
     * @param fastThumb  Fast thumb of video
     * @param descriptor File Descriptor
     */
    @ObjectiveCName("sendVideoWithPeer:withName:withW:withH:withDuration:withThumb:withDescriptor:")
    public void sendVideo(Peer peer, String fileName, int w, int h, int duration,
                          FastThumb fastThumb, String descriptor) {
        modules.getMessagesModule().sendVideo(peer, fileName, w, h, duration, fastThumb, descriptor);
    }

    /**
     * Send document without preview
     *
     * @param peer       destination peer
     * @param fileName   File name (without path)
     * @param mimeType   mimetype of document
     * @param descriptor File Descriptor
     */
    @ObjectiveCName("sendDocumentWithPeer:withName:withMime:withDescriptor:")
    public void sendDocument(Peer peer, String fileName, String mimeType, String descriptor) {
        sendDocument(peer, fileName, mimeType, null, descriptor);
    }

    /**
     * Send document with preview
     *
     * @param peer       destination peer
     * @param fileName   File name (without path)
     * @param mimeType   mimetype of document
     * @param descriptor File Descriptor
     * @param fastThumb  FastThumb of preview
     */
    @ObjectiveCName("sendDocumentWithPeer:withName:withMime:withThumb:withDescriptor:")
    public void sendDocument(Peer peer, String fileName, String mimeType, FastThumb fastThumb,
                             String descriptor) {
        modules.getMessagesModule().sendDocument(peer, fileName, mimeType, fastThumb, descriptor);
    }

    /**
     * Delete messages
     *
     * @param peer destination peer
     * @param rids rids of messages
     */
    @ObjectiveCName("deleteMessagesWithPeer:withRids:")
    public void deleteMessages(Peer peer, long[] rids) {
        modules.getMessagesModule().deleteMessages(peer, rids);
    }

    /**
     * Delete chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("deleteChatCommandWithPeer:")
    public Command<Boolean> deleteChat(Peer peer) {
        return modules.getMessagesModule().deleteChat(peer);
    }

    /**
     * Clear chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("clearChatCommandWithPeer:")
    public Command<Boolean> clearChat(Peer peer) {
        return modules.getMessagesModule().clearChat(peer);
    }

    /**
     * Save message draft
     *
     * @param peer  destination peer
     * @param draft message draft
     */
    @ObjectiveCName("saveDraftWithPeer:withDraft:")
    public void saveDraft(Peer peer, String draft) {
        modules.getMessagesModule().saveDraft(peer, draft);
    }

    /**
     * Load message draft
     *
     * @param peer destination peer
     * @return null if no draft available
     */
    @Nullable
    @ObjectiveCName("loadDraftWithPeer:")
    public String loadDraft(Peer peer) {
        return modules.getMessagesModule().loadDraft(peer);
    }


    //////////////////////////////////////
    //         Peer operations
    //////////////////////////////////////

    /**
     * Edit current user's name
     *
     * @param newName new user's name
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editMyNameCommandWithName:")
    public Command<Boolean> editMyName(final String newName) {
        return modules.getUsersModule().editMyName(newName);
    }

    /**
     * Change current user's avatar
     *
     * @param descriptor descriptor of avatar file
     */
    @ObjectiveCName("changeMyAvatarWithDescriptor:")
    public void changeMyAvatar(String descriptor) {
        modules.getProfile().changeAvatar(descriptor);
    }

    /**
     * Remove current user's avatar
     */
    @ObjectiveCName("removeMyAvatar")
    public void removeMyAvatar() {
        modules.getProfile().removeAvatar();
    }

    /**
     * Edit user's local name
     *
     * @param uid  user's id
     * @param name new user's local name
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editNameCommandWithUid:withName:")
    public Command<Boolean> editName(final int uid, final String name) {
        return modules.getUsersModule().editName(uid, name);
    }

    /**
     * Edit group's title
     *
     * @param gid   group's id
     * @param title new group title
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editGroupTitleCommandWithGid:withTitle:")
    public Command<Boolean> editGroupTitle(final int gid, final String title) {
        return modules.getGroupsModule().editTitle(gid, title);
    }

    /**
     * Change group avatar
     *
     * @param gid        group's id
     * @param descriptor descriptor of avatar file
     */
    @ObjectiveCName("changeGroupAvatarWithGid:withDescriptor:")
    public void changeGroupAvatar(int gid, String descriptor) {
        modules.getGroupsModule().changeAvatar(gid, descriptor);
    }

    /**
     * Removing group avatar
     *
     * @param gid group's id
     */
    @ObjectiveCName("removeGroupAvatarWithGid:")
    public void removeGroupAvatar(int gid) {
        modules.getGroupsModule().removeAvatar(gid);
    }


    //////////////////////////////////////
    //         Group operations
    //////////////////////////////////////

    /**
     * Create group
     *
     * @param title            group title
     * @param avatarDescriptor descriptor of group avatar (can be null if not set)
     * @param uids             member's ids
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("createGroupCommandWithTitle:withAvatar:withUids:")
    public Command<Integer> createGroup(String title, String avatarDescriptor, int[] uids) {
        return modules.getGroupsModule().createGroup(title, avatarDescriptor, uids);
    }


    /**
     * Leave group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("leaveGroupCommandWithGid:")
    public Command<Boolean> leaveGroup(final int gid) {
        return modules.getGroupsModule().leaveGroup(gid);
    }

    /**
     * Adding member to group
     *
     * @param gid group's id
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("inviteMemberCommandWithGid:withUid:")
    public Command<Boolean> inviteMember(int gid, int uid) {
        return modules.getGroupsModule().addMemberToGroup(gid, uid);
    }

    /**
     * Kick member from group
     *
     * @param gid group's id
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("kickMemberCommandWithGid:withUid:")
    public Command<Boolean> kickMember(int gid, int uid) {
        return modules.getGroupsModule().kickMember(gid, uid);
    }

    /**
     * Request invite link for group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("requestInviteLinkCommandWithGid:")
    public Command<String> requestInviteLink(int gid) {
        return modules.getGroupsModule().requestInviteLink(gid);
    }

    /**
     * Revoke invite link for group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("requestRevokeLinkCommandWithGid:")
    public Command<String> revokeInviteLink(int gid) {
        return modules.getGroupsModule().requestRevokeLink(gid);
    }

    @Nullable
    @ObjectiveCName("joinGroupViaLinkCommandWithUrl:")
    public Command<Integer> joinGroupViaLink(String url) {
        return modules.getGroupsModule().joinGroupViaLink(url);
    }

    //////////////////////////////////////
    //         Contact operations
    //////////////////////////////////////

    /**
     * Remove user from contact's list
     *
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("removeContactCommandWithUid:")
    public Command<Boolean> removeContact(int uid) {
        return modules.getContactsModule().removeContact(uid);
    }

    /**
     * Add contact to contact's list
     *
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("addContactCommandWithUid:")
    public Command<Boolean> addContact(int uid) {
        return modules.getContactsModule().addContact(uid);
    }

    /**
     * Find Users
     *
     * @param query query for search
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("findUsersCommandWithQuery:")
    public Command<UserVM[]> findUsers(String query) {
        return modules.getContactsModule().findUsers(query);
    }


    /**
     * Bind File View Model
     *
     * @param fileReference reference to file
     * @param isAutoStart   automatically start download
     * @param callback      View Model file state callback
     * @return File View Model
     */
    @Nullable
    @ObjectiveCName("bindFileWithReference:autoStart:withCallback:")
    public FileVM bindFile(FileReference fileReference, boolean isAutoStart, FileVMCallback callback) {
        return new FileVM(fileReference, isAutoStart, modules, callback);
    }

    /**
     * Bind Uploading File View Model
     *
     * @param rid      randomId of uploading file
     * @param callback View Model file state callback
     * @return Upload File View Model
     */
    @Nullable
    @ObjectiveCName("bindUploadWithRid:withCallback:")
    public UploadFileVM bindUpload(long rid, UploadFileVMCallback callback) {
        return new UploadFileVM(rid, callback, modules);
    }

    /**
     * Raw Bind File
     *
     * @param fileReference reference to file
     * @param isAutoStart   automatically start download
     * @param callback      file state callback
     */
    @ObjectiveCName("bindRawFileWithReference:autoStart:withCallback:")
    public void bindRawFile(FileReference fileReference, boolean isAutoStart, FileCallback callback) {
        modules.getFilesModule().bindFile(fileReference, isAutoStart, callback);
    }

    /**
     * Unbind Raw File
     *
     * @param fileId       file id
     * @param isAutoCancel automatically cancel download
     * @param callback     file state callback
     */
    @ObjectiveCName("unbindRawFileWithFileId:autoCancel:withCallback:")
    public void unbindRawFile(long fileId, boolean isAutoCancel, FileCallback callback) {
        modules.getFilesModule().unbindFile(fileId, callback, isAutoCancel);
    }

    /**
     * Raw Bind Upload File
     *
     * @param rid      randomId of uploading file
     * @param callback file state callback
     */
    @ObjectiveCName("bindRawUploadFileWithRid:withCallback:")
    public void bindRawUploadFile(long rid, UploadFileCallback callback) {
        modules.getFilesModule().bindUploadFile(rid, callback);
    }

    /**
     * Unbind Raw Upload File
     *
     * @param rid      randomId of uploading file
     * @param callback file state callback
     */
    @ObjectiveCName("unbindRawUploadFileWithRid:withCallback:")
    public void unbindRawUploadFile(long rid, UploadFileCallback callback) {
        modules.getFilesModule().unbindUploadFile(rid, callback);
    }

    /**
     * Request file state
     *
     * @param fileId   file id
     * @param callback file state callback
     */
    @ObjectiveCName("requestStateWithFileId:withCallback:")
    public void requestState(long fileId, final FileCallback callback) {
        modules.getFilesModule().requestState(fileId, callback);
    }

    /**
     * Request upload file state
     *
     * @param rid      file's random id
     * @param callback file state callback
     */
    @ObjectiveCName("requestUploadStateWithRid:withCallback:")
    public void requestUploadState(long rid, UploadFileCallback callback) {
        modules.getFilesModule().requestUploadState(rid, callback);
    }

    /**
     * Cancel file download
     *
     * @param fileId file's id
     */
    @ObjectiveCName("cancelDownloadingWithFileId:")
    public void cancelDownloading(long fileId) {
        modules.getFilesModule().cancelDownloading(fileId);
    }

    /**
     * Start file download
     *
     * @param reference file's reference
     */
    @ObjectiveCName("startDownloadingWithReference:")
    public void startDownloading(FileReference reference) {
        modules.getFilesModule().startDownloading(reference);
    }

    /**
     * Resume upload
     *
     * @param rid file's random id
     */
    @ObjectiveCName("resumeUploadWithRid:")
    public void resumeUpload(long rid) {
        modules.getFilesModule().resumeUpload(rid);
    }

    /**
     * Pause upload
     *
     * @param rid file's random id
     */
    @ObjectiveCName("pauseUploadWithRid:")
    public void pauseUpload(long rid) {
        modules.getFilesModule().pauseUpload(rid);
    }

    /**
     * Get downloaded file descriptor
     *
     * @param fileId file' id
     * @return descriptor if file is downloaded
     */
    @Deprecated
    @Nullable
    @ObjectiveCName("getDownloadedDescriptorWithFileId:")
    public String getDownloadedDescriptor(long fileId) {
        return modules.getFilesModule().getDownloadedDescriptor(fileId);
    }

    //////////////////////////////////////
    //            Settings
    //////////////////////////////////////

    /**
     * Is in app conversation tones enabled
     *
     * @return is conversation tones enabled flag
     */
    @ObjectiveCName("isConversationTonesEnabled")
    public boolean isConversationTonesEnabled() {
        return modules.getSettings().isConversationTonesEnabled();
    }

    /**
     * Change conversation tones enabled value
     *
     * @param val is conversation tones enabled
     */
    @ObjectiveCName("changeConversationTonesEnabledWithValue:")
    public void changeConversationTonesEnabled(boolean val) {
        modules.getSettings().changeConversationTonesEnabled(val);
    }

    /**
     * Is notifications enabled setting
     *
     * @return is notifications enabled
     */
    @ObjectiveCName("isNotificationsEnabled")
    public boolean isNotificationsEnabled() {
        return modules.getSettings().isNotificationsEnabled();
    }

    /**
     * Change notifications enabled value
     *
     * @param val is notifications enabled
     */
    @ObjectiveCName("changeNotificationsEnabledWithValue:")
    public void changeNotificationsEnabled(boolean val) {
        modules.getSettings().changeNotificationsEnabled(val);
    }

    /**
     * Is notifications sounds enabled
     *
     * @return is notification sounds enabled
     */
    @ObjectiveCName("isNotificationSoundEnabled")
    public boolean isNotificationSoundEnabled() {
        return modules.getSettings().isNotificationSoundEnabled();
    }

    /**
     * Change notification sounds enabled
     *
     * @param val is notification sounds enabled
     */
    @ObjectiveCName("changeNotificationSoundEnabledWithValue:")
    public void changeNotificationSoundEnabled(boolean val) {
        modules.getSettings().changeNotificationSoundEnabled(val);
    }

    /**
     * Sound that used for notifications
     *
     * @return notification sound name
     */
    @Nullable
    @ObjectiveCName("getNotificationSound")
    public String getNotificationSound() {
        return modules.getSettings().getNotificationSound();
    }

    /**
     * Change sound that used for notifications
     *
     * @param sound notification sound name
     */
    @ObjectiveCName("changeNotificationSoundWithSound:")
    public void changeNotificationSound(String sound) {
        modules.getSettings().changeNotificationSound(sound);
    }

    /**
     * Is notification vibration enabled
     *
     * @return is notification vibration enabled
     */
    @ObjectiveCName("isNotificationVibrationEnabled")
    public boolean isNotificationVibrationEnabled() {
        return modules.getSettings().isVibrationEnabled();
    }

    /**
     * Change notification vibration enabled
     *
     * @param val is notification vibration enabled
     */
    @ObjectiveCName("changeNotificationVibrationEnabledWithValue")
    public void changeNotificationVibrationEnabled(boolean val) {
        modules.getSettings().changeNotificationVibrationEnabled(val);
    }

    /**
     * Is displaying text in notifications enabled
     *
     * @return is displaying text in notifications enabled
     */
    @ObjectiveCName("isShowNotificationsText")
    public boolean isShowNotificationsText() {
        return modules.getSettings().isShowNotificationsText();
    }

    /**
     * Change displaying text in notifications enabled
     *
     * @param val is displaying text in notifications enabled
     */
    @ObjectiveCName("changeShowNotificationTextEnabledWithValue:")
    public void changeShowNotificationTextEnabled(boolean val) {
        modules.getSettings().changeShowNotificationTextEnabled(val);
    }

    /**
     * Is send by enter enabled. Useful for android and web.
     *
     * @return is send by enter enabled
     */
    @ObjectiveCName("isSendByEnterEnabled")
    public boolean isSendByEnterEnabled() {
        return modules.getSettings().isSendByEnterEnabled();
    }

    /**
     * Change if send by enter enabled
     *
     * @param val is send by enter enabled
     */
    @ObjectiveCName("changeSendByEnterWithValue:")
    public void changeSendByEnter(boolean val) {
        modules.getSettings().changeSendByEnter(val);
    }

    /**
     * Is notifications enabled for peer
     *
     * @param peer destination peer
     * @return is notifications enabled
     */
    @ObjectiveCName("isNotificationsEnabledWithPeer:")
    public boolean isNotificationsEnabled(Peer peer) {
        return modules.getSettings().isNotificationsEnabled(peer);
    }

    /**
     * Change if notifications enabled for peer
     *
     * @param peer destination peer
     * @param val  is notifications enabled
     */
    @ObjectiveCName("changeNotificationsEnabledWithPeer:withValue:")
    public void changeNotificationsEnabled(Peer peer, boolean val) {
        modules.getSettings().changeNotificationsEnabled(peer, val);
    }

    /**
     * Is in-app notifications enabled
     *
     * @return is notifications enabled
     */
    @ObjectiveCName("isInAppNotificationsEnabled")
    public boolean isInAppNotificationsEnabled() {
        return modules.getSettings().isInAppEnabled();
    }

    /**
     * Change in-app notifications enable value
     *
     * @param val is notifications enabled
     */
    @ObjectiveCName("changeInAppNotificationsEnabledWithValue:")
    public void changeInAppNotificationsEnabled(boolean val) {
        modules.getSettings().changeInAppEnabled(val);
    }

    /**
     * Is in-app notifications sound enabled
     *
     * @return is notifications sound enabled
     */
    @ObjectiveCName("isInAppNotificationSoundEnabled")
    public boolean isInAppNotificationSoundEnabled() {
        return modules.getSettings().isInAppSoundEnabled();
    }

    /**
     * Change in-app notifications sound enabled value
     *
     * @param val is notifications sound enabled
     */
    @ObjectiveCName("changeInAppNotificationSoundEnabledWithValue:")
    public void changeInAppNotificationSoundEnabled(boolean val) {
        modules.getSettings().changeInAppSoundEnabled(val);
    }

    /**
     * Is in-app notification vibration enabled
     *
     * @return is notifications vibration enabled
     */
    @ObjectiveCName("isInAppNotificationVibrationEnabled")
    public boolean isInAppNotificationVibrationEnabled() {
        return modules.getSettings().isInAppVibrationEnabled();
    }

    /**
     * Change in-app notifications vibration enabled value
     *
     * @param val is notifications vibration enabled
     */
    @ObjectiveCName("changeInAppNotificationVibrationEnabledWithValue:")
    public void changeInAppNotificationVibrationEnabled(boolean val) {
        modules.getSettings().changeInAppVibrationEnabled(val);
    }

    /**
     * Change group invite url
     *
     * @param peer destination peer
     * @param val invite url
     */
    @ObjectiveCName("changeGroupInviteLinkWithValue:")
    public void changeGroupInviteLink(Peer peer, String val) {
        modules.getSettings().changeGroupInviteLink(peer, val);
    }

    /**
     * Current group invite url
     *
     * @param peer destination peer
     * @return current group invite url
     */
    @ObjectiveCName("getGroupInviteLinkWithPeer:")
    public String getGroupInviteLink(Peer peer) {
        return modules.getSettings().getGroupInviteLink(peer);
    }


    //////////////////////////////////////
    //            Security
    //////////////////////////////////////

    /**
     * Loading active sessions
     *
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("loadSessionsCommand")
    public Command<List<AuthSession>> loadSessions() {
        return modules.getSecurity().loadSessions();
    }

    /**
     * Terminate all other sessions
     *
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("terminateAllSessionsCommand")
    public Command<Boolean> terminateAllSessions() {
        return modules.getSecurity().terminateAllSessions();
    }

    /**
     * Terminate active session
     *
     * @param id session id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("terminateSessionCommandWithId:")
    public Command<Boolean> terminateSession(int id) {
        return modules.getSecurity().terminateSession(id);
    }

    //////////////////////////////////////
    //         User Tracking
    //////////////////////////////////////

    /**
     * Track phone number authentication screen
     */
    @ObjectiveCName("trackAuthPhoneOpen")
    public void trackAuthPhoneOpen() {
        modules.getAnalytics().trackAuthPhoneOpen();
    }

    /**
     * Track pick country open
     */
    @ObjectiveCName("trackAuthCountryOpen")
    public void trackAuthCountryOpen() {
        modules.getAnalytics().trackAuthCountryOpen();
    }

    /**
     * Track pick country closed
     */
    @ObjectiveCName("trackAuthCountryClosed")
    public void trackAuthCountryClosed() {
        modules.getAnalytics().trackAuthCountryClosed();
    }

    /**
     * Track country picked
     */
    @ObjectiveCName("trackAuthCountryPickedWithCountry:")
    public void trackAuthCountryPicked(String country) {
        modules.getAnalytics().trackAuthCountryPicked(country);
    }

    /**
     * Track auth phone typing
     */
    @ObjectiveCName("trackAuthPhoneTypeWithValue:")
    public void trackAuthPhoneType(String newValue) {
        modules.getAnalytics().trackAuthPhoneType(newValue);
    }

    /**
     * Tack opening why screen
     */
    @ObjectiveCName("trackAuthPhoneInfoOpen")
    public void trackAuthPhoneInfoOpen() {
        modules.getAnalytics().trackAuthPhoneInfoOpen();
    }

    /**
     * Track request code tap
     */
    @ObjectiveCName("trackCodeRequest")
    public void trackCodeRequest() {
        modules.getAnalytics().trackCodeRequest();
    }

    @ObjectiveCName("trackAuthCodeTypeWithValue:")
    public void trackAuthCodeType(String newValue) {
        modules.getAnalytics().trackAuthCodeType(newValue);
    }

    @ObjectiveCName("trackBackPressed")
    public void trackBackPressed() {
        modules.getAnalytics().trackBackPressed();
    }

    @ObjectiveCName("trackUpPressed")
    public void trackUpPressed() {
        modules.getAnalytics().trackUpPressed();
    }

    @ObjectiveCName("trackAuthCodeWrongNumber")
    public void trackAuthCodeWrongNumber() {
        modules.getAnalytics().trackAuthCodeWrongNumber();
    }

    @ObjectiveCName("trackAuthCodeWrongNumberCancel")
    public void trackAuthCodeWrongNumberCancel() {
        modules.getAnalytics().trackAuthCodeWrongNumberCancel();
    }

    @ObjectiveCName("trackAuthCodeWrongNumberChange")
    public void trackAuthCodeWrongNumberChange() {
        modules.getAnalytics().trackAuthCodeWrongNumberChange();
    }

    @ObjectiveCName("trackAuthCodeOpen")
    public void trackAuthCodeOpen() {
        modules.getAnalytics().trackAuthCodeOpen();
    }

    @ObjectiveCName("trackAuthCodeClosed")
    public void trackAuthCodeClosed() {
        modules.getAnalytics().trackAuthCodeClosed();
    }

    // Auth signup

    @ObjectiveCName("trackAuthSignupOpen")
    public void trackAuthSignupOpen() {
        modules.getAnalytics().trackAuthSignupOpen();
    }

    @ObjectiveCName("trackAuthSignupClosed")
    public void trackAuthSignupClosed() {
        modules.getAnalytics().trackAuthSignupClosed();
    }

    @ObjectiveCName("trackAuthSignupNameTypeWithValue:")
    public void trackAuthSignupNameType(String newValue) {
        modules.getAnalytics().trackAuthSignupClosedNameType(newValue);
    }

    @ObjectiveCName("trackAuthSignupPressedAvatar")
    public void trackAuthSignupPressedAvatar() {
        modules.getAnalytics().trackAuthSignupPressedAvatar();
    }

    @ObjectiveCName("trackAuthSignupAvatarPicked")
    public void trackAuthSignupAvatarPicked() {
        modules.getAnalytics().trackAuthSignupAvatarPicked();
    }

    @ObjectiveCName("trackAuthSignupAvatarDeleted")
    public void trackAuthSignupAvatarDeleted() {
        modules.getAnalytics().trackAuthSignupAvatarDeleted();
    }

    @ObjectiveCName("trackAuthSignupAvatarCanelled")
    public void trackAuthSignupAvatarCanelled() {
        modules.getAnalytics().trackAuthSignupAvatarCanelled();
    }

    // Auth success

    @ObjectiveCName("trackAuthSuccess")
    public void trackAuthSuccess() {
        modules.getAnalytics().trackAuthSuccess();
    }

    // Main screens

    @ObjectiveCName("trackDialogsOpen")
    public void trackDialogsOpen() {
        modules.getAnalytics().trackDialogsOpen();
    }

    @ObjectiveCName("trackDialogsClosed")
    public void trackDialogsClosed() {
        modules.getAnalytics().trackDialogsClosed();
    }

    @ObjectiveCName("trackContactsOpen")
    public void trackContactsOpen() {
        modules.getAnalytics().trackContactsOpen();
    }

    @ObjectiveCName("trackContactsClosed")
    public void trackContactsClosed() {
        modules.getAnalytics().trackContactsClosed();
    }

    @ObjectiveCName("trackMainScreensOpen")
    public void trackMainScreensOpen() {
        modules.getAnalytics().trackMainScreensOpen();
    }

    @ObjectiveCName("trackMainScreensClosed")
    public void trackMainScreensClosed() {
        modules.getAnalytics().trackMainScreensClosed();
    }

    @ObjectiveCName("trackOwnProfileOpen")
    public void trackOwnProfileOpen() {
        modules.getAnalytics().trackOwnProfileOpen();
    }

    @ObjectiveCName("trackOwnProfileClosed")
    public void trackOwnProfileClosed() {
        modules.getAnalytics().trackOwnProfileClosed();
    }

    // Track message send

    @ObjectiveCName("trackTextSendWithPeer:")
    public void trackTextSend(Peer peer) {
        modules.getAnalytics().trackTextSend(peer);
    }

    @ObjectiveCName("trackPhotoSendWithPeer:")
    public void trackPhotoSend(Peer peer) {
        modules.getAnalytics().trackPhotoSend(peer);
    }

    @ObjectiveCName("trackVideoSendWithPeer:")
    public void trackVideoSend(Peer peer) {
        modules.getAnalytics().trackVideoSend(peer);
    }

    @ObjectiveCName("trackDocumentSendWithPeer:")
    public void trackDocumentSend(Peer peer) {
        modules.getAnalytics().trackDocumentSend(peer);
    }

    /**
     * Track sync action error
     *
     * @param action  action key
     * @param tag     error tag
     * @param message error message that shown to user
     */
    @ObjectiveCName("trackActionError:withTag:withMessage:")
    public void trackActionError(String action, String tag, String message) {
        modules.getAnalytics().trackActionError(action, tag, message);
    }

    /**
     * Track sync action success
     *
     * @param action action key
     */
    @ObjectiveCName("trackActionSuccess:")
    public void trackActionSuccess(String action) {
        modules.getAnalytics().trackActionSuccess(action);
    }

    /**
     * Track sync action try again
     *
     * @param action action key
     */
    @ObjectiveCName("trackActionTryAgain:")
    public void trackActionTryAgain(String action) {
        modules.getAnalytics().trackActionTryAgain(action);
    }

    /**
     * Track sync action cancel
     *
     * @param action action key
     */
    @ObjectiveCName("trackActionCancel:")
    public void trackActionCancel(String action) {
        modules.getAnalytics().trackActionCancel(action);
    }

    //////////////////////////////////////
    //        Tools and Tech
    //////////////////////////////////////

    /**
     * Formatting texts for UI
     *
     * @return formatter engine
     */
    @NotNull
    @ObjectiveCName("getFormatter")
    public I18nEngine getFormatter() {
        return modules.getI18nEngine();
    }

    /**
     * Register google push
     *
     * @param projectId GCM project id
     * @param token     GCM token
     */
    @ObjectiveCName("registerGooglePushWithProjectId:withToken:")
    public void registerGooglePush(long projectId, String token) {
        modules.getPushes().registerGooglePush(projectId, token);
    }

    /**
     * Register apple push
     *
     * @param apnsId internal APNS cert key
     * @param token  APNS token
     */
    @ObjectiveCName("registerApplePushWithApnsId:withToken:")
    public void registerApplePush(int apnsId, String token) {
        modules.getPushes().registerApplePush(apnsId, token);
    }

    /**
     * Get preferences storage
     *
     * @return the Preferences
     */
    @NotNull
    @ObjectiveCName("getPreferences")
    public PreferencesStorage getPreferences() {
        return modules.getPreferences();
    }


    /**
     * Executing external command
     *
     * @param request command request
     * @param <T>     return type
     * @return Command
     */
    @NotNull
    @ObjectiveCName("executeExternalCommand:")
    public <T extends Response> Command<T> executeExternalCommand(@NotNull Request<T> request) {
        return modules.getExternal().externalMethod(request);
    }
}