/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { throttle } from 'lodash';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';
import { prepareTextMessage, findLastEditableMessage } from '../utils/MessageUtils';

import MessageStore from '../stores/MessageStore';

class MessageActionCreators {
  constructor() {
    this.setMessages = throttle(this.setMessages, 10);
  }

  setMessageShown(peer, message) {
    ActorClient.onMessageShown(peer, message);
  }

  sendTextMessage(peer, text) {
    ActorClient.sendTextMessage(peer, prepareTextMessage(text));
    dispatch(ActionTypes.MESSAGE_SEND_TEXT, { peer, text });
  }

  editTextMessage(peer, rid, text) {
    dispatch(ActionTypes.MESSAGES_EDIT_END);
    ActorClient.editMessage(peer, rid, text).catch((e) => {
      console.error(e);
    });
  }

  deleteMessage(peer, rid) {
    ActorClient.deleteMessage(peer, rid);
    dispatch(ActionTypes.MESSAGE_DELETE, { peer, rid });
  }

  sendFileMessage(peer, file) {
    ActorClient.sendFileMessage(peer, file);
    dispatch(ActionTypes.MESSAGE_SEND_FILE, { peer, file });
  }

  sendPhotoMessage(peer, photo) {
    ActorClient.sendPhotoMessage(peer, photo);
    dispatch(ActionTypes.MESSAGE_SEND_PHOTO, { peer, photo });
  }

  sendAnimationMessage(peer, file) {
    ActorClient.sendAnimationMessage(peer, file);
  }

  // Deprecated
  sendClipboardPhotoMessage(peer, photo) {
    ActorClient.sendClipboardPhotoMessage(peer, photo);
  }

  sendVoiceMessage(peer, duration, voice) {
    ActorClient.sendVoiceMessage(peer, duration, voice);
    dispatch(ActionTypes.MESSAGE_SEND_VOICE, { peer, duration, voice });
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

  startEditMessage(message) {
    dispatch(ActionTypes.MESSAGES_EDIT_START, { message });
  }

  editLastMessage() {
    const uid = ActorClient.getUid();
    const { messages } = MessageStore.getState();
    const message = findLastEditableMessage(messages, uid);

    if (message) {
      this.startEditMessage(message);
    }
  }
}

export default new MessageActionCreators();
