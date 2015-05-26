var DialogActionCreators = require('../actions/DialogActionCreators');

var setDialogs = function(dialogs) {
    DialogActionCreators.setDialogs(dialogs);
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
  }
};

module.exports = ActorClient;
