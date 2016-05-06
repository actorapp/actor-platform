/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { throttle } from 'lodash';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ComposeActionCreators from './ComposeActionCreators';

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

  editLastMessage() {
    const uid = ActorClient.getUid();
    const { messages } = MessageStore.getState();
    const message = findLastEditableMessage(messages, uid);

    if (message) {
      ComposeActionCreators.toggleAutoFocus(false);
      dispatch(ActionTypes.MESSAGES_EDIT_START, { message });
    }
  }

  endEdit(peer, message, text) {
    if (!text) {
      this.deleteMessage(peer, message.rid);
    } else if (text !== message.content.text) {
      ActorClient.editMessage(peer, message.rid, text).catch((e) => {
        console.error(e);
      });
    }

    dispatch(ActionTypes.MESSAGES_EDIT_END);
    ComposeActionCreators.toggleAutoFocus(true);
  }
}

export default new MessageActionCreators();
