/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';
import { emoji } from '../utils/EmojiUtils';

const replaceColons = (text) => {
  emoji.change_replace_mode('unified');
  return emoji.replace_colons(text);
};

class MessageActionCreators {
  constructor() {
    this.setMessages = debounce(this.setMessages, 10, { maxWait: 50, leading: true });
  }

  setMessageShown(peer, message) {
    ActorClient.onMessageShown(peer, message);
  }

  sendTextMessage(peer, text) {
    ActorClient.sendTextMessage(peer, replaceColons(text));
    dispatch(ActionTypes.MESSAGE_SEND_TEXT, { peer, text });
  }

  sendFileMessage(peer, file) {
    ActorClient.sendFileMessage(peer, file);
    dispatch(ActionTypes.MESSAGE_SEND_FILE, { peer, file });
  }

  sendPhotoMessage(peer, photo) {
    ActorClient.sendPhotoMessage(peer, photo);
    dispatch(ActionTypes.MESSAGE_SEND_PHOTO, { peer, photo });
  }

  // Deprecated
  sendClipboardPhotoMessage(peer, photo) {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  }

  sendVoiceMessage(peer, duration, voice) {
    ActorClient.sendVoiceMessage(peer, duration, voice);
    dispatch(ActionTypes.MESSAGE_SEND_VOICE, { peer, duration, voice });
  }

  deleteMessage(peer, rid) {
    ActorClient.deleteMessage(peer, rid);
    dispatch(ActionTypes.MESSAGE_DELETE, { peer, rid });
  }

  addLike(peer, rid) {
    ActorClient.addLike(peer, rid);
    dispatch(ActionTypes.MESSAGE_LIKE_ADD, { peer, rid });
  }

  removeLike(peer, rid) {
    ActorClient.removeLike(peer, rid);
    dispatch(ActionTypes.MESSAGE_LIKE_REMOVE, { peer, rid });
  }

  setMessages(messages, overlay, isLoaded, receiveDate, readDate, readByMeDate) {
    dispatch(ActionTypes.MESSAGES_CHANGED, {
      messages,
      overlay,
      isLoaded,
      receiveDate,
      readDate,
      readByMeDate
    });
  }

  toggleSelected(id) {
    dispatch(ActionTypes.MESSAGES_TOGGLE_SELECTED, { id });
  }
}

export default new MessageActionCreators();
