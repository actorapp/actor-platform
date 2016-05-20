/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class DraftStore extends ReduceStore {
  getInitialState() {
    return null;
  }

  getDraft() {
    return this.getState();
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.DRAFT_LOAD:
      case ActionTypes.DRAFT_CHANGE:
        return action.draft;

      default:
        return state;
    }
  }
}

export default new DraftStore(Dispatcher);
