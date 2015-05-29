var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

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
  },

  createSelectedDialogInfoChanged: function(info) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECTED_DIALOG_INFO_CHANGED,
      info: info
    })
  }
};
