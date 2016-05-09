/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiRawValue;
import im.actor.core.api.ApiSex;
import im.actor.core.api.ApiAuthSession;
import im.actor.core.api.rpc.ResponseRawRequest;
import im.actor.core.api.rpc.ResponseSeqDate;
import im.actor.core.entity.AuthCodeRes;
import im.actor.core.entity.AuthRes;
import im.actor.core.entity.AuthStartRes;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.Group;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.MessageSearchEntity;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerSearchType;
import im.actor.core.entity.PhoneBookContact;
import im.actor.core.entity.PublicGroup;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.entity.WebActionDescriptor;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.Sticker;
import im.actor.core.events.PeerChatPreload;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.Modules;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.DialogsClosed;
import im.actor.core.events.DialogsOpened;
import im.actor.core.events.PeerChatClosed;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.events.PeerInfoClosed;
import im.actor.core.events.PeerInfoOpened;
import im.actor.core.events.UserVisible;
import im.actor.core.network.NetworkState;
import im.actor.core.util.ActorTrace;
import im.actor.core.util.Timing;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.core.viewmodel.DialogGroupsVM;
import im.actor.core.viewmodel.FileCallback;
import im.actor.core.viewmodel.FileEventCallback;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.GlobalStateVM;
import im.actor.core.viewmodel.GroupAvatarVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.OwnAvatarVM;
import im.actor.core.viewmodel.StickersVM;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.core.viewmodel.UploadFileVMCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.PreferencesStorage;
import im.actor.runtime.threading.SimpleDispatcher;
import im.actor.runtime.threading.ThreadDispatcher;

/**
 * Entry point to Actor Messaging
 * Before using Messenger you need to create Configuration object by using ConfigurationBuilder.
 */
public class Messenger {

    // Do Not Remove! WorkAround for missing j2objc translator include
    private static final Void DUMB = null;

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

        // Start Messenger initialization
        // Timing timing = new Timing("MESSENGER_INIT");

        // Actor system
        // timing.section("Actors");
        ActorSystem.system().setTraceInterface(new ActorTrace());
        ActorSystem.system().addDispatcher("network_manager", 1);
        ActorSystem.system().addDispatcher("heavy", 2);

        // Configure dispatcher
        // timing.section("Dispatcher");
//        if (!Runtime.isMainThread()) {
//            throw new RuntimeException("Messenger need to be created on Main Thread!");
//        }
        // ThreadDispatcher.pushDispatcher(Runtime::postToMainThread);

        // timing.section("Modules:Create");
        this.modules = new Modules(this, configuration);

        // timing.section("Modules:Run");
        this.modules.run();

        // timing.end();
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
    @Deprecated
    public AuthState getAuthState() {
        return modules.getAuthModule().getAuthState();
    }

    /**
     * Convenience method for checking if user logged in
     *
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return modules.getAuthModule().isLoggedIn();
    }

    /**
     * Starting email auth
     *
     * @param email email for authentication
     * @return promise of AuthStartRes
     */
    @NotNull
    @ObjectiveCName("doStartAuthWithEmail:")
    public Promise<AuthStartRes> doStartEmailAuth(String email) {
        return modules.getAuthModule().doStartEmailAuth(email);
    }

    /**
     * Starting phone auth
     *
     * @param phone phone for authentication
     * @return promise of AuthStartRes
     */
    @NotNull
    @ObjectiveCName("doStartAuthWithPhone:")
    public Promise<AuthStartRes> doStartPhoneAuth(long phone) {
        return modules.getAuthModule().doStartPhoneAuth(phone);
    }

    /**
     * Validating Confirmation Code
     *
     * @param code            code
     * @param transactionHash transaction hash
     * @return promise of AuthCodeRes
     */
    @NotNull
    @ObjectiveCName("doValidateCode:withTransaction:")
    public Promise<AuthCodeRes> doValidateCode(String code, String transactionHash) {
        return modules.getAuthModule().doValidateCode(transactionHash, code);
    }


    /**
     * Sending activation code via voice
     *
     * @param transactionHash transaction hash
     * @return promice of Boolean
     */
    @NotNull
    @ObjectiveCName("doSendCodeViaCall:")
    public Promise<Boolean> doSendCodeViaCall(String transactionHash) {
        return modules.getAuthModule().doSendCall(transactionHash);
    }

    /**
     * Signing Up
     *
     * @param name            name
     * @param sex             sex of user
     * @param transactionHash transaction hash
     * @return promise of AuthRes
     */
    @NotNull
    @ObjectiveCName("doSignupWithName:withSex:withTransaction:")
    public Promise<AuthRes> doSignup(String name, Sex sex, String transactionHash) {
        return modules.getAuthModule().doSignup(name, sex, transactionHash);
    }

    /**
     * Complete Authentication
     *
     * @param authRes authentication result for commiting
     * @return promise of Boolean
     */
    @NotNull
    @ObjectiveCName("doCompleteAuth:")
    public Promise<Boolean> doCompleteAuth(AuthRes authRes) {
        return modules.getAuthModule().doCompleteAuth(authRes);
    }

    /**
     * Request email auth
     *
     * @param email email to authenticate
     * @return Command for execution
     */
    @NotNull
    @Deprecated
    @ObjectiveCName("requestStartAuthCommandWithEmail:")
    public Command<AuthState> requestStartEmailAuth(final String email) {
        return modules.getAuthModule().requestStartEmailAuth(email);
    }

    /**
     * Request phone auth
     *
     * @param phone phone to authenticate
     * @return Command for execution
     */
    @NotNull
    @Deprecated
    @ObjectiveCName("requestStartAuthCommandWithPhone:")
    public Command<AuthState> requestStartPhoneAuth(final long phone) {
        return modules.getAuthModule().requestStartPhoneAuth(phone);
    }

    /**
     * Request user name anonymous auth
     *
     * @param userName userName to authenticate
     * @return Command for execution
     */
    @NotNull
    @Deprecated
    @ObjectiveCName("requestStartAnonymousAuthWithUserName:")
    public Command<AuthState> requestStartAnonymousAuth(String userName) {
        return modules.getAuthModule().requestStartAnonymousAuth(userName);
    }

    /**
     * Request user name auth
     *
     * @param userName userName to authenticate
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("requestStartAuthCommandWithUserName:")
    public Command<AuthState> requestStartUserNameAuth(String userName) {
        return modules.getAuthModule().requestStartUserNameAuth(userName);
    }

    /**
     * Request OAuth params
     *
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("requestGetOAuthParamsCommand")
    public Command<AuthState> requestGetOAuthParams() {
        return modules.getAuthModule().requestGetOAuth2Params();
    }

    /**
     * Request complete OAuth
     *
     * @param code code from oauth
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("requestCompleteOAuthCommandWithCode:")
    public Command<AuthState> requestCompleteOAuth(String code) {
        return modules.getAuthModule().requestCompleteOauth(code);
    }

    /**
     * Request phone call activation
     *
     * @return command for execution
     */
    @NotNull
    @ObjectiveCName("requestPhoneCall")
    public Command<Boolean> requestPhoneCall() {
        return modules.getAuthModule().requestCallActivation();
    }

    /**
     * Sending activation code
     *
     * @param code activation code
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("validateCodeCommand:")
    public Command<AuthState> validateCode(final String code) {
        return modules.getAuthModule().requestValidateCode(code);
    }

    /**
     * Sending password
     *
     * @param password Account password
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("validatePasswordCommand:")
    public Command<AuthState> validatePassword(String password) {
        return modules.getAuthModule().requestValidatePassword(password);
    }

    /**
     * Perform signup
     *
     * @param name       Name of User
     * @param sex        user sex
     * @param avatarPath File descriptor of avatar (may be null if not set)
     * @return Comand for execution
     */
    @NotNull
    @ObjectiveCName("signUpCommandWithName:WithSex:withAvatar:")
    public Command<AuthState> signUp(String name, Sex sex, String avatarPath) {
        return modules.getAuthModule().signUp(name, ApiSex.UNKNOWN, avatarPath);
    }

    /**
     * Get current Authentication phone.
     * Value is valid only for SIGN_UP or CODE_VALIDATION_PHONE states.
     *
     * @return phone number in international format
     */
    @ObjectiveCName("getAuthPhone")
    @Deprecated
    public long getAuthPhone() {
        return modules.getAuthModule().getPhone();
    }

    /**
     * Get current Authentication email.
     * Value is valid only for SIGN_UP or CODE_VALIDATION_EMAIL states.
     *
     * @return email
     */
    @ObjectiveCName("getAuthEmail")
    @Deprecated
    public String getAuthEmail() {
        return modules.getAuthModule().getEmail();
    }

    /**
     * Resetting authentication process
     */
    @ObjectiveCName("resetAuth")
    @Deprecated
    public void resetAuth() {
        modules.getAuthModule().resetAuth();
    }

    /**
     * This method is called when messenger was logged in. Useful for subclasses
     */
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
     * Get ViewModel of global application state
     *
     * @return view model of application state
     */
    @NotNull
    @ObjectiveCName("getGlobalState")
    public GlobalStateVM getGlobalState() {
        return modules.getAppStateModule().getGlobalStateVM();
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
        return modules.getUsersModule().getUsers();
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
        return getGroups().get(gid);
    }

    /**
     * Get Dialog Groups
     *
     * @return dialog groups
     */
    @NotNull
    @ObjectiveCName("getDialogGroupsVM")
    public DialogGroupsVM getDialogGroupsVM() {
        return modules.getMessagesModule().getDialogGroupsVM();
    }

    /**
     * Get private chat ViewModel
     *
     * @param uid chat's User Id
     * @return ValueModel of Boolean for typing state
     */
    @NotNull
    @ObjectiveCName("getTypingWithUid:")
    public ValueModel<Boolean> getTyping(int uid) {
        return modules.getTypingModule().getTyping(uid).getTyping();
    }

    /**
     * Get group chat ViewModel
     *
     * @param gid chat's Group Id
     * @return ValueModel of int[] for typing state
     */
    @NotNull
    @ObjectiveCName("getGroupTypingWithGid:")
    public ValueModel<int[]> getGroupTyping(int gid) {
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
        return modules.getProfileModule().getOwnAvatarVM();
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
        modules.getEvents().postSticky(new AppVisibleChanged(true));
    }

    /**
     * MUST be called on app became hidden
     */
    @ObjectiveCName("onAppHidden")
    public void onAppHidden() {
        modules.getEvents().postSticky(new AppVisibleChanged(false));
    }

    /**
     * MUST be called on dialogs open
     */
    @ObjectiveCName("onDialogsOpen")
    public void onDialogsOpen() {
        modules.getEvents().post(new DialogsOpened());
    }

    /**
     * MUST be called on dialogs closed
     */
    @ObjectiveCName("onDialogsClosed")
    public void onDialogsClosed() {
        modules.getEvents().post(new DialogsClosed());
    }

    /**
     * MUST be called on conversation open
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onConversationOpenWithPeer:")
    public void onConversationOpen(@NotNull Peer peer) {
        modules.getEvents().post(new PeerChatOpened(peer));
    }

    /**
     * Can be called for forcing conversation loading in background
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onConversationPreLoadWithPeer:")
    public void onConversationPreLoad(@NotNull Peer peer) {
        modules.getEvents().post(new PeerChatPreload(peer));
    }

    /**
     * MUST be called on conversation closed
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onConversationClosedWithPeer:")
    public void onConversationClosed(@NotNull Peer peer) {
        modules.getEvents().post(new PeerChatClosed(peer));
    }

    /**
     * MUST be called on profile open
     *
     * @param uid user's Id
     */
    @ObjectiveCName("onProfileOpenWithUid:")
    public void onProfileOpen(int uid) {
        modules.getEvents().post(new PeerInfoOpened(Peer.user(uid)));
    }

    /**
     * Fire event when user object became visible
     *
     * @param uid user's Id
     */
    @ObjectiveCName("onUserVisibleWithUid:")
    public void onUserVisible(int uid) {
        modules.getEvents().post(new UserVisible(uid));
    }

    /**
     * MUST be called on profile closed
     *
     * @param uid user's Id
     */
    @ObjectiveCName("onProfileClosedWithUid:")
    public void onProfileClosed(int uid) {
        modules.getEvents().post(new PeerInfoClosed(Peer.user(uid)));
    }

    /**
     * MUST be called on typing in chat.
     * Can be called with any frequency
     *
     * @param peer conversation's peer
     */
    @ObjectiveCName("onTypingWithPeer:")
    public void onTyping(@NotNull Peer peer) {
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
     *
     * @param state New network state
     */
    @ObjectiveCName("onNetworkChanged:")
    public void onNetworkChanged(@NotNull NetworkState state) {
        modules.getActorApi().onNetworkChanged(state);
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
     * Getting Conversation VM
     *
     * @param peer peer
     * @return Conversation VM
     */
    @NotNull
    @ObjectiveCName("getConversationVM")
    public ConversationVM getConversationVM(Peer peer) {
        return modules.getMessagesModule().getConversationVM(peer);
    }


    /**
     * Getting Available Stickers VM
     *
     * @return Stickers VM
     */
    @NotNull
    @ObjectiveCName("getAvailableStickersVM")
    public StickersVM getAvailableStickersVM() {
        return modules.getStickersModule().getStickersVM();
    }

    /**
     * Send Markdown Message with mentions
     *
     * @param peer         destination peer
     * @param text         message text
     * @param markDownText message markdown text
     * @param mentions     user's mentions
     */
    @ObjectiveCName("sendMessageWithPeer:withText:withMarkdownText:withMentions:autoDetect:")
    public void sendMessage(@NotNull Peer peer, @NotNull String text, @Nullable String markDownText,
                            @Nullable ArrayList<Integer> mentions, boolean autoDetect) {
        modules.getMessagesModule().sendMessage(peer, text, markDownText, mentions, autoDetect);
    }

    /**
     * Update Message
     *
     * @param peer destination peer
     * @param text message text
     * @param rid  message rundom id
     */
    @ObjectiveCName("updateMessageWithPeer:withText:withRid:")
    public Command<Void> updateMessage(@NotNull Peer peer, @NotNull String text, long rid) {
        return callback -> modules.getMessagesModule().updateMessage(peer, text, rid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }


    /**
     * Send Markdown Message with mentions
     *
     * @param peer        destination peer
     * @param name        contact name
     * @param phones      contact phones
     * @param emails      contact emails
     * @param base64photo contact photo
     */
    @ObjectiveCName("sendContactWithPeer:withName:withPhones:withEmails:withPhoto:")
    public void sendContact(@NotNull Peer peer, @NotNull String name, @NotNull ArrayList<String> phones,
                            @NotNull ArrayList<String> emails, @Nullable String base64photo) {
        modules.getMessagesModule().sendContact(peer, name, phones, emails, base64photo);
    }

    /**
     * Send Markdown Message with mentions
     *
     * @param peer         destination peer
     * @param text         message text
     * @param markDownText message markdown text
     * @param mentions     user's mentions
     */
    @ObjectiveCName("sendMessageWithPeer:withText:withMarkdownText:withMentions:")
    public void sendMessage(@NotNull Peer peer, @NotNull String text, @Nullable String markDownText,
                            @Nullable ArrayList<Integer> mentions) {
        modules.getMessagesModule().sendMessage(peer, text, markDownText, mentions, false);
    }

    /**
     * Send Markdown Message
     *
     * @param peer         destination peer
     * @param text         message text
     * @param markDownText message markdown text
     */
    @ObjectiveCName("sendMessageWithPeer:withText:withMarkdownText:")
    public void sendMessage(@NotNull Peer peer, @NotNull String text, @Nullable String markDownText) {
        sendMessage(peer, text, markDownText, null, false);
    }

    /**
     * Send Text Message with mentions
     *
     * @param peer     destination peer
     * @param text     message text
     * @param mentions user's mentions
     */
    @ObjectiveCName("sendMessageWithPeer:withText:withMentions:")
    public void sendMessage(@NotNull Peer peer, @NotNull String text, @Nullable ArrayList<Integer> mentions) {
        sendMessage(peer, text, null, mentions, false);
    }

    /**
     * Send Text Message
     *
     * @param peer destination peer
     * @param text message text
     */
    @ObjectiveCName("sendMessageWithPeer:withText:")
    public void sendMessage(@NotNull Peer peer, @NotNull String text) {
        sendMessage(peer, text, null, null, true);
    }

    /**
     * Send Text Message
     *
     * @param peer destination peer
     * @param text message text
     */
    @ObjectiveCName("sendMessageWithMentionsDetect:withText:")
    public void sendMessageWithMentionsDetect(@NotNull Peer peer, @NotNull String text) {
        sendMessage(peer, text, null, null, true);
    }

    /**
     * Send Text Message
     *
     * @param peer destination peer
     * @param text message text
     */
    @ObjectiveCName("sendMessageWithMentionsDetect:withText:withMarkdownText:")
    public void sendMessageWithMentionsDetect(@NotNull Peer peer, @NotNull String text, @NotNull String markdownText) {
        sendMessage(peer, text, markdownText, null, true);
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
    public void sendPhoto(@NotNull Peer peer, @NotNull String fileName,
                          int w, int h, @Nullable FastThumb fastThumb,
                          @NotNull String descriptor) {
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
     * Send Audio message
     *
     * @param peer       destination peer
     * @param duration   audio duration
     * @param descriptor File Descriptor
     */
    @ObjectiveCName("sendAudioWithPeer:withName:withDuration:withDescriptor:")
    public void sendAudio(@NotNull Peer peer, @NotNull String fileName,
                          int duration, @NotNull String descriptor) {
        modules.getMessagesModule().sendAudio(peer, fileName, duration, descriptor);
    }

    /**
     * Send Location Message
     *
     * @param peer      destination peer
     * @param longitude user location longitude
     * @param latitude  user location latitude
     * @param street    user location street
     * @param place     user location place
     */
    @ObjectiveCName("sendLocationWithPeer:withLongitude:withLatitude:withStreet:withPlace:")
    public void sendLocation(@NotNull Peer peer,
                             @NotNull Double longitude, @NotNull Double latitude,
                             @Nullable String street, @Nullable String place) {
        modules.getMessagesModule().sendLocation(peer, longitude, latitude, street, place);
    }

    /**
     * Send json message
     *
     * @param peer    destination peer
     * @param content json content
     */
    @ObjectiveCName("sendCustomJsonMessageWithPeer:withJson:")
    public void sendCustomJsonMessage(@NotNull Peer peer, @NotNull JsonContent content) {
        modules.getMessagesModule().sendJson(peer, content);
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
     * Send DocumentContent - used for forwarding
     *
     * @param peer    destination peer
     * @param content content to forward
     */
    @ObjectiveCName("forwardContentContentWithPeer:withContent:")
    public void forwardContent(Peer peer, AbsContent content) {
        modules.getMessagesModule().forwardContent(peer, content);
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
    public Command<Void> deleteChat(Peer peer) {
        return callback -> modules.getMessagesModule().deleteChat(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Send document without preview
     *
     * @param peer    destination peer
     * @param sticker sticker to send
     */
    @ObjectiveCName("sendStickerWithPeer:withSticker:")
    public void sendSticker(Peer peer, Sticker sticker) {
        modules.getMessagesModule().sendSticker(peer, sticker);
    }

    /**
     * Clear chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("clearChatCommandWithPeer:")
    public Command<Void> clearChat(Peer peer) {
        return callback -> modules.getMessagesModule().clearChat(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Archive chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("archiveChatCommandWithPeer:")
    public Command<Void> archiveChat(Peer peer) {
        return callback -> modules.getMessagesModule().archiveChat(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Favouriting chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("favouriteChatCommandWithPeer:")
    public Command<Void> favouriteChat(Peer peer) {
        return callback -> modules.getMessagesModule().favoriteChat(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Unfavouriting chat
     *
     * @param peer destination peer
     * @return Command for execution
     */
    @ObjectiveCName("unfavouriteChatCommandWithPeer:")
    public Command<Void> unfavoriteChat(Peer peer) {
        return callback -> modules.getMessagesModule().unfavoriteChat(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Adding reaction to a message
     *
     * @param peer destination peer
     * @param rid  random id of message
     * @param code reaction code
     * @return Command for execution
     */
    @ObjectiveCName("addReactionWithPeer:withRid:withCode:")
    public Command<Void> addReaction(Peer peer, long rid, String code) {
        return callback -> modules.getMessagesModule().addReaction(peer, rid, code)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Removing reaction to a message
     *
     * @param peer destination peer
     * @param rid  random id of message
     * @param code reaction code
     * @return Command for execution
     */
    @ObjectiveCName("removeReactionWithPeer:withRid:withCode:")
    public Command<Void> removeReaction(Peer peer, long rid, String code) {
        return callback -> modules.getMessagesModule().removeReaction(peer, rid, code)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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

    /**
     * Loading last read messages
     *
     * @param peer destination peer
     * @return rid of last read message
     */
    @ObjectiveCName("loadFirstUnread:")
    @Deprecated
    public long loadFirstUnread(Peer peer) {
        return getConversationVM(peer).getOwnReadDate().get();
    }

    /**
     * Finding suitable mentions
     *
     * @param gid   gid of group
     * @param query query for search
     * @return matches
     */
    @ObjectiveCName("findMentionsWithGid:withQuery:")
    public List<MentionFilterResult> findMentions(int gid, String query) {
        return modules.getMentions().findMentions(gid, query);
    }

    /**
     * Finding peers by type
     *
     * @param type type of peer
     * @return found peers
     */
    @ObjectiveCName("findPeersWithType:")
    public Command<List<PeerSearchEntity>> findPeers(PeerSearchType type) {
        return callback -> modules.getSearchModule().findPeers(type)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Finding text messages by query
     *
     * @param peer  peer for search
     * @param query query for search
     * @return found messages
     */
    @ObjectiveCName("findTextMessagesWithPeer:withQuery:")
    public Command<List<MessageSearchEntity>> findTextMessages(Peer peer, String query) {
        return callback -> modules.getSearchModule().findTextMessages(peer, query)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Finding all doc messages
     *
     * @param peer peer for search
     * @return found messages
     */
    @ObjectiveCName("findAllDocsWithPeer:")
    public Command<List<MessageSearchEntity>> findAllDocs(Peer peer) {
        return callback -> modules.getSearchModule().findAllDocs(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Finding all messages with links
     *
     * @param peer peer for search
     * @return found messages
     */
    @ObjectiveCName("findAllLinksWithPeer:")
    public Command<List<MessageSearchEntity>> findAllLinks(Peer peer) {
        return callback -> modules.getSearchModule().findAllLinks(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Finding all messages with photos
     *
     * @param peer peer for search
     * @return found messages
     */
    @ObjectiveCName("findAllPhotosWithPeer:")
    public Command<List<MessageSearchEntity>> findAllPhotos(Peer peer) {
        return callback -> modules.getSearchModule().findAllPhotos(peer)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    //////////////////////////////////////
    //             Calls
    //////////////////////////////////////

    /**
     * Calling to user
     *
     * @param uid user you want to call
     * @return command to execute
     */
    @ObjectiveCName("doCallWithUid:")
    public Command<Long> doCall(int uid) {
        return modules.getCallsModule().makeCall(Peer.user(uid));
    }

    /**
     * Starting new group call
     *
     * @param gid group you want to call
     * @return command to execute
     */
    @ObjectiveCName("doCallWithGid:")
    public Command<Long> doGroupCall(int gid) {
        return modules.getCallsModule().makeCall(Peer.group(gid));
    }

    /**
     * Ending call by call id
     *
     * @param callId Call id
     */
    @ObjectiveCName("endCallWithCallId:")
    public void endCall(long callId) {
        modules.getCallsModule().endCall(callId);
    }

    /**
     * Toggle muting of call
     *
     * @param callId Call Id
     */
    @ObjectiveCName("toggleCallMuteWithCallId:")
    public void toggleCallMute(long callId) {
        if (modules.getCallsModule().getCall(callId).getIsMuted().get()) {
            modules.getCallsModule().unmuteCall(callId);
        } else {
            modules.getCallsModule().muteCall(callId);
        }
    }

    /**
     * Answer call by call id
     *
     * @param callId Call id
     */
    @ObjectiveCName("answerCallWithCallId:")
    public void answerCall(long callId) {
        modules.getCallsModule().answerCall(callId);
    }

    /**
     * Get Call View Model by call id
     *
     * @param callId Call id
     * @return Call view model
     */
    @ObjectiveCName("getCallWithCallId:")
    public CallVM getCall(long callId) {
        return modules.getCallsModule().getCall(callId);
    }

    /**
     * Call this method when user is pobabbly want to end call. For example when power button
     * was pressed on iOS device
     */
    @ObjectiveCName("probablyEndCall")
    public void probablyEndCall() {
        if (modules.getCallsModule() != null) {
            modules.getCallsModule().probablyEndCall();
        }
    }

    /**
     * Checking incoming call from push notification
     *
     * @param callId  Call Id
     * @param attempt Call Attempt
     */
    @ObjectiveCName("checkCall:withAttempt:")
    public void checkCall(long callId, int attempt) {
        if (modules.getCallsModule() != null) {
            modules.getCallsModule().checkCall(callId, attempt);
        }
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
        return callback -> modules.getUsersModule().editMyName(newName)
                .then(v -> callback.onResult(true))
                .failure(e -> callback.onError(e));
    }

    /**
     * Edit current user's nick
     *
     * @param newNick new user's nick
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editMyNickCommandWithNick:")
    public Command<Boolean> editMyNick(final String newNick) {
        return callback -> modules.getUsersModule().editNick(newNick)
                .then(v -> callback.onResult(true))
                .failure(e -> callback.onError(e));
    }

    /**
     * Edit current user's about
     *
     * @param newAbout new user's about
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editMyAboutCommandWithNick:")
    public Command<Boolean> editMyAbout(final String newAbout) {
        return callback -> modules.getUsersModule().editAbout(newAbout)
                .then(v -> callback.onResult(true))
                .failure(e -> callback.onError(e));
    }

    /**
     * Change current user's avatar
     *
     * @param descriptor descriptor of avatar file
     */
    @ObjectiveCName("changeMyAvatarWithDescriptor:")
    public void changeMyAvatar(String descriptor) {
        modules.getProfileModule().changeAvatar(descriptor);
    }

    /**
     * Remove current user's avatar
     */
    @ObjectiveCName("removeMyAvatar")
    public void removeMyAvatar() {
        modules.getProfileModule().removeAvatar();
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
        return callback -> modules.getUsersModule().editName(uid, name)
                .then(v -> callback.onResult(true))
                .failure(e -> callback.onError(e));
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
    public Command<Void> editGroupTitle(final int gid, final String title) {
        return callback -> modules.getGroupsModule().editTitle(gid, title)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Edit group's theme
     *
     * @param gid   group's id
     * @param theme new group theme
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("editGroupThemeCommandWithGid:withTheme:")
    public Command<Void> editGroupTheme(final int gid, final String theme) {
        return callback -> modules.getGroupsModule().editTheme(gid, theme)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Edit group's about
     *
     * @param gid   group's id
     * @param about new group about
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("editGroupAboutCommandWithGid:withAbout:")
    public Command<Void> editGroupAbout(final int gid, final String about) {
        return callback -> modules.getGroupsModule().editAbout(gid, about)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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
        return callback -> modules.getGroupsModule().createGroup(title, avatarDescriptor, uids)
                .then(integer -> callback.onResult(integer))
                .failure(e -> callback.onError(e));
    }


    /**
     * Leave group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("leaveGroupCommandWithGid:")
    public Command<Void> leaveGroup(final int gid) {
        return callback -> modules.getGroupsModule().leaveGroup(gid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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
    public Command<Void> inviteMember(int gid, int uid) {
        return callback -> modules.getGroupsModule().addMember(gid, uid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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
    public Command<Void> kickMember(int gid, int uid) {
        return callback -> modules.getGroupsModule().kickMember(gid, uid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Make member admin of group
     *
     * @param gid group's id
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("makeAdminCommandWithGid:withUid:")
    public Command<Void> makeAdmin(final int gid, final int uid) {
        return callback -> modules.getGroupsModule().makeAdmin(gid, uid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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
        return callback -> modules.getGroupsModule().requestInviteLink(gid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
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
        return callback -> modules.getGroupsModule().requestRevokeLink(gid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Join group using invite link
     *
     * @param token invite token
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("joinGroupViaLinkCommandWithToken:")
    public Command<Integer> joinGroupViaToken(String token) {
        return callback -> modules.getGroupsModule().joinGroupByToken(token)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Request integration token for group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("requestIntegrationTokenCommandWithGid:")
    public Command<String> requestIntegrationToken(int gid) {
        return callback -> modules.getGroupsModule().requestIntegrationToken(gid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Revoke get integration token for group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("revokeIntegrationTokenCommandWithGid:")
    public Command<String> revokeIntegrationToken(int gid) {
        return callback -> modules.getGroupsModule().revokeIntegrationToken(gid)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    /**
     * Check if chat with bot is started
     *
     * @param uid bot user id
     * @return is chat with bot started
     */
    @ObjectiveCName("isStartedWithUid:")
    public Promise<Boolean> isStarted(int uid) {
        return modules.getMessagesModule().chatIsEmpty(Peer.user(uid));
    }


    //////////////////////////////////////
    //         Blocked List
    //////////////////////////////////////

    /**
     * Load Blocked users list
     *
     * @return promise
     */
    @NotNull
    @ObjectiveCName("loadBlockedUsers")
    public Promise<List<User>> loadBlockedUsers() {
        return modules.getUsersModule().loadBlockedUsers();
    }

    /**
     * Block users
     *
     * @param uid user's id
     * @return promise
     */
    @NotNull
    @ObjectiveCName("blockUser:")
    public Promise<Void> blockUser(int uid) {
        return modules.getUsersModule().blockUser(uid);
    }

    /**
     * Unblock users
     *
     * @param uid user's id
     * @return promise
     */
    @NotNull
    @ObjectiveCName("unblockUser:")
    public Promise<Void> unblockUser(int uid) {
        return modules.getUsersModule().unblockUser(uid);
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
    @NotNull
    @ObjectiveCName("findUsersCommandWithQuery:")
    public Command<UserVM[]> findUsers(String query) {
        return callback -> modules.getContactsModule().findUsers(query)
                .then(v -> callback.onResult(v))
                .failure(e -> callback.onError(e));
    }

    //////////////////////////////////////
    //             Bindings
    //////////////////////////////////////

    /**
     * Bind File View Model
     *
     * @param fileReference reference to file
     * @param isAutoStart   automatically start download
     * @param callback      View Model file state callback
     * @return File View Model
     */
    @NotNull
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
    @NotNull
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
    @Nullable
    @ObjectiveCName("findDownloadedDescriptorWithFileId:")
    public String findDownloadedDescriptor(long fileId) {
        return modules.getFilesModule().getDownloadedDescriptor(fileId);
    }

    /**
     * Subscribing to download events
     *
     * @param callback subscribe callback
     */
    @ObjectiveCName("subscribeToDownloads:")
    public void subscribeToDownloads(FileEventCallback callback) {
        modules.getFilesModule().subscribe(callback);
    }

    /**
     * Unsubscribing from download events
     *
     * @param callback unsubscribe callback
     */
    @ObjectiveCName("unsubscribeFromDownloads:")
    public void unsubscribeFromDownloads(FileEventCallback callback) {
        modules.getFilesModule().unsubscribe(callback);
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
        return modules.getSettingsModule().isConversationTonesEnabled();
    }

    /**
     * Change conversation tones enabled value
     *
     * @param val is conversation tones enabled
     */
    @ObjectiveCName("changeConversationTonesEnabledWithValue:")
    public void changeConversationTonesEnabled(boolean val) {
        modules.getSettingsModule().changeConversationTonesEnabled(val);
    }

    /**
     * Is notifications enabled setting
     *
     * @return is notifications enabled
     */
    @ObjectiveCName("isNotificationsEnabled")
    public boolean isNotificationsEnabled() {
        return modules.getSettingsModule().isNotificationsEnabled();
    }

    /**
     * Change notifications enabled value
     *
     * @param val is notifications enabled
     */
    @ObjectiveCName("changeNotificationsEnabledWithValue:")
    public void changeNotificationsEnabled(boolean val) {
        modules.getSettingsModule().changeNotificationsEnabled(val);
    }

    /**
     * Is notifications sounds enabled
     *
     * @return is notification sounds enabled
     */
    @ObjectiveCName("isNotificationSoundEnabled")
    public boolean isNotificationSoundEnabled() {
        return modules.getSettingsModule().isNotificationSoundEnabled();
    }

    /**
     * Change notification sounds enabled
     *
     * @param val is notification sounds enabled
     */
    @ObjectiveCName("changeNotificationSoundEnabledWithValue:")
    public void changeNotificationSoundEnabled(boolean val) {
        modules.getSettingsModule().changeNotificationSoundEnabled(val);
    }

    /**
     * Sound that used for notifications
     *
     * @return notification sound name
     */
    @Nullable
    @ObjectiveCName("getNotificationSound")
    public String getNotificationSound() {
        return modules.getSettingsModule().getNotificationSound();
    }

    /**
     * Change sound that used for notifications
     *
     * @param sound notification sound name
     */
    @ObjectiveCName("changeNotificationSoundWithSound:")
    public void changeNotificationSound(String sound) {
        modules.getSettingsModule().changeNotificationSound(sound);
    }

    /**
     * Is notification vibration enabled
     *
     * @return is notification vibration enabled
     */
    @ObjectiveCName("isNotificationVibrationEnabled")
    public boolean isNotificationVibrationEnabled() {
        return modules.getSettingsModule().isVibrationEnabled();
    }

    /**
     * Change notification vibration enabled
     *
     * @param val is notification vibration enabled
     */
    @ObjectiveCName("changeNotificationVibrationEnabledWithValue:")
    public void changeNotificationVibrationEnabled(boolean val) {
        modules.getSettingsModule().changeNotificationVibrationEnabled(val);
    }

    /**
     * Is displaying text in notifications enabled
     *
     * @return is displaying text in notifications enabled
     */
    @ObjectiveCName("isShowNotificationsText")
    public boolean isShowNotificationsText() {
        return modules.getSettingsModule().isShowNotificationsText();
    }

    /**
     * Change displaying text in notifications enabled
     *
     * @param val is displaying text in notifications enabled
     */
    @ObjectiveCName("changeShowNotificationTextEnabledWithValue:")
    public void changeShowNotificationTextEnabled(boolean val) {
        modules.getSettingsModule().changeShowNotificationTextEnabled(val);
    }

    /**
     * Is send by enter enabled. Useful for android and web.
     *
     * @return is send by enter enabled
     */
    @ObjectiveCName("isSendByEnterEnabled")
    public boolean isSendByEnterEnabled() {
        return modules.getSettingsModule().isSendByEnterEnabled();
    }

    /**
     * Change if send by enter enabled
     *
     * @param val is send by enter enabled
     */
    @ObjectiveCName("changeSendByEnterWithValue:")
    public void changeSendByEnter(boolean val) {
        modules.getSettingsModule().changeSendByEnter(val);
    }

    /**
     * Change text size
     *
     * @param val new chat text size
     */
    @ObjectiveCName("changeTextSizeWithValue:")
    public void changeTextSize(int val) {
        modules.getSettingsModule().changeTextSize(val);
    }

    /**
     * Get text size
     */
    @ObjectiveCName("getTextSize")
    public int getTextSize() {
        return modules.getSettingsModule().getTextSize();
    }

    /**
     * Get privacy state
     *
     * @return privacy state
     */
    @NotNull
    @ObjectiveCName("getPrivacy")
    public String getPrivacy() {
        return modules.getSettingsModule().getPrivacy();
    }

    /**
     * Change privacy
     *
     * @param privacy privacy state (none|contacts|always)
     */
    @ObjectiveCName("setPrivacyWithPrivacy:")
    public void setPrivacy(String privacy) {
        modules.getSettingsModule().setPrivacy(privacy);
    }

    /**
     * Is notifications enabled for peer
     *
     * @param peer destination peer
     * @return is notifications enabled
     */
    @ObjectiveCName("isNotificationsEnabledWithPeer:")
    public boolean isNotificationsEnabled(Peer peer) {
        return modules.getSettingsModule().isNotificationsEnabled(peer);
    }

    /**
     * Change if notifications enabled for peer
     *
     * @param peer destination peer
     * @param val  is notifications enabled
     */
    @ObjectiveCName("changeNotificationsEnabledWithPeer:withValue:")
    public void changeNotificationsEnabled(Peer peer, boolean val) {
        modules.getSettingsModule().changeNotificationsEnabled(peer, val);
    }

    /**
     * Is in-app notifications enabled
     *
     * @return is notifications enabled
     */
    @ObjectiveCName("isInAppNotificationsEnabled")
    public boolean isInAppNotificationsEnabled() {
        return modules.getSettingsModule().isInAppEnabled();
    }

    /**
     * Change in-app notifications enable value
     *
     * @param val is notifications enabled
     */
    @ObjectiveCName("changeInAppNotificationsEnabledWithValue:")
    public void changeInAppNotificationsEnabled(boolean val) {
        modules.getSettingsModule().changeInAppEnabled(val);
    }

    /**
     * Is in-app notifications sound enabled
     *
     * @return is notifications sound enabled
     */
    @ObjectiveCName("isInAppNotificationSoundEnabled")
    public boolean isInAppNotificationSoundEnabled() {
        return modules.getSettingsModule().isInAppSoundEnabled();
    }

    /**
     * Change in-app notifications sound enabled value
     *
     * @param val is notifications sound enabled
     */
    @ObjectiveCName("changeInAppNotificationSoundEnabledWithValue:")
    public void changeInAppNotificationSoundEnabled(boolean val) {
        modules.getSettingsModule().changeInAppSoundEnabled(val);
    }

    /**
     * Is in-app notification vibration enabled
     *
     * @return is notifications vibration enabled
     */
    @ObjectiveCName("isInAppNotificationVibrationEnabled")
    public boolean isInAppNotificationVibrationEnabled() {
        return modules.getSettingsModule().isInAppVibrationEnabled();
    }

    /**
     * Change in-app notifications vibration enabled value
     *
     * @param val is notifications vibration enabled
     */
    @ObjectiveCName("changeInAppNotificationVibrationEnabledWithValue:")
    public void changeInAppNotificationVibrationEnabled(boolean val) {
        modules.getSettingsModule().changeInAppVibrationEnabled(val);
    }

    /**
     * Is Group Notifications Enabled
     *
     * @return is group notifications enabled
     */
    @ObjectiveCName("isGroupNotificationsEnabled")
    public boolean isGroupNotificationsEnabled() {
        return modules.getSettingsModule().isGroupNotificationsEnabled();
    }

    /**
     * Change group notifications enabled
     *
     * @param val is group notifications enabled
     */
    @ObjectiveCName("changeGroupNotificationsEnabled:")
    public void changeGroupNotificationsEnabled(boolean val) {
        modules.getSettingsModule().changeGroupNotificationsEnabled(val);
    }

    /**
     * Is Group Notifications only for mentions enabled
     *
     * @return val is group notifications only for mentions
     */
    @ObjectiveCName("isGroupNotificationsOnlyMentionsEnabled")
    public boolean isGroupNotificationsOnlyMentionsEnabled() {
        return modules.getSettingsModule().isGroupNotificationsOnlyMentionsEnabled();
    }

    /**
     * Change group notifications only for mentions enabled
     *
     * @param val is group notifications only for mentions
     */
    @ObjectiveCName("changeGroupNotificationsOnlyMentionsEnabled:")
    public void changeGroupNotificationsOnlyMentionsEnabled(boolean val) {
        modules.getSettingsModule().changeGroupNotificationsOnlyMentionsEnabled(val);
    }

    /**
     * Getting selected wallpaper uri. local:[file_name] for local files
     *
     * @return not null if custom background set
     */
    @ObjectiveCName("getSelectedWallpaper")
    public String getSelectedWallpaper() {
        return modules.getSettingsModule().getSelectedWallpapper();
    }

    /**
     * Change background
     *
     * @param uri background uri
     */
    @ObjectiveCName("changeSelectedWallpaper:")
    public void changeSelectedWallpaper(String uri) {
        modules.getSettingsModule().changeSelectedWallpapper(uri);
    }


    /**
     * Is Hint about contact rename shown to user and automatically mark as shown if not.
     *
     * @return is hint already shown
     */
    @ObjectiveCName("isRenameHintShown")
    public boolean isRenameHintShown() {
        return modules.getSettingsModule().isRenameHintShown();
    }

    //////////////////////////////////////
    //            Security
    //////////////////////////////////////

    /**
     * Loading active sessions
     *
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("loadSessionsCommand")
    public Command<List<ApiAuthSession>> loadSessions() {
        return callback -> modules.getSecurityModule().loadSessions()
                .then(r -> callback.onResult(r))
                .failure(e -> callback.onError(e));
    }

    /**
     * Terminate all other sessions
     *
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("terminateAllSessionsCommand")
    public Command<Void> terminateAllSessions() {
        return callback -> modules.getSecurityModule().terminateAllSessions()
                .then(r -> callback.onResult(r))
                .failure(e -> callback.onError(e));
    }

    /**
     * Terminate active session
     *
     * @param id session id
     * @return Command for execution
     */
    @NotNull
    @ObjectiveCName("terminateSessionCommandWithId:")
    public Command<Void> terminateSession(int id) {
        return callback -> modules.getSecurityModule().terminateSession(id)
                .then(r -> callback.onResult(r))
                .failure(e -> callback.onError(e));
    }

    //////////////////////////////////////
    //          Web Actions
    //////////////////////////////////////

    /**
     * Command for starting web action
     *
     * @param webAction web action name
     * @return Command for execution
     */
    @ObjectiveCName("startWebAction:")
    public Command<WebActionDescriptor> startWebAction(final String webAction) {
        return modules.getExternalModule().startWebAction(webAction);
    }

    /**
     * Command for completing web action
     *
     * @param actionHash web action name
     * @param url        completion url
     * @return Command for execution
     */
    @ObjectiveCName("completeWebActionWithHash:withUrl:")
    public Command<Boolean> completeWebAction(final String actionHash, final String url) {
        return modules.getExternalModule().completeWebAction(actionHash, url);
    }

    //////////////////////////////////////
    //              Raw api
    //////////////////////////////////////

    /**
     * Command for raw api request
     *
     * @param service service name
     * @param method  method name
     * @param params  request params
     * @return Command for execution
     */
    @ObjectiveCName("rawRequestCommandWithService:withMethod:WithParams:")
    public Command<ResponseRawRequest> rawRequestCommand(String service, String method, ApiRawValue params) {
        return modules.getExternalModule().rawRequestCommand(service, method, params);
    }

    /**
     * Command for raw api request
     *
     * @param service service name
     * @param method  method name
     * @param params  request params
     */
    @ObjectiveCName("rawRequestWithService:withMethod:WithParams:")
    public void rawRequest(String service, String method, ApiRawValue params) {
        modules.getExternalModule().rawRequest(service, method, params);
    }

    /**
     * Command for persistent raw api requests
     *
     * @param service service name
     * @param method  method name
     * @param params  request params
     */
    @ObjectiveCName("rawPersistentRequestWithService:withMethod:WithParams:")
    public void rawPersistentRequest(String service, String method, ApiRawValue params) {
        modules.getExternalModule().rawPersistentRequest(service, method, params);
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
        return modules.getI18nModule();
    }

    /**
     * Register google push
     *
     * @param projectId GCM project id
     * @param token     GCM token
     */
    @ObjectiveCName("registerGooglePushWithProjectId:withToken:")
    public void registerGooglePush(long projectId, String token) {
        modules.getPushesModule().registerGooglePush(projectId, token);
    }

    /**
     * Register apple push
     *
     * @param apnsId internal APNS cert key
     * @param token  APNS token
     */
    @ObjectiveCName("registerApplePushWithApnsId:withToken:")
    public void registerApplePush(int apnsId, String token) {
        modules.getPushesModule().registerApplePush(apnsId, token);
    }

    /**
     * Register apple push kit tokens
     *
     * @param apnsId internal APNS cert key
     * @param token  APNS token
     */
    @ObjectiveCName("registerApplePushKitWithApnsId:withToken:")
    public void registerApplePushKit(int apnsId, String token) {
        modules.getPushesModule().registerApplePushKit(apnsId, token);
    }

    /**
     * Register actor push
     *
     * @param endpoint push endpoint
     */
    @ObjectiveCName("registerActorPushWithEndpoint:")
    public void registerActorPush(String endpoint) {
        modules.getPushesModule().registerActorPush(endpoint);
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
     * Force checking of connection
     */
    @ObjectiveCName("forceNetworkCheck")
    public void forceNetworkCheck() {
        modules.getActorApi().forceNetworkCheck();
    }

    /**
     * Get modules of messenger for extensions
     *
     * @return Module Contexts
     */
    ModuleContext getModuleContext() {
        return modules;
    }
}