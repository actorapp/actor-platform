import _ from 'lodash';

import ActorClient from 'utils/ActorClient';

import mixpanel from 'utils/Mixpanel';

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
    mixpanel.track('Send Text');
    ActorClient.sendTextMessage(peer, replaceNames(text));
  },

  sendFileMessage: function(peer, file) {
    mixpanel.track('Send Document');
    ActorClient.sendFileMessage(peer, file);
  },

  sendPhotoMessage: function(peer, photo) {
    mixpanel.track('Send Photo');
    ActorClient.sendPhotoMessage(peer, photo);
  },

  sendClipboardPhotoMessage: function(peer, photo) {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  }
};
