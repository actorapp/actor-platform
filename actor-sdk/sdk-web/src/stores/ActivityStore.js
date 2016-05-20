/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class ActivityStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.CALL_MODAL_OPEN:
      case ActionTypes.ACTIVITY_HIDE:
        return this.getInitialState();
      case ActionTypes.ACTIVITY_SHOW:
        return {
          isOpen: true
        };
      default:
        return state;
    }
  }

  isOpen() {
    return this.getState().isOpen;
  }
}

export default new ActivityStore(Dispatcher);
