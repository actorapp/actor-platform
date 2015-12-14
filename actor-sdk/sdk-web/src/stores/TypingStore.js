/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _typing = null;

/**
 * Class representing a store for typing info.
 */
class TypingStore extends Store {
  /**
   * @returns {String}
   */
  getTyping() {
    return _typing;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.TYPING_CHANGED:
        _typing = action.typing;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new TypingStore(Dispatcher);
