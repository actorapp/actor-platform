import ActorAppConstants from '../constants/ActorAppConstants';

const PeerTypes = ActorAppConstants.PeerTypes;

export default {
  requestSms: function(phone, callback) {
    window.messenger.requestSms(phone, callback);
  },

  sendCode: function(code, callback) {
    window.messenger.sendCode(code, callback);
  },

  signUp: function(name, cb) {
    window.messenger.signUp(name, cb);
  },

  isLoggedIn: function() {
    return window.messenger.isLoggedIn();
  },

  bindDialogs: function(callback) {
    window.messenger.bindDialogs(callback);
  },

  unbindDialogs: function(callback) {
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
    window.messenger.addContact(uid);
  },

  removeContact: function(uid) {
    window.messenger.removeContact(uid);
  },

  joinGroup (url) {
    const p = window.messenger.joinGroupViaLink(url)
      .then((r) => {
        if (typeof r.java_lang_Integer_value !== 'undefined') {
          return {type: PeerTypes.GROUP, id: r.java_lang_Integer_value};
        } else {
          return r;
        }
      });
    return p;
  },

  leaveGroup: function(groupId) {
    window.messenger.leaveGroup(groupId);
  },

  kickMember: function(memberId, groupId) {
    window.messenger.kickMember(memberId, groupId);
  },

  inviteMember: function(groupId, userId) {
    return window.messenger.inviteMember(groupId, userId);
  }
};

