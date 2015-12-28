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
  setMessageShown: (peer, message) => {
    ActorClient.onMessageShown(peer, message);
  },

  sendTextMessage: (peer, text) => {
    dispatch(ActionTypes.MESSAGE_SEND_TEXT, {
      peer, text
    });
    ActorClient.sendTextMessage(peer, replaceColons(text));
  },

  sendFileMessage: (peer, file) => {
    dispatch(ActionTypes.MESSAGE_SEND_FILE, {
      peer, file
    });
    ActorClient.sendFileMessage(peer, file);
  },

  sendPhotoMessage: (peer, photo) => {
    dispatch(ActionTypes.MESSAGE_SEND_PHOTO, {
      peer, photo
    });
    ActorClient.sendPhotoMessage(peer, photo);
  },

  sendClipboardPhotoMessage: (peer, photo) => {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  },

  deleteMessage: (peer, rid) => {
    ActorClient.deleteMessage(peer, rid);
    dispatch(ActionTypes.MESSAGE_DELETE, {
      peer, rid
    });
  },

  addLike: (peer, rid) => {
    ActorClient.addLike(peer, rid);
  },

  removeLike: (peer, rid) => {
    ActorClient.removeLike(peer, rid);
  },

  setMessages(messages) {
    dispatch(ActionTypes.MESSAGES_CHANGED, { messages });
  },

  setSelected(selectedMesages) {
    dispatch(ActionTypes.MESSAGES_SET_SELECTED, { selectedMesages });
  }
};
