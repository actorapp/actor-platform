var DialogActionCreators = require('../actions/DialogActionCreators');

var setDialogs = function(dialogs) {
  // We need setTimeout here because bindDialogs dispatches event but bindDialogs itseld is called in the middle of dispatch (DialogStore)
  setTimeout(function() {
    DialogActionCreators.setDialogs(dialogs);
  }, 0);
};

var ActorClient = {
  isLoggedIn: function() {
    return(window.messenger.isLoggedIn());
  },

  bindDialogs: function() {
    window.messenger.bindDialogs(setDialogs);
  },

  unbindDialogs: function() {
    window.messenger.unbindDialogs(setDialogs);
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

  sendTextMessage: function(peer, text) {
    window.messenger.sendMessage(peer, text);
  },

  sendFileMessage: function(peer, file) {
    window.messenger.sendFile(peer, file);
  },

  sendPhotoMessage: function(peer, photo) {
    window.messenger.sendPhoto(peer, photo);
  }

};

module.exports = ActorClient;
