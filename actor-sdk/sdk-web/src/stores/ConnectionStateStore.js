/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _state = 'updating';

class ConnectionStateStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  /**
   * @returns {string} Connection state
   */
  getState() {
    return _state;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.CONNECTION_STATE_CHANGED:
        _state = action.state;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new ConnectionStateStore(Dispatcher);
