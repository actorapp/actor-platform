var ActorWebAppDispatcher = require('../dispatcher/ActorWebAppDispatcher');

module.exports = {
  selectPeer: function(peer) {
    console.log('selectpeer action');
    ActorWebAppDispatcher.dispatch({
      type: 'select-peer',
      peer: peer
    });
  }
};
