/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class LoggerStore extends ReduceStore {
  getInitialState() {
    return {
      logs: [],
      isOpen: false
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.LOGGER_TOGGLE:
        return {
          ...state,
          isOpen: !state.isOpen
        };
      case ActionTypes.LOGGER_APPEND:
        return {
          ...state,
          logs: [...state.logs, action.payload]
        };
      default:
        return state;
    }
  }
}

export default new LoggerStore(Dispatcher);
