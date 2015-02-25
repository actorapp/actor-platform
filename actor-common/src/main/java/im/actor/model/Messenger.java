package im.actor.model;

import im.actor.model.concurrency.Command;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.droidkit.actors.debug.TraceInterface;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.storage.ListEngine;
import im.actor.model.viewmodel.GroupTypingVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserTypingVM;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Messenger {
    private Modules modules;

    public Messenger(Configuration configuration) {
        // Init internal actor system
        Environment.setThreading(configuration.getThreading());

        // Init Crypto
        CryptoUtils.init(configuration.getCryptoProvider());

        // Init MVVM
        MVVMEngine.init(configuration.getMainThread());

        // Init Log
        Log.setLog(configuration.getLog());

        ActorSystem.system().setTraceInterface(new TraceInterface() {
            @Override
            public void onEnvelopeDelivered(Envelope envelope) {

            }

            @Override
            public void onEnvelopeProcessed(Envelope envelope, long duration) {

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

        this.modules = new Modules(configuration);
        this.modules.run();
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
        return modules.getUsersModule().getUsersCollection();
    }

    public MVVMCollection<Group, GroupVM> getGroups() {
        return modules.getGroupsModule().getGroupsCollection();
    }

    public ListEngine<Dialog> getDialogs() {
        return modules.getMessagesModule().getDialogsEngine();
    }

    public ListEngine<Message> getMessages(Peer peer) {
        return modules.getMessagesModule().getConversationEngine(peer);
    }

    public UserTypingVM getTyping(int uid) {
        return modules.getTypingModule().getTyping(uid);
    }

    public GroupTypingVM getGroupTyping(int gid) {
        return modules.getTypingModule().getGroupTyping(gid);
    }

    public void onAppVisible() {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().onAppVisible();
        }
    }

    public void onAppHidden() {
        if (modules.getPresenceModule() != null) {
            modules.getPresenceModule().onAppHidden();
        }
    }

    public void onConversationOpen(Peer peer) {
        modules.getPresenceModule().subscribe(peer);
    }

    public void onConversationClosed(Peer peer) {

    }

    public void onProfileOpen(int uid) {
        modules.getPresenceModule().subscribe(Peer.user(uid));
    }

    public void onProfileClosed(int uid) {

    }

    public void onInMessageShown(Peer peer, long rid, long sortDate, boolean isEncrypted) {
        modules.getMessagesModule().onInMessageShown(peer, rid, sortDate, isEncrypted);
    }

    public void onTyping(Peer peer) {
        modules.getTypingModule().onTyping(peer);
    }

    public void onPhoneBookChanged() {
        if (modules.getContactsModule() != null) {
            modules.getContactsModule().onPhoneBookChanged();
        }
    }

    public long loadLastReadSortDate(Peer peer) {
        return modules.getMessagesModule().loadReadState(peer);
    }

    public void saveDraft(Peer peer, String draft) {
        modules.getMessagesModule().saveDraft(peer, draft);
    }

    public String loadDraft(Peer peer) {
        return modules.getMessagesModule().loadDraft(peer);
    }

    public void sendMessage(Peer peer, String text) {
        modules.getMessagesModule().sendMessage(peer, text);
    }

    public Command<Boolean> editMyName(final String newName) {
        return modules.getUsersModule().editMyName(newName);
    }

    public Command<Boolean> editName(final int uid, final String name) {
        return modules.getUsersModule().editName(uid, name);
    }

    public Command<Boolean> editGroupTitle(final int gid, final String title) {
        return modules.getGroupsModule().editTitle(gid, title);
    }

    public Command<Boolean> leaveGroup(final int gid) {
        return modules.getGroupsModule().leaveGroup(gid);
    }

    public Command<Boolean> removeContact(int uid) {
        return modules.getContactsModule().removeContact(uid);
    }

    public Command<Boolean> addContact(int uid) {
        return modules.getContactsModule().addContact(uid);
    }
}