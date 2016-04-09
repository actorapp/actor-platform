/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class PeopleStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      query: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.CONTACT_LIST_SHOW:
        return {
          ...state,
          isOpen: true
        };
      case ActionTypes.CONTACT_LIST_HIDE:
        return this.getInitialState();
      case ActionTypes.CONTACT_LIST_SEARCH:
        return {
          ...state,
          query: action.query
        };
      default:
        return state;
    }
  }

  isOpen() {
    return this.getState().isOpen;
  }
}

export default new PeopleStore(Dispatcher);
