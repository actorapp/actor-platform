import _ from 'lodash';

import ActorClient from 'utils/ActorClient';

import emojiCharacters from 'emoji-named-characters';

var variants = _.map(Object.keys(emojiCharacters), function(name) {
  return name.replace(/\+/g, '\\+');
});

var regexp = new RegExp('\\:(' + variants.join('|') + ')\\:', 'gi');

var replaceNames = function(text) {
  return text.replace(regexp, function(match, name) {
    return emojiCharacters[name].character;
  });
};

export default {

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
