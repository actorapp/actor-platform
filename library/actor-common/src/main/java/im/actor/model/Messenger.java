/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

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
import im.actor.model.files.FileSystemReference;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ValueModel;
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
    public Messenger(Configuration configuration) {
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
    public Command<AuthState> requestSms(final long phone) {
        return modules.getAuthModule().requestSms(phone);
    }

    /**
     * Sending activation code
     *
     * @param code activation code
     * @return Command for execution
     */
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
    public Command<AuthState> signUp(final String name, String avatarPath, final boolean isSilent) {
        return modules.getAuthModule().signUp(name, avatarPath, isSilent);
    }

    /**
     * Get current Authentication phone.
     * Value is valid only for SIGN_UP or CODE_VALIDATION states.
     *
     * @return phone number in international format
     */
    public long getAuthPhone() {
        return modules.getAuthModule().getPhone();
    }

    /**
     * Resetting authentication process
     */
    public void resetAuth() {
        modules.getAuthModule().resetAuth();
    }

    //////////////////////////////////////
    //        Authenticated state
    //////////////////////////////////////

    /**
     * Get ViewModel of application state
     *
     * @return view model of application state
     */
    public AppStateVM getAppState() {
        return modules.getAppStateModule().getAppStateVM();
    }

    /**
     * Get authenticated User Id
     *
     * @return current User Id
     */
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
    public MVVMCollection<User, UserVM> getUsers() {
        if (modules.getUsersModule() == null) {
            return null;
        }
        return modules.getUsersModule().getUsersCollection();
    }

    /**
     * Get Group View Model Collection
     *
     * @return Group ViewModel Collection
     */
    public MVVMCollection<Group, GroupVM> getGroups() {
        if (modules.getGroupsModule() == null) {
            return null;
        }
        return modules.getGroupsModule().getGroupsCollection();
    }

    /**
     * Get private chat ViewModel
     *
     * @param uid chat's User Id
     * @return ValueModel of Boolean for typing state
     */
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
    public GroupAvatarVM getGroupAvatarVM(int gid) {
        return modules.getGroupsModule().getAvatarVM(gid);
    }


    //////////////////////////////////////
    //         Application events
    //////////////////////////////////////

    /**
     * MUST be called on app became visible
     */
    public void onAppVisible() {
        modules.onAppVisible();
    }

    /**
     * MUST be called on app became hidden
     */
    public void onAppHidden() {
        modules.onAppHidden();
    }

    /**
     * MUST be called on dialogs open
     */
    public void onDialogsOpen() {
        if (modules.getNotifications() != null) {
            modules.getNotifications().onDialogsOpen();
        }
    }

    /**
     * MUST be called on dialogs closed
     */
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
    public void onProfileClosed(int uid) {
        modules.getAnalytics().trackProfileClosed(uid);
    }

    /**
     * MUST be called on typing in chat.
     * Can be called with any frequency
     *
     * @param peer conversation's peer
     */
    public void onTyping(Peer peer) {
        modules.getTypingModule().onTyping(peer);
    }


    //////////////////////////////////////
    //         Technical events
    //////////////////////////////////////

    /**
     * MUST be called when phone book change detected
     */
    public void onPhoneBookChanged() {
        if (modules.getContactsModule() != null) {
            modules.getContactsModule().onPhoneBookChanged();
        }
    }

    /**
     * MUST be called when network status change detected
     */
    public void onNetworkChanged() {
        // TODO: Implement
    }

    /**
     * MUST be called when external push received
     *
     * @param seq sequence number of update
     */
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
    public void sendMessage(Peer peer, String text) {
        modules.getMessagesModule().sendMessage(peer, text);
    }

    /**
     * Send Photo message
     *
     * @param peer                destination peer
     * @param fileName            File name (without path)
     * @param w                   photo width
     * @param h                   photo height
     * @param fastThumb           Fast thumb of photo
     * @param fileSystemReference File System Reference
     */
    public void sendPhoto(Peer peer, String fileName,
                          int w, int h, FastThumb fastThumb,
                          FileSystemReference fileSystemReference) {
        modules.getMessagesModule().sendPhoto(peer, fileName, w, h, fastThumb, fileSystemReference);
    }

    /**
     * Send Video message
     *
     * @param peer                destination peer
     * @param fileName            File name (without path)
     * @param w                   video width
     * @param h                   video height
     * @param duration            video duration
     * @param fastThumb           Fast thumb of video
     * @param fileSystemReference File System Reference
     */
    public void sendVideo(Peer peer, String fileName, int w, int h, int duration,
                          FastThumb fastThumb, FileSystemReference fileSystemReference) {
        modules.getMessagesModule().sendVideo(peer, fileName, w, h, duration, fastThumb, fileSystemReference);
    }

    /**
     * Send document without preview
     *
     * @param peer                destination peer
     * @param fileName            File name (without path)
     * @param mimeType            mimetype of document
     * @param fileSystemReference File System Reference
     */
    public void sendDocument(Peer peer, String fileName, String mimeType, FileSystemReference fileSystemReference) {
        sendDocument(peer, fileName, mimeType, fileSystemReference, null);
    }

    /**
     * Send document with preview
     *
     * @param peer                destination peer
     * @param fileName            File name (without path)
     * @param mimeType            mimetype of document
     * @param fileSystemReference File System Reference
     * @param fastThumb           FastThumb of preview
     */
    public void sendDocument(Peer peer, String fileName, String mimeType, FileSystemReference fileSystemReference,
                             FastThumb fastThumb) {
        modules.getMessagesModule().sendDocument(peer, fileName, mimeType, fastThumb, fileSystemReference);
    }

    /**
     * Delete messages
     *
     * @param peer destination peer
     * @param rids rids of messages
     */
    public void deleteMessages(Peer peer, long[] rids) {
        modules.getMessagesModule().deleteMessages(peer, rids);
    }

    /**
     * Delete chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    public Command<Boolean> deleteChat(Peer peer) {
        return modules.getMessagesModule().deleteChat(peer);
    }

    /**
     * Clear chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    public Command<Boolean> clearChat(Peer peer) {
        return modules.getMessagesModule().clearChat(peer);
    }

    /**
     * Save message draft
     *
     * @param peer  destination peer
     * @param draft message draft
     */
    public void saveDraft(Peer peer, String draft) {
        modules.getMessagesModule().saveDraft(peer, draft);
    }

    /**
     * Load message draft
     *
     * @param peer destination peer
     * @return null if no draft available
     */
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
    public Command<Boolean> editMyName(final String newName) {
        return modules.getUsersModule().editMyName(newName);
    }

    /**
     * Change current user's avatar
     *
     * @param descriptor descriptor of avatar file
     */
    public void changeAvatar(String descriptor) {
        modules.getProfile().changeAvatar(descriptor);
    }

    /**
     * Remove current user's avatar
     */
    public void removeAvatar() {
        modules.getProfile().removeAvatar();
    }

    /**
     * Edit user's local name
     *
     * @param uid  user's id
     * @param name new user's local name
     * @return Command for execution
     */
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
    public Command<Boolean> editGroupTitle(final int gid, final String title) {
        return modules.getGroupsModule().editTitle(gid, title);
    }

    /**
     * Change group avatar
     *
     * @param gid        group's id
     * @param descriptor descriptor of avatar file
     */
    public void changeGroupAvatar(int gid, String descriptor) {
        modules.getGroupsModule().changeAvatar(gid, descriptor);
    }

    /**
     * Removing group avatar
     *
     * @param gid group's id
     */
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
    public Command<Integer> createGroup(String title, String avatarDescriptor, int[] uids) {
        return modules.getGroupsModule().createGroup(title, avatarDescriptor, uids);
    }


    /**
     * Leave group
     *
     * @param gid group's id
     * @return Command for execution
     */
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
    public Command<Boolean> addMemberToGroup(int gid, int uid) {
        return modules.getGroupsModule().addMemberToGroup(gid, uid);
    }

    /**
     * Kick member from group
     *
     * @param gid group's id
     * @param uid user's id
     * @return Command for execution
     */
    public Command<Boolean> kickMember(int gid, int uid) {
        return modules.getGroupsModule().kickMember(gid, uid);
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
    public Command<Boolean> removeContact(int uid) {
        return modules.getContactsModule().removeContact(uid);
    }

    /**
     * Add contact to contact's list
     *
     * @param uid user's id
     * @return Command for execution
     */
    public Command<Boolean> addContact(int uid) {
        return modules.getContactsModule().addContact(uid);
    }

    /**
     * Find Users
     *
     * @param query query for search
     * @return Command for execution
     */
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
    public void unbindRawFile(long fileId, boolean isAutoCancel, FileCallback callback) {
        modules.getFilesModule().unbindFile(fileId, callback, isAutoCancel);
    }

    /**
     * Raw Bind Upload File
     *
     * @param rid      randomId of uploading file
     * @param callback file state callback
     */
    public void bindRawUploadFile(long rid, UploadFileCallback callback) {
        modules.getFilesModule().bindUploadFile(rid, callback);
    }

    /**
     * Unbind Raw Upload File
     *
     * @param rid      randomId of uploading file
     * @param callback file state callback
     */
    public void unbindRawUploadFile(long rid, UploadFileCallback callback) {
        modules.getFilesModule().unbindUploadFile(rid, callback);
    }

    /**
     * Request file state
     *
     * @param fileId   file id
     * @param callback file state callback
     */
    public void requestState(long fileId, final FileCallback callback) {
        modules.getFilesModule().requestState(fileId, callback);
    }

    /**
     * Request upload file state
     *
     * @param rid      file's random id
     * @param callback file state callback
     */
    public void requestUploadState(long rid, UploadFileCallback callback) {
        modules.getFilesModule().requestUploadState(rid, callback);
    }

    /**
     * Cancel file download
     *
     * @param fileId file's id
     */
    public void cancelDownloading(long fileId) {
        modules.getFilesModule().cancelDownloading(fileId);
    }

    /**
     * Start file download
     *
     * @param location file's reference
     */
    public void startDownloading(FileReference location) {
        modules.getFilesModule().startDownloading(location);
    }

    /**
     * Resume upload
     *
     * @param rid file's random id
     */
    public void resumeUpload(long rid) {
        modules.getFilesModule().resumeUpload(rid);
    }

    /**
     * Pause upload
     *
     * @param rid file's random id
     */
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
    public boolean isConversationTonesEnabled() {
        return modules.getSettings().isConversationTonesEnabled();
    }

    /**
     * Change conversation tones enabled value
     *
     * @param val is conversation tones enabled
     */
    public void changeConversationTonesEnabled(boolean val) {
        modules.getSettings().changeConversationTonesEnabled(val);
    }

    /**
     * Is notifications enabled setting
     *
     * @return is notifications enabled
     */
    public boolean isNotificationsEnabled() {
        return modules.getSettings().isNotificationsEnabled();
    }

    /**
     * Change notifications enabled value
     *
     * @param val is notifications enabled
     */
    public void changeNotificationsEnabled(boolean val) {
        modules.getSettings().changeNotificationsEnabled(val);
    }

    /**
     * Is notifications sounds enabled
     *
     * @return is notification sounds enabled
     */
    public boolean isNotificationSoundEnabled() {
        return modules.getSettings().isNotificationSoundEnabled();
    }

    /**
     * Change notification sounds enabled
     *
     * @param val is notification sounds enabled
     */
    public void changeNotificationSoundEnabled(boolean val) {
        modules.getSettings().changeNotificationSoundEnabled(val);
    }

    /**
     * Sound that used for notifications
     *
     * @return notification sound name
     */
    public String getNotificationSound() {
        return modules.getSettings().getNotificationSound();
    }

    /**
     * Change sound that used for notifications
     *
     * @param sound notification sound name
     */
    public void changeNotificationSound(String sound) {
        modules.getSettings().changeNotificationSound(sound);
    }

    /**
     * Is notification vibration enabled
     *
     * @return is notification vibration enabled
     */
    public boolean isNotificationVibrationEnabled() {
        return modules.getSettings().isVibrationEnabled();
    }

    /**
     * Change notification vibration enabled
     *
     * @param val is notification vibration enabled
     */
    public void changeNotificationVibrationEnabled(boolean val) {
        modules.getSettings().changeNotificationVibrationEnabled(val);
    }

    /**
     * Is displaying text in notifications enabled
     *
     * @return is displaying text in notifications enabled
     */
    public boolean isShowNotificationsText() {
        return modules.getSettings().isShowNotificationsText();
    }

    /**
     * Change displaying text in notifications enabled
     *
     * @param val is displaying text in notifications enabled
     */
    public void changeShowNotificationTextEnabled(boolean val) {
        modules.getSettings().changeShowNotificationTextEnabled(val);
    }

    /**
     * Is send by enter enabled. Useful for android and web.
     *
     * @return is send by enter enabled
     */
    public boolean isSendByEnterEnabled() {
        return modules.getSettings().isSendByEnterEnabled();
    }

    /**
     * Change if send by enter enabled
     *
     * @param val is send by enter enabled
     */
    public void changeSendByEnter(boolean val) {
        modules.getSettings().changeSendByEnter(val);
    }

    /**
     * Is notifications enabled for peer
     *
     * @param peer destination peer
     * @return is notifications enabled
     */
    public boolean isNotificationsEnabled(Peer peer) {
        return modules.getSettings().isNotificationsEnabled(peer);
    }

    /**
     * Change if notifications enabled for peer
     *
     * @param peer destination peer
     * @param val  is notifications enabled
     */
    public void changeNotificationsEnabled(Peer peer, boolean val) {
        modules.getSettings().changeNotificationsEnabled(peer, val);
    }

    /**
     * Is in-app notifications enabled
     *
     * @return is notifications enabled
     */
    public boolean isInAppNotificationsEnabled() {
        return modules.getSettings().isInAppEnabled();
    }

    /**
     * Change in-app notifications enable value
     *
     * @param val is notifications enabled
     */
    public void changeInAppNotificationsEnabled(boolean val) {
        modules.getSettings().changeInAppEnabled(val);
    }

    /**
     * Is in-app notifications sound enabled
     *
     * @return is notifications sound enabled
     */
    public boolean isInAppNotificationSoundEnabled() {
        return modules.getSettings().isInAppSoundEnabled();
    }

    /**
     * Change in-app notifications sound enabled value
     *
     * @param val is notifications sound enabled
     */
    public void changeInAppNotificationSoundEnabled(boolean val) {
        modules.getSettings().changeInAppSoundEnabled(val);
    }

    /**
     * Is in-app notification vibration enabled
     *
     * @return is notifications vibration enabled
     */
    public boolean isInAppNotificationVibrationEnabled() {
        return modules.getSettings().isInAppVibrationEnabled();
    }

    /**
     * Change in-app notifications vibration enabled value
     *
     * @param val is notifications vibration enabled
     */
    public void changeInAppNotificationVibrationEnabled(boolean val) {
        modules.getSettings().changeInAppVibrationEnabled(val);
    }

    //////////////////////////////////////
    //            Security
    //////////////////////////////////////

    /**
     * Loading active sessions
     *
     * @return Command for execution
     */
    public Command<List<AuthSession>> loadSessions() {
        return modules.getSecurity().loadSessions();
    }

    /**
     * Terminate all other sessions
     *
     * @return Command for execution
     */
    public Command<Boolean> terminateAllSessions() {
        return modules.getSecurity().terminateAllSessions();
    }

    /**
     * Terminate active session
     *
     * @param id session id
     * @return Command for execution
     */
    public Command<Boolean> terminateSession(int id) {
        return modules.getSecurity().terminateSession(id);
    }

    //////////////////////////////////////
    //         User Tracking
    //////////////////////////////////////

    /**
     * Track phone number authentication screen
     */
    public void trackAuthPhoneOpen() {
        modules.getAnalytics().trackAuthPhoneOpen();
    }

    /**
     * Track pick country open
     */
    public void trackAuthCountryOpen() {
        modules.getAnalytics().trackAuthCountryOpen();
    }

    /**
     * Track pick country closed
     */
    public void trackAuthCountryClosed() {
        modules.getAnalytics().trackAuthCountryClosed();
    }

    /**
     * Track country picked
     */
    public void trackAuthCountryPicked(String country) {
        modules.getAnalytics().trackAuthCountryPicked(country);
    }

    /**
     * Track auth phone typing
     */
    public void trackAuthPhoneType(String newValue) {
        modules.getAnalytics().trackAuthPhoneType(newValue);
    }

    /**
     * Tack opening why screen
     */
    public void trackAuthPhoneInfoOpen() {
        modules.getAnalytics().trackAuthPhoneInfoOpen();
    }

    /**
     * Track request code tap
     */
    public void trackCodeRequest() {
        modules.getAnalytics().trackCodeRequest();
    }

    public void trackAuthCodeType(String newValue) {
        modules.getAnalytics().trackAuthCodeType(newValue);
    }

    public void trackBackPressed() {
        modules.getAnalytics().trackBackPressed();
    }

    public void trackUpPressed() {
        modules.getAnalytics().trackUpPressed();
    }

    public void trackAuthCodeWrongNumber() {
        modules.getAnalytics().trackAuthCodeWrongNumber();
    }

    public void trackAuthCodeWrongNumberCancel() {
        modules.getAnalytics().trackAuthCodeWrongNumberCancel();
    }

    public void trackAuthCodeWrongNumberChange() {
        modules.getAnalytics().trackAuthCodeWrongNumberChange();
    }

    public void trackAuthCodeOpen() {
        modules.getAnalytics().trackAuthCodeOpen();
    }

    public void trackAuthCodeClosed() {
        modules.getAnalytics().trackAuthCodeClosed();
    }

    // Auth signup

    public void trackAuthSignupOpen() {
        modules.getAnalytics().trackAuthSignupOpen();
    }

    public void trackAuthSignupClosed() {
        modules.getAnalytics().trackAuthSignupClosed();
    }

    public void trackAuthSignupClosedNameType(String newValue) {
        modules.getAnalytics().trackAuthSignupClosedNameType(newValue);
    }

    public void trackAuthSignupPressedAvatar() {
        modules.getAnalytics().trackAuthSignupPressedAvatar();
    }

    public void trackAuthSignupAvatarPicked() {
        modules.getAnalytics().trackAuthSignupAvatarPicked();
    }

    public void trackAuthSignupAvatarDeleted() {
        modules.getAnalytics().trackAuthSignupAvatarDeleted();
    }

    public void trackAuthSignupAvatarCanelled() {
        modules.getAnalytics().trackAuthSignupAvatarCanelled();
    }

    // Auth success

    public void trackAuthSuccess() {
        modules.getAnalytics().trackAuthSuccess();
    }

    // Main screens

    public void trackDialogsOpen() {
        modules.getAnalytics().trackDialogsOpen();
    }

    public void trackDialogsClosed() {
        modules.getAnalytics().trackDialogsClosed();
    }

    public void trackContactsOpen() {
        modules.getAnalytics().trackContactsOpen();
    }

    public void trackContactsClosed() {
        modules.getAnalytics().trackContactsClosed();
    }

    public void trackMainScreensOpen() {
        modules.getAnalytics().trackMainScreensOpen();
    }

    public void trackMainScreensClosed() {
        modules.getAnalytics().trackMainScreensClosed();
    }

    public void trackOwnProfileOpen() {
        modules.getAnalytics().trackOwnProfileOpen();
    }

    public void trackOwnProfileClosed() {
        modules.getAnalytics().trackOwnProfileClosed();
    }

    // Track message send

    public void trackTextSend(Peer peer) {
        modules.getAnalytics().trackTextSend(peer);
    }

    public void trackPhotoSend(Peer peer) {
        modules.getAnalytics().trackPhotoSend(peer);
    }

    public void trackVideoSend(Peer peer) {
        modules.getAnalytics().trackVideoSend(peer);
    }

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
    public void trackActionError(String action, String tag, String message) {
        modules.getAnalytics().trackActionError(action, tag, message);
    }

    /**
     * Track sync action success
     *
     * @param action action key
     */
    public void trackActionSuccess(String action) {
        modules.getAnalytics().trackActionSuccess(action);
    }

    /**
     * Track sync action try again
     *
     * @param action action key
     */
    public void trackActionTryAgain(String action) {
        modules.getAnalytics().trackActionTryAgain(action);
    }

    /**
     * Track sync action cancel
     *
     * @param action action key
     */
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
    public I18nEngine getFormatter() {
        return modules.getI18nEngine();
    }

    /**
     * Register google push
     *
     * @param projectId GCM project id
     * @param token     GCM token
     */
    public void registerGooglePush(long projectId, String token) {
        modules.getPushes().registerGooglePush(projectId, token);
    }

    /**
     * Register apple push
     *
     * @param apnsId internal APNS cert key
     * @param token  APNS token
     */
    public void registerApplePush(int apnsId, String token) {
        modules.getPushes().registerApplePush(apnsId, token);
    }

    /**
     * Get preferences storage
     *
     * @return the Preferences
     */
    public PreferencesStorage getPreferences() {
        return modules.getPreferences();
    }
}