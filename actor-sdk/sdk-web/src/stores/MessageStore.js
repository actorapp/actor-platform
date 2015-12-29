/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _messages = [];
let _overlay = [];
let _selectedMessages = new Immutable.Set();

/**
 * Class representing a store for messages.
 */
class MessageStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  /**
   * @returns {Array} All messages stored for currently bound conversation
   */
  getAll() {
    return _messages;
  }

  /**
   * @returns {Array} Meesages overlay
   */
  getOverlay() {
    return _overlay;
  }

  /**
   * @returns {Array} Selected messages
   */
  getSelected() {
    return _selectedMessages;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.SELECT_DIALOG_PEER:
        _selectedMessages = new Immutable.Set();
        this.__emitChange();
        break;

      case ActionTypes.MESSAGES_CHANGED:
        _messages = action.messages;
        _overlay = action.overlay;
        this.__emitChange();
        break;

      case ActionTypes.MESSAGES_SET_SELECTED:
        _selectedMessages = action.selectedMesages;
        this.__emitChange();
        break;

      default:
    }
  }
}

export default new MessageStore(Dispatcher);
