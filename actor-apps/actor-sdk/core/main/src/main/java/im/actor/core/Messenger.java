/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiSex;
import im.actor.core.api.ApiAuthSession;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.Group;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PublicGroup;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.entity.WebActionDescriptor;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.Modules;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.DialogsClosed;
import im.actor.core.modules.events.DialogsOpened;
import im.actor.core.modules.events.PeerChatClosed;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.events.PeerInfoClosed;
import im.actor.core.modules.events.PeerInfoOpened;
import im.actor.core.modules.events.UserVisible;
import im.actor.core.network.NetworkState;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.util.ActorTrace;
import im.actor.core.util.Timing;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.FileCallback;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.GroupAvatarVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.OwnAvatarVM;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.core.viewmodel.UploadFileVMCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.storage.PreferencesStorage;

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

        // Start Messenger initialization
        Timing timing = new Timing("MESSENGER_INIT");

        // Actor system
        timing.section("Actors");
        ActorSystem.system().setTraceInterface(new ActorTrace());
        ActorSystem.system().addDispatcher("network");
        ActorSystem.system().addDispatcher("heavy");
        ActorSystem.system().addDispatcher("updates", 1);

        timing.section("Modules:Create");
        this.modules = new Modules(this, configuration);

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
     * Request email auth
     *
     * @param email email to authenticate
     * @return Command for execution
     */
    @NotNull
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
    @ObjectiveCName("requestStartAuthCommandWithPhone:")
    public Command<AuthState> requestStartPhoneAuth(final long phone) {
        return modules.getAuthModule().requestStartPhoneAuth(phone);
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
    public String getAuthEmail() {
        return modules.getAuthModule().getEmail();
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
    @ObjectiveCName("onNetworkChanged")
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
        sendMessage(peer, text, null, null, false);
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

    /**
     * Loading last read messages
     *
     * @param peer destination peer
     * @return rid of last read message
     */
    @ObjectiveCName("loadFirstUnread:")
    public long loadFirstUnread(Peer peer) {
        return modules.getMessagesModule().loadReadState(peer);
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
     * Edit current user's nick
     *
     * @param newNick new user's nick
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editMyNickCommandWithNick:")
    public Command<Boolean> editMyNick(final String newNick) {
        return modules.getUsersModule().editNick(newNick);
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
        return modules.getUsersModule().editAbout(newAbout);
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
     * Edit group's theme
     *
     * @param gid   group's id
     * @param theme new group theme
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editGroupThemeCommandWithGid:withTheme:")
    public Command<Boolean> editGroupTheme(final int gid, final String theme) {
        return modules.getGroupsModule().editTheme(gid, theme);
    }

    /**
     * Edit group's about
     *
     * @param gid   group's id
     * @param about new group about
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("editGroupAboutCommandWithGid:withAbout:")
    public Command<Boolean> editGroupAbout(final int gid, final String about) {
        return modules.getGroupsModule().editAbout(gid, about);
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
     * Make member admin of group
     *
     * @param gid group's id
     * @param uid user's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("makeAdminCommandWithGid:withUid:")
    public Command<Boolean> makeAdmin(final int gid, final int uid) {
        return modules.getGroupsModule().makeAdmin(gid, uid);
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

    /**
     * Join group using invite link
     *
     * @param url invite link
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("joinGroupViaLinkCommandWithUrl:")
    public Command<Integer> joinGroupViaLink(String url) {
        return modules.getGroupsModule().joinGroupViaLink(url);
    }

    /**
     * Join public group
     *
     * @param gid        group's id
     * @param accessHash group's accessHash
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("joinPublicGroupCommandWithGig:withAccessHash:")
    public Command<Integer> joinPublicGroup(int gid, long accessHash) {
        return modules.getGroupsModule().joinPublicGroup(gid, accessHash);
    }

    /**
     * Listing public groups
     *
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("listPublicGroups")
    public Command<List<PublicGroup>> listPublicGroups() {
        return modules.getGroupsModule().listPublicGroups();
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
        return modules.getGroupsModule().requestIntegrationToken(gid);
    }

    /**
     * Revoke get integration token for group
     *
     * @param gid group's id
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("revokeIntegrationTokenCommandWithGid:")
    public Command<String> revokeIntegrationToken(int gid) {
        return modules.getGroupsModule().revokeIntegrationToken(gid);
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
    @ObjectiveCName("changeNotificationVibrationEnabledWithValue")
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
     * Is markdown enabled.
     *
     * @return is markdown enabled
     */
    @ObjectiveCName("isMarkdownEnabled")
    public boolean isMarkdownEnabled() {
        return modules.getSettingsModule().isMarkdownEnabled();
    }

    /**
     * Change if markdown enabled
     *
     * @param val is markdown enabled
     */
    @ObjectiveCName("changeMarkdownWithValue:")
    public void changeMarkdown(boolean val) {
        modules.getSettingsModule().changeMarkdown(val);
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
     * Is Hint about contact rename shown to user and automatically mark as shown if not.
     *
     * @return is hint already shown
     */
    @ObjectiveCName("isRenameHintShown")
    public boolean isRenameHintShown() {
        return modules.getSettingsModule().isRenameHintShown();
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
    public Command<List<ApiAuthSession>> loadSessions() {
        return modules.getSecurityModule().loadSessions();
    }

    /**
     * Terminate all other sessions
     *
     * @return Command for execution
     */
    @Nullable
    @ObjectiveCName("terminateAllSessionsCommand")
    public Command<Boolean> terminateAllSessions() {
        return modules.getSecurityModule().terminateAllSessions();
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
        return modules.getSecurityModule().terminateSession(id);
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
     * Find core extension by key
     *
     * @param key extension key
     * @return founded extension, null if not found
     */
    @ObjectiveCName("findExtension:")
    public Extension findExtension(String key) {
        return modules.findExtension(key);
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