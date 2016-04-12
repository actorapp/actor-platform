/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, ModalTypes } from '../constants/ActorAppConstants';

class ModalStore extends ReduceStore {
  getInitialState() {
    return {
      current: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.PROFILE_SHOW:
        return {
          ...state,
          current: ModalTypes.PROFILE
        }
      case ActionTypes.PROFILE_HIDE:
        return this.getInitialState();
      default:
        return state;
    }
  }
}

export default new ModalStore(Dispatcher);
