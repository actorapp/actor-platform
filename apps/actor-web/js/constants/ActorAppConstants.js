var keymirror = require('keymirror');

module.exports = {

  ActionTypes: keymirror({
    SET_LOGGED_IN: null,

    DIALOGS_CHANGED: null,
    SELECT_DIALOG: null,
    SELECTED_DIALOG_INFO_CHANGED: null,

    SEND_MESSAGE_TEXT: null,
    SEND_MESSAGE_FILE: null,
    SEND_MESSAGE_PHOTO: null
  })

};
