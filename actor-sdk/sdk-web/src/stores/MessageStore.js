/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const initialRenderMessagesCount = 20;
const renderMessagesStep = 20;

/**
 * Class representing a store for messages.
 */
class MessageStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this._renderMessagesCount = initialRenderMessagesCount;
    this._messages = [];
    this._overlay = [];
    this._isLoaded = false;
    this._selectedMessages = new Immutable.Set();
  }

  /**
   * @returns {Array} All messages stored for currently bound conversation
   */
  getAll() {
    return this._messages;
  }

  getRenderMessagesCount() {
    return this._renderMessagesCount;
  }

  getMessages() {
    return this._messages;
  }

  /**
   * @returns {Array} Messages overlay
   */
  getOverlay() {
    return this._overlay;
  }

  /**
   * @returns {Boolean} is all messages loaded for current conversation
   */
  isLoaded() {
    return this._isLoaded;
  }

  isAllRendered() {
    return this._messages.length === this._renderMessagesCount;
  }

  /**
   * @returns {Array} Selected messages
   */
  getSelected() {
    return this._selectedMessages;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.BIND_DIALOG_PEER:
        this._renderMessagesCount = 0;
        this._messages = [];
        this._overlay = [];
        this._selectedMessages = new Immutable.Set();
        this.__emitChange();
        break;

      case ActionTypes.MESSAGES_CHANGED:
        this._messages = action.messages;
        this._overlay = action.overlay;
        this._isLoaded = action.isLoaded;
        this.__emitChange();
        break;

      case ActionTypes.MESSAGES_SET_SELECTED:
        this._selectedMessages = action.selectedMesages;
        this.__emitChange();
        break;

      case ActionTypes.MESSAGES_LOAD_MORE:
        this._renderMessagesCount += renderMessagesStep;
        if (this._renderMessagesCount > this._messages.length) {
          this._renderMessagesCount = this._messages.length;
        }
        this.__emitChange();
        break;

      default:
    }
  }
}

export default new MessageStore(Dispatcher);
