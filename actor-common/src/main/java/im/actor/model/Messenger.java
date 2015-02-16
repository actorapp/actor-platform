package im.actor.model;

import im.actor.model.concurrency.Command;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Messenger {
    private Modules modules;

    public Messenger(Configuration configuration) {
        this.modules = new Modules(configuration);
    }

    // Auth

    public State getState() {
        return modules.getAuthModule().getState();
    }

    public boolean isLoggedIn() {
        return getState() == State.LOGGED_IN;
    }

    public Command<State> requestSms(final long phone) {
        return modules.getAuthModule().requestSms(phone);
    }

    public Command<State> sendCode(final int code) {
        return modules.getAuthModule().sendCode(code);
    }

    public Command<State> signUp(final String firstName, String avatarPath, final boolean isSilent) {
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

    public KeyValueEngine<im.actor.model.entity.User> getUsers() {
        return modules.getUsersModule().getUsers();
    }

    public ListEngine<Dialog> getDialogs() {
        return modules.getMessagesModule().getDialogsEngine();
    }

    public ListEngine<Message> getMessages(Peer peer) {
        return modules.getMessagesModule().getConversationEngine(peer);
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
        modules.getPresenceModule().onConversationOpen(peer);
    }

    public void onConversationClosed(Peer peer) {
        modules.getPresenceModule().onConversationClosed(peer);
    }

    public void onTyping(Peer peer) {
        modules.getTypingModule().onTyping(peer);
    }

    public void saveDraft(Peer peer, String draft) {
        modules.getMessagesModule().saveDraft(peer, draft);
    }

    public String loadDraft(Peer peer) {
        return modules.getMessagesModule().loadDraft(peer);
    }

    public Command<Boolean> editMyName(final String newName) {
        return modules.getUsersModule().editMyName(newName);
    }

    public Command<Boolean> editName(final int uid, final String name) {
        return modules.getUsersModule().editName(uid, name);
    }
}