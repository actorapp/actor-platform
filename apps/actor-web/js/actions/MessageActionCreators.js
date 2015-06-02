'use strict';

var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {

  setMessageShown: function(peer, message) {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage: function(peer, text) {
    ActorClient.sendTextMessage(peer, text);
  },

  sendFileMessage: function(peer, file) {
    ActorClient.sendFileMessage(peer, file);
  },

  sendPhotoMessage: function(peer, photo) {
    ActorClient.sendPhotoMessage(peer, photo);
  }

};
