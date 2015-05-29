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

  selectDialog: function(dialog) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECT_DIALOG,
      dialog: dialog
    });
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
