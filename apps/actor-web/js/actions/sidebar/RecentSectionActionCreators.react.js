var ActorAppDispatcher = require('../../dispatcher/ActorAppDispatcher.react');
var ActorAppConstants = require('../../constants/ActorAppConstants.react');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {

  selectPeer: function(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECT_DIALOG,
      peer: peer
    });
  }

};
