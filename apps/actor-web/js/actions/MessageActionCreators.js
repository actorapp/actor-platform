'use strict';

var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {

  setMessageShown: function(peer, message) {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage: function(dialog, text) {
    ActorClient.sendTextMessage(dialog.peer.peer, text);

    ActorAppDispatcher.dispatch({
      type: ActionTypes.SEND_MESSAGE_TEXT,
      dialog: dialog,
      text: text
    });
  },

  sendFileMessage: function(dialog, file) {
    ActorClient.sendFileMessage(dialog.peer.peer, file);

    ActorAppDispatcher.dispatch({
      type: ActionTypes.SEND_MESSAGE_FILE,
      dialog: dialog,
      file: file
    });
  },

  sendPhotoMessage: function(dialog, photo) {
    ActorClient.sendPhotoMessage(dialog.peer.peer, photo);

    ActorAppDispatcher.dispatch({
      type: ActionTypes.SEND_MESSAGE_PHOTO,
      dialog: dialog,
      file: photo
    });
  }

};
