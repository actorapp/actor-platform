'use strict';

var _ = require('lodash');

var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

var emojiCharacters = require('emoji-named-characters');

var variants = _.map(Object.keys(emojiCharacters), function(name) {
  return(name.replace(/\+/g, '\\+'));
});

var regexp = new RegExp('\\:(' + variants.join('|') + ')\\:', 'gi');

var replaceNames = function(text) {
  return text.replace(regexp, function(match, name) {
    return emojiCharacters[name].character;
  });
};

module.exports = {

  setMessageShown: function(peer, message) {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage: function(peer, text) {
    ActorClient.sendTextMessage(peer, replaceNames(text));
  },

  sendFileMessage: function(peer, file) {
    ActorClient.sendFileMessage(peer, file);
  },

  sendPhotoMessage: function(peer, photo) {
    ActorClient.sendPhotoMessage(peer, photo);
  },

  sendClipboardPhotoMessage: function(peer, photo) {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  }
};
