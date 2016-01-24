/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

export default {

  // Auth

  requestSms(phone) {
    return new Promise((resolve, reject) => {
      window.messenger.requestSms(phone, resolve, reject);
    });
  },

  requestCodeEmail(email) {
    return new Promise((resolve, reject) => {
      window.messenger.requestCodeEmail(email, resolve, reject);
    });
  },

  sendCode(code) {
    return new Promise((resolve, reject) => {
      window.messenger.sendCode(code, resolve, reject);
    });
  },

  signUp(name) {
    return new Promise((resolve, reject) => {
      window.messenger.signUp(name, resolve, reject);
    });
  },

  isLoggedIn() {
    return window.messenger.isLoggedIn();
  },

  // Bindings

  bindDialogs(callback) {
    window.messenger.bindDialogs(callback);
  },

  unbindDialogs(callback) {
    window.messenger.unbindDialogs(callback);
  },

  bindGroupDialogs(callback) {
    window.messenger.bindGroupDialogs(callback);
  },

  unbindGroupDialogs(callback) {
    window.messenger.unbindGroupDialogs(callback);
  },

  bindChat(peer, callback) {
    window.messenger.bindChat(peer, callback);
  },

  unbindChat(peer, callback) {
    window.messenger.unbindChat(peer, callback);
  },

  bindGroup(gid, callback) {
    window.messenger.bindGroup(gid, callback);
  },

  unbindGroup(gid, callback) {
    window.messenger.unbindGroup(gid, callback);
  },

  bindUser(uid, callback) {
    window.messenger.bindUser(uid, callback);
  },

  unbindUser(uid, callback) {
    window.messenger.unbindUser(uid, callback);
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

  bindConnectState(callback) {
    window.messenger.bindConnectState(callback);
  },

  unbindConnectState(callback) {
    window.messenger.unbindConnectState(callback);
  },

  bindGlobalCounter(callback) {
    window.messenger.bindGlobalCounter(callback);
  },

  unbindGlobalCounter(callback) {
    window.messenger.unbindGlobalCounter(callback);
  },

  bindTempGlobalCounter(callback) {
    window.messenger.bindTempGlobalCounter(callback);
  },

  unbindTempGlobalCounter(callback) {
    window.messenger.unbindTempGlobalCounter(callback);
  },

  bindUserOnline(uid, callback) {
    window.messenger.bindUserOnline(uid, callback);
  },

  unbindUserOnline(uid, callback) {
    window.messenger.unbindUserOnline(uid, callback);
  },

  bindGroupOnline(gid, callback) {
    window.messenger.bindGroupOnline(gid, callback);
  },

  unbindGroupOnline(gid, callback) {
    window.messenger.unbindGroupOnline(gid, callback);
  },

  getUser(uid) {
    return window.messenger.getUser(uid);
  },

  getUid() {
    return window.messenger.getUid();
  },

  getGroup(gid) {
    return window.messenger.getGroup(gid);
  },

  getInviteUrl(gid) {
    return window.messenger.getInviteLink(gid);
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
    window.messenger.onMessageShown(peer, message);
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

  joinGroupViaLink (url) {
    return window.messenger.joinGroupViaLink(url);
  },

  leaveGroup(gid) {
    return window.messenger.leaveGroup(gid);
  },

  createGroup(title, avatar, userIds) {
    return window.messenger.createGroup(title, avatar, userIds);
  },

  kickMember(gid, uid) {
    return window.messenger.kickMember(gid, uid);
  },

  inviteMember(gid, uid) {
    return window.messenger.inviteMember(gid, uid);
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

  hideChat(peer) {
    return window.messenger.hideChat(peer);
  },

  // Mentions

  findMentions(gid, query = '') {
    return window.messenger.findMentions(gid, query);
  },

  // Nickname

  editMyNick(string) {
    window.messenger.editMyNick(string)
  },

  deleteChat(peer) {
    return window.messenger.deleteChat(peer);
  },

  clearChat(peer) {
    return window.messenger.clearChat(peer);
  },

  editMyAbout(about) {
    return window.messenger.editMyAbout(about);
  },

  editGroupTitle(gid, title) {
    return window.messenger.editGroupTitle(gid, title);
  },

  editGroupAbout(gid, about) {
    return window.messenger.editGroupAbout(gid, about);
  },

  renderMarkdown(markdownText) {
    return window.messenger.renderMarkdown(markdownText);
  },

  // Settings

  changeNotificationsEnabled(peer, isEnabled) {
    window.messenger.changeNotificationsEnabled(peer, isEnabled);
  },

  isNotificationsEnabled(peer) {
    return window.messenger.isNotificationsEnabled(peer);
  },

  isSendByEnterEnabled() {
    return window.messenger.isSendByEnterEnabled();
  },

  changeSendByEnter(isEnabled) {
    window.messenger.changeSendByEnter(isEnabled);
  },

  isGroupsNotificationsEnabled() {
    return window.messenger.isGroupsNotificationsEnabled();
  },

  changeGroupNotificationsEnabled(isEnabled) {
    window.messenger.changeGroupNotificationsEnabled(isEnabled);
  },

  isOnlyMentionNotifications() {
    return window.messenger.isOnlyMentionNotifications();
  },

  changeIsOnlyMentionNotifications(isEnabled) {
    window.messenger.changeIsOnlyMentionNotifications(isEnabled);
  },

  isSoundEffectsEnabled() {
    return window.messenger.isSoundEffectsEnabled();
  },

  changeSoundEffectsEnabled(isEnabled) {
    window.messenger.changeSoundEffectsEnabled(isEnabled);
  },

  isShowNotificationsTextEnabled() {
    return window.messenger.isShowNotificationsTextEnabled();
  },

  changeIsShowNotificationTextEnabled(isEnabled) {
    window.messenger.changeIsShowNotificationTextEnabled(isEnabled);
  },

  loadSessions() {
    return window.messenger.loadSessions();
  },

  terminateSession(id) {
    return window.messenger.terminateSession(id);
  },

  terminateAllSessions() {
    return window.messenger.terminateAllSessions();
  },

  changeMyAvatar(avatar) {
    window.messenger.changeMyAvatar(avatar);
  },

  changeGroupAvatar(gid, avatar) {
    window.messenger.changeGroupAvatar(gid, avatar);
  },

  removeMyAvatar() {
    window.messenger.removeMyAvatar();
  },

  removeGroupAvatar(gid) {
    window.messenger.removeGroupAvatar(gid);
  },

  // Search

  findGroups(query) {
    return window.messenger.findGroups(query);
  },

  findUsers(phone) {
    return window.messenger.findUsers(phone.toString());
  },

  // Messages

  deleteMessage(peer, rid) {
    return window.messenger.deleteMessage(peer, rid);
  },

  addLike(peer, rid) {
    return window.messenger.addLike(peer, rid);
  },

  removeLike(peer, rid) {
    return window.messenger.removeLike(peer, rid);
  },

  sendVoiceMessage(peer, duration, voice) {
    window.messenger.sendVoiceMessage(peer, duration, voice);
  },

  // Search

  bindSearch(callback) {
    window.messenger.bindSearch(callback);
  },

  unbindSearch(callback) {
    window.messenger.unbindSearch(callback);
  },

  findAllText(peer, query) {
    return window.messenger.findAllText(peer, query);
  },

  findAllDocs(peer) {
    return window.messenger.findAllDocs(peer);
  },

  findAllLinks(peer) {
    return window.messenger.findAllLinks(peer);
  },

  findAllPhotos(peer) {
    return window.messenger.findAllPhotos(peer);
  },

  handleLinkClick(event) {
    messenger.handleLinkClick(event)
  },

  isElectron() {
    return window.messenger.isElectron();
  },

  favoriteChat(peer) {
    return window.messenger.favoriteChat(peer);
  },

  unfavoriteChat(peer) {
    return window.messenger.unfavoriteChat(peer);
  }
}
