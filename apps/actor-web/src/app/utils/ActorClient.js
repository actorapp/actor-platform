var ActorClient = {
  requestSms: function(phone, callback) {
    window.messenger.requestSms(phone, callback);
  },

  sendCode: function(code, callback) {
    window.messenger.sendCode(code, callback);
  },

  isLoggedIn: function() {
    return window.messenger.isLoggedIn();
  },

  bindDialogs: function(callback) {
    window.messenger.bindDialogs(callback);
  },

  unbindDialogs: function() {
    window.messenger.unbindDialogs(callback);
  },

  bindChat: function(peer, callback) {
    window.messenger.bindChat(peer, callback);
  },

  unbindChat: function(peer, callback) {
    window.messenger.unbindChat(peer, callback);
  },

  bindGroup: function(groupId, callback) {
    window.messenger.bindGroup(groupId, callback);
  },

  unbindGroup: function(groupId, callback) {
    window.messenger.unbindGroup(groupId, callback);
  },

  bindUser: function(userId, callback) {
    window.messenger.bindUser(userId, callback);
  },

  unbindUser: function(userId, callback) {
    window.messenger.unbindUser(userId, callback);
  },

  bindTyping: function(peer, callback) {
    window.messenger.bindTyping(peer, callback);
  },

  unbindTyping: function(peer, callback) {
    window.messenger.unbindTyping(peer, callback);
  },

  bindContacts: function(peer, callback) {
    window.messenger.bindContacts(peer, callback);
  },

  unbindContacts: function(peer, callback) {
    window.messenger.unbindContacts(peer, callback);
  },

  getUser: function(userId) {
    return window.messenger.getUser(userId);
  },

  getUid: function() {
    return window.messenger.getUid();
  },

  getGroup: function(groupId) {
    return window.messenger.getGroup(groupId);
  },

  sendTextMessage: function(peer, text) {
    window.messenger.sendMessage(peer, text);
  },

  sendFileMessage: function(peer, file) {
    window.messenger.sendFile(peer, file);
  },

  sendPhotoMessage: function(peer, photo) {
    window.messenger.sendPhoto(peer, photo);
  },

  sendClipboardPhotoMessage: function(peer, photo) {
    window.messenger.sendClipboardPhoto(peer, photo);
  },

  onMessageShown: function(peer, message) {
    window.messenger.onMessageShown(peer, message.sortKey, message.isOut);
  },

  onConversationOpen: function(peer) {
    window.messenger.onConversationOpen(peer);
  },

  onConversationClosed: function(peer) {
    window.messenger.onConversationClosed(peer);
  },

  onTyping: function(peer) {
    window.messenger.onTyping(peer);
  },

  onAppHidden: function() {
    window.messenger.onAppHidden();
  },

  onAppVisible: function() {
    window.messenger.onAppVisible();
  },

  editMyName: function(string) {
    window.messenger.editMyName(string);
  },

  addContact: function(uid) {
    //console.warn('addContact', uid);
    messenger.addContact(uid);
  },

  removeContact: function(uid) {
    //console.warn('removeContact', uid);
    messenger.removeContact(uid);
  }
};

module.exports = ActorClient;
