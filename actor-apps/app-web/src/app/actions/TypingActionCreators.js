import ActorClient from 'utils/ActorClient';

export default {
  onTyping: function(peer) {
    ActorClient.onTyping(peer);
  }
};

