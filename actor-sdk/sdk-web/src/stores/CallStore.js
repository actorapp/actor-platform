/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class CallStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      isFloating: false,
      id: null,
      peer: null,
      state: null,
      members: null,
      isMuted: false,
      isOutgoing: false
    };
  }

  reduce(state, action) {
    switch(action.type) {
      case ActionTypes.CALL_MODAL_OPEN:
        return {
          ...state,
          isOpen: true,
          id: action.id
        };
      case ActionTypes.CALL_MODAL_HIDE:
        return this.getInitialState();
      case ActionTypes.CALL_CHANGED:
        return {
          ...state,
          time: '00:00',
          peer: action.call.peer,
          state: action.call.state,
          members: action.call.members,
          isOutgoing: action.call.isOutgoing
        };
      case ActionTypes.CALL_TIME_CHANGED:
        return {
          ...state,
          time: action.time
        };
      case ActionTypes.CALL_MUTE_TOGGLE:
        return {
          ...state,
          isMuted: !state.isMuted
        };
      case ActionTypes.CALL_FLOAT_TOGGLE:
        return {
          ...state,
          isFloating: !state.isFloating
        };
      default:
        return state;
    }
  }
}

export default new CallStore(Dispatcher);
