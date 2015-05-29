var ActorClient = require('../utils/ActorClient');

var TypingActionCreators = {
  onTyping: function(peer) {
    ActorClient.onTyping(peer);
  }
};

module.exports = TypingActionCreators;
