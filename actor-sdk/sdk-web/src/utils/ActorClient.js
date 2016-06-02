/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

class ActorClient {
  requestSms(phone) {
    return new Promise((resolve, reject) => {
      window.messenger.requestSms(phone.trim(), resolve, reject);
    });
  }

  requestCodeEmail(email) {
    return new Promise((resolve, reject) => {
      window.messenger.requestCodeEmail(email.trim(), resolve, reject);
    });
  }

  sendCode(code) {
    return new Promise((resolve, reject) => {
      window.messenger.sendCode(code, resolve, reject);
    });
  }

  signUp(name) {
    return new Promise((resolve, reject) => {
      window.messenger.signUp(name, resolve, reject);
    });
  }

  isLoggedIn() {
    return window.messenger.isLoggedIn();
  }

  // Bindings

  static createBindings(bindName, unbindName, ...bindArgs) {
    let callback = bindArgs[bindArgs.length - 1];

    if (process.env.NODE_ENV === 'development') {
      if (typeof callback !== 'function') {
        console.error('%s expected %d argument to be function', bindName, bindArgs.length);
      }
    }

    let active = true;
    const checkCallback = (...args) => {
      if (active) {
        callback(...args);
      } else {
        console.error('You\'re trying to emit new data to inactive callback!', { bindName, unbindName });
      }
    };

    bindArgs[bindArgs.length - 1] = checkCallback;

    window.messenger[bindName](...bindArgs);

    return {
      unbind() {
        active = false;
        window.messenger[unbindName](...bindArgs);
        callback = null;
        bindArgs = null;
      }
    };
  }

  bindGroupDialogs(callback) {
    return ActorClient.createBindings('bindGroupDialogs', 'unbindGroupDialogs', callback);
  }

  bindChat(peer, callback) {
    return ActorClient.createBindings('bindChat', 'unbindChat', peer, callback);
  }

  bindGroup(gid, callback) {
    return ActorClient.createBindings('bindGroup', 'unbindGroup', gid, callback);
  }

  bindUser(uid, callback) {
    return ActorClient.createBindings('bindUser', 'unbindUser', uid, callback);
  }

  bindTyping(peer, callback) {
    return ActorClient.createBindings('bindTyping', 'unbindTyping', peer, callback);
  }

  bindContacts(callback) {
    return ActorClient.createBindings('bindContacts', 'unbindContacts', callback);
  }

  bindConnectState(callback) {
    return ActorClient.createBindings('bindConnectState', 'unbindConnectState', callback);
  }

  bindGlobalCounter(callback) {
    return ActorClient.createBindings('bindGlobalCounter', 'unbindGlobalCounter', callback);
  }

  bindTempGlobalCounter(callback) {
    return ActorClient.createBindings('bindTempGlobalCounter', 'unbindTempGlobalCounter', callback);
  }

  bindUserOnline(uid, callback) {
    return ActorClient.createBindings('bindUserOnline', 'unbindUserOnline', uid, callback);
  }

  bindGroupOnline(gid, callback) {
    return ActorClient.createBindings('bindGroupOnline', 'unbindGroupOnline', gid, callback);
  }

  bindMessages(peer, callback) {
    let active = true;
    let binding = window.messenger.bindMessages(peer, (...args) => {
      if (active) {
        callback(...args);
      } else {
        console.error('You\'re trying to emit new data to inactive messages binding!')
      }
    });

    binding.initAll();

    return {
      unbind() {
        binding.unbind();
        active = false;
        binding = null;
      }
    };
  }

  bindEventBus(callback) {
    return ActorClient.createBindings('bindEventBus', 'unbindEventBus', callback);
  }

  bindCall(callId, callback) {
    return ActorClient.createBindings('bindCall', 'unbindCall', callId, callback);
  }

  bindStickers(callback) {
    return ActorClient.createBindings('bindStickers', 'unbindStickers', callback);
  }

  makeCall(userId) {
    return window.messenger.doCall(userId);
  }

  makeGroupCall(groupId) {
    return window.messenger.doGroupCall(groupId);
  }

  answerCall(callId) {
    window.messenger.answerCall(callId);
  }

  endCall(callId) {
    window.messenger.endCall(callId);
  }

  toggleCallMute(callId) {
    window.messenger.toggleCallMute(callId);
  }

  getUser(uid) {
    return window.messenger.getUser(uid);
  }

  getUid() {
    return window.messenger.getUid();
  }

  getGroup(gid) {
    return window.messenger.getGroup(gid);
  }

  getInviteUrl(gid) {
    return window.messenger.getInviteLink(gid);
  }

  sendTextMessage(peer, text) {
    window.messenger.sendMessage(peer, text);
  }

  editMessage(peer, rid, text) {
    return window.messenger.editMessage(peer, rid, text);
  }

  sendFileMessage(peer, file) {
    window.messenger.sendFile(peer, file);
  }

  sendPhotoMessage(peer, photo) {
    window.messenger.sendPhoto(peer, photo);
  }

  sendAnimationMessage(peer, file) {
    window.messenger.sendAnimation(peer, file);
  }

  sendClipboardPhotoMessage(peer, photo) {
    window.messenger.sendClipboardPhoto(peer, photo);
  }

  onMessageShown(peer, message) {
    window.messenger.onMessageShown(peer, message);
  }

  onChatEnd (peer) {
    window.messenger.onChatEnd(peer);
  }

  onDialogsEnd () {
    window.messenger.onDialogsEnd();
  }

  onConversationOpen(peer) {
    window.messenger.onConversationOpen(peer);
  }

  onConversationClosed(peer) {
    window.messenger.onConversationClosed(peer);
  }

  onTyping(peer) {
    window.messenger.onTyping(peer);
  }

  onAppHidden() {
    window.messenger.onAppHidden();
  }

  onAppVisible() {
    window.messenger.onAppVisible();
  }

  addContact(uid) {
    return window.messenger.addContact(uid);
  }

  removeContact(uid) {
    return window.messenger.removeContact(uid);
  }

  // Profile

  editMyName(newName) {
    return window.messenger.editMyName(newName);
  }

  editMyNick(newNick) {
    return window.messenger.editMyNick(newNick)
  }

  editMyAbout(newAbout) {
    return window.messenger.editMyAbout(newAbout);
  }

  // Groups

  joinGroupViaToken(token) {
    const link = `https://quit.email/join/${token}`;
    return window.messenger.joinGroupViaLink(link);
  }

  joinGroupViaLink (url) {
    return window.messenger.joinGroupViaLink(url);
  }

  leaveGroup(gid) {
    return window.messenger.leaveGroup(gid);
  }

  createGroup(title, avatar, userIds) {
    return window.messenger.createGroup(title, avatar, userIds);
  }

  kickMember(gid, uid) {
    return window.messenger.kickMember(gid, uid);
  }

  inviteMember(gid, uid) {
    console.log(`%c Invite new member ${uid} to ${gid}`, 'color: #fd5c52');
    return window.messenger.inviteMember(gid, uid);
  }

  getIntegrationToken(gid) {
    return window.messenger.getIntegrationToken(gid);
  }

  loadDraft(peer) {
    return window.messenger.loadDraft(peer);
  }

  saveDraft(peer, draft) {
    if (draft !== null) {
      window.messenger.saveDraft(peer, draft);
    }
  }

  getUserPeer(uid) {
    return window.messenger.getUserPeer(uid);
  }

  getGroupPeer(gid) {
    return window.messenger.getGroupPeer(gid);
  }

  hideChat(peer) {
    return window.messenger.hideChat(peer);
  }

  findBotCommands(id, query) {
    const result = window.messenger.findBotCommands(id, query);
    if (result && result.length) {
      return result;
    }

    return null;
  }

  // Mentions

  findMentions(gid, query = '') {
    const result = window.messenger.findMentions(gid, query);
    if (result && result.length) {
      return result;
    }

    return null;
  }

  deleteChat(peer) {
    return window.messenger.deleteChat(peer);
  }

  clearChat(peer) {
    return window.messenger.clearChat(peer);
  }

  editGroupTitle(gid, title) {
    return window.messenger.editGroupTitle(gid, title);
  }

  editGroupAbout(gid, about) {
    return window.messenger.editGroupAbout(gid, about);
  }

  renderMarkdown(markdownText) {
    return window.messenger.renderMarkdown(markdownText);
  }

  // Settings

  changeNotificationsEnabled(peer, isEnabled) {
    window.messenger.changeNotificationsEnabled(peer, isEnabled);
  }

  isNotificationsEnabled(peer) {
    return window.messenger.isNotificationsEnabled(peer);
  }

  isSendByEnterEnabled() {
    return window.messenger.isSendByEnterEnabled();
  }

  changeSendByEnter(isEnabled) {
    window.messenger.changeSendByEnter(isEnabled);
  }

  isGroupsNotificationsEnabled() {
    return window.messenger.isGroupsNotificationsEnabled();
  }

  changeGroupNotificationsEnabled(isEnabled) {
    window.messenger.changeGroupNotificationsEnabled(isEnabled);
  }

  isOnlyMentionNotifications() {
    return window.messenger.isOnlyMentionNotifications();
  }

  changeIsOnlyMentionNotifications(isEnabled) {
    window.messenger.changeIsOnlyMentionNotifications(isEnabled);
  }

  isSoundEffectsEnabled() {
    return window.messenger.isSoundEffectsEnabled();
  }

  changeSoundEffectsEnabled(isEnabled) {
    window.messenger.changeSoundEffectsEnabled(isEnabled);
  }

  isShowNotificationsTextEnabled() {
    return window.messenger.isShowNotificationsTextEnabled();
  }

  changeIsShowNotificationTextEnabled(isEnabled) {
    window.messenger.changeIsShowNotificationTextEnabled(isEnabled);
  }

  loadSessions() {
    return window.messenger.loadSessions();
  }

  terminateSession(id) {
    return window.messenger.terminateSession(id);
  }

  terminateAllSessions() {
    return window.messenger.terminateAllSessions();
  }

  changeMyAvatar(avatar) {
    window.messenger.changeMyAvatar(avatar);
  }

  changeGroupAvatar(gid, avatar) {
    window.messenger.changeGroupAvatar(gid, avatar);
  }

  removeMyAvatar() {
    window.messenger.removeMyAvatar();
  }

  removeGroupAvatar(gid) {
    window.messenger.removeGroupAvatar(gid);
  }

  // Search

  findGroups(query) {
    return window.messenger.findGroups(query);
  }

  findUsers(phone) {
    return window.messenger.findUsers(phone.toString());
  }

  // Messages

  deleteMessage(peer, rid) {
    return window.messenger.deleteMessage(peer, rid);
  }

  addLike(peer, rid) {
    return window.messenger.addLike(peer, rid);
  }

  removeLike(peer, rid) {
    return window.messenger.removeLike(peer, rid);
  }

  sendVoiceMessage(peer, duration, voice) {
    window.messenger.sendVoiceMessage(peer, duration, voice);
  }

  // Search

  bindSearch(callback) {
    return ActorClient.createBindings('bindSearch', 'unbindSearch', callback);
  }

  findAllText(peer, query) {
    return window.messenger.findAllText(peer, query);
  }

  findAllDocs(peer) {
    return window.messenger.findAllDocs(peer);
  }

  findAllLinks(peer) {
    return window.messenger.findAllLinks(peer);
  }

  findAllPhotos(peer) {
    return window.messenger.findAllPhotos(peer);
  }

  handleLinkClick(event) {
    window.messenger.handleLinkClick(event)
  }

  isElectron() {
    return window.messenger.isElectron();
  }

  favoriteChat(peer) {
    return window.messenger.favoriteChat(peer);
  }

  unfavoriteChat(peer) {
    return window.messenger.unfavoriteChat(peer);
  }

  archiveChat(peer) {
    return window.messenger.archiveChat(peer);
  }

  loadArchivedDialogs() {
    return window.messenger.loadArchivedDialogs();
  }

  loadMoreArchivedDialogs() {
    return window.messenger.loadMoreArchivedDialogs();
  }

  sendSticker(peer, sticker) {
    window.messenger.sendSticker(peer, sticker);
  }

  blockUser(id) {
    return window.messenger.blockUser(id);
  }

  unblockUser(id) {
    return window.messenger.unblockUser(id);
  }

  loadBlockedUsers() {
    return window.messenger.loadBlockedUsers();
  }
}

export default new ActorClient();
