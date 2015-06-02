var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

var _lastOpenConversationPeer = null;

var DialogActionCreators = {
  setDialogs: function(dialogs) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DIALOGS_CHANGED,
      dialogs: dialogs
    });
  },

  selectDialogPeer: function(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECT_DIALOG_PEER,
      peer: peer
    });
  },

  selectDialogPeerUser: function(userId) {
    this.selectDialogPeer({
      id: userId,
      type: ActorAppConstants.PeerTypes.USER
    })
  },

  createSelectedDialogInfoChanged: function(info) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECTED_DIALOG_INFO_CHANGED,
      info: info
    })
  },

  onConversationOpen: function(peer) {
    ActorClient.onConversationOpen(peer);
  },

  onConversationClosed: function(peer) {
    ActorClient.onConversationClosed(peer);
  }
};

module.exports = DialogActionCreators;
