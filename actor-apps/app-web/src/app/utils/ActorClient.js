export default {
  requestSms(phone, success, failure) {
    window.messenger.requestSms(phone, success, failure);
  },

  sendCode(code, success, failure) {
    window.messenger.sendCode(code, success, failure);
  },

  signUp(name, success, failure) {
    window.messenger.signUp(name, success, failure);
  },

  isLoggedIn() {
    return window.messenger.isLoggedIn();
  },

  bindDialogs(callback) {
    window.messenger.bindDialogs(callback);
  },

  unbindDialogs(callback) {
    window.messenger.unbindDialogs(callback);
  },

  bindChat(peer, callback) {
    window.messenger.bindChat(peer, callback);
  },

  unbindChat(peer, callback) {
    window.messenger.unbindChat(peer, callback);
  },

  bindGroup(groupId, callback) {
    window.messenger.bindGroup(groupId, callback);
  },

  unbindGroup(groupId, callback) {
    window.messenger.unbindGroup(groupId, callback);
  },

  bindUser(userId, callback) {
    window.messenger.bindUser(userId, callback);
  },

  unbindUser(userId, callback) {
    window.messenger.unbindUser(userId, callback);
  },

  bindTyping(peer, callback) {
    window.messenger.bindTyping(peer, callback);
  },

  unbindTyping(peer, callback) {
    window.messenger.unbindTyping(peer, callback);
  },

  bindContacts(peer, callback) {
    window.messenger.bindContacts(peer, callback);
  },

  unbindContacts(peer, callback) {
    window.messenger.unbindContacts(peer, callback);
  },

  getUser(userId) {
    return window.messenger.getUser(userId);
  },

  getUid() {
    return window.messenger.getUid();
  },

  getGroup(groupId) {
    return window.messenger.getGroup(groupId);
  },

  getInviteUrl(groupId) {
    return window.messenger.getInviteLink(groupId);
  },

  sendTextMessage(peer, text) {
    window.messenger.sendMessage(peer, text);
  },

  sendFileMessage(peer, file) {
    window.messenger.sendFile(peer, file);
  },

  sendPhotoMessage(peer, photo) {
    window.messenger.sendPhoto(peer, photo);
  },

  sendClipboardPhotoMessage(peer, photo) {
    window.messenger.sendClipboardPhoto(peer, photo);
  },

  onMessageShown(peer, message) {
    window.messenger.onMessageShown(peer, message.sortKey, message.isOut);
  },

  onChatEnd (peer) {
    window.messenger.onChatEnd(peer);
  },

  onDialogsEnd () {
    window.messenger.onDialogsEnd();
  },

  onConversationOpen(peer) {
    window.messenger.onConversationOpen(peer);
  },

  onConversationClosed(peer) {
    window.messenger.onConversationClosed(peer);
  },

  onTyping(peer) {
    window.messenger.onTyping(peer);
  },

  onAppHidden() {
    window.messenger.onAppHidden();
  },

  onAppVisible() {
    window.messenger.onAppVisible();
  },

  editMyName(string) {
    window.messenger.editMyName(string);
  },

  addContact(uid) {
    window.messenger.addContact(uid);
  },

  removeContact(uid) {
    window.messenger.removeContact(uid);
  },

  // Groups

  joinGroup (url) {
    console.log('Joining group by url: ' + url);
    const p = window.messenger.joinGroupViaLink(url);

    return p;
  },

  leaveGroup(groupId) {
    window.messenger.leaveGroup(groupId);
  },

  createGroup(title, avatar, userIds) {
    console.log('Creating group', title, userIds);
    return window.messenger.createGroup(title, avatar, userIds);
  },

  kickMember(memberId, groupId) {
    window.messenger.kickMember(memberId, groupId);
  },

  inviteMember(groupId, userId) {
    return window.messenger.inviteMember(groupId, userId);
  },

  getIntegrationToken(gid) {
    return window.messenger.getIntegrationToken(gid);
  },

  loadDraft(peer) {
    return window.messenger.loadDraft(peer);
  },

  saveDraft(peer, draft) {
    if (draft !== null) {
      window.messenger.saveDraft(peer, draft);
    }
  },

  getUserPeer(uid) {
    return window.messenger.getUserPeer(uid);
  },

  getGroupPeer(gid) {
    return window.messenger.getGroupPeer(gid);
  },

  isNotificationsEnabled(peer) {
    return window.messenger.isNotificationsEnabled(peer);
  },

  changeNotificationsEnabled(peer, isEnabled) {
    window.messenger.changeNotificationsEnabled(peer, isEnabled);
  },

  findUsers(phone) {
    return window.messenger.findUsers(phone);
  }

};
