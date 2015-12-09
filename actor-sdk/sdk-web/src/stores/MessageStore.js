/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _messages = [];

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

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.MESSAGES_CHANGED:
        _messages = action.messages;
        this.__emitChange();
        break;
    }
  }
}

export default new MessageStore(Dispatcher);
