/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from 'utils/ActorClient';
import mixpanel from 'utils/Mixpanel';
import Markdown from 'utils/Markdown';
import { emoji } from 'utils/EmojiUtils';

const replaceColons = (text) => {
  emoji.change_replace_mode('unified');
  const replacedText = emoji.replace_colons(text);
  return replacedText;
};

export default {
  setMessageShown: function(peer, message) {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage: function(peer, text) {
    mixpanel.track('Send Text');
    ActorClient.sendTextMessage(peer, replaceColons(text));
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
