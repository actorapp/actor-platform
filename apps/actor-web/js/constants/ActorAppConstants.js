var keymirror = require('keymirror');

module.exports = {

  ActionTypes: keymirror({
    REQUEST_SMS: null,
    SEND_CODE: null,
    SET_LOGGED_IN: null,

    DIALOGS_CHANGED: null,
    SELECT_DIALOG: null,

    SEND_MESSAGE_TEXT: null,
    SEND_MESSAGE_FILE: null,
    SEND_MESSAGE_PHOTO: null
  })

};
