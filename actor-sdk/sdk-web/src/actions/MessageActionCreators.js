/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';
import { emoji } from '../utils/EmojiUtils';

const replaceColons = (text) => {
  emoji.change_replace_mode('unified');
  return emoji.replace_colons(text);
};

export default {
  setMessageShown(peer, message) {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage(peer, text) {
    ActorClient.sendTextMessage(peer, replaceColons(text));
    dispatch(ActionTypes.MESSAGE_SEND_TEXT, { peer, text });
  },

  sendFileMessage(peer, file) {
    ActorClient.sendFileMessage(peer, file);
    dispatch(ActionTypes.MESSAGE_SEND_FILE, { peer, file });
  },

  sendPhotoMessage(peer, photo) {
    ActorClient.sendPhotoMessage(peer, photo);
    dispatch(ActionTypes.MESSAGE_SEND_PHOTO, { peer, photo });
  },

  sendClipboardPhotoMessage(peer, photo) {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  },

  sendVoiceMessage(peer, duration, voice) {
    ActorClient.sendVoiceMessage(peer, duration, voice);
    dispatch(ActionTypes.MESSAGE_SEND_VOICE, { peer, duration, voice });
  },

  deleteMessage(peer, rid) {
    ActorClient.deleteMessage(peer, rid);
    dispatch(ActionTypes.MESSAGE_DELETE, { peer, rid });
  },

  addLike(peer, rid) {
    ActorClient.addLike(peer, rid);
    dispatch(ActionTypes.MESSAGE_LIKE_ADD, { peer, rid });
  },

  removeLike(peer, rid) {
    ActorClient.removeLike(peer, rid);
    dispatch(ActionTypes.MESSAGE_LIKE_REMOVE, { peer, rid });
  },

  setMessages(messages, overlay) {
    dispatch(ActionTypes.MESSAGES_CHANGED, { messages, overlay });
  },

  setSelected(selectedMesages) {
    dispatch(ActionTypes.MESSAGES_SET_SELECTED, { selectedMesages });
  }
};
