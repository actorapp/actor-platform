/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class LoggerStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
    this._logs = [];
    this._isOpen = false;
  }

  isOpen() {
    return this._isOpen;
  }

  getLogs() {
    return this._logs;
  }

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.LOGGER_TOGGLE:
        this._isOpen = !this._isOpen;
        this.__emitChange();
        break;
      case ActionTypes.LOGGER_APPEND:
        this._logs.push(action.payload);
        this.__emitChange();
        break;
    }
  }
}

export default new LoggerStore(Dispatcher);
