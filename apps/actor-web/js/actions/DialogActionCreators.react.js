var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher.react');
var ActorAppConstants = require('../constants/ActorAppConstants.react');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {
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
  }
};
