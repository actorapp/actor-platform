/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class FaviconStore extends ReduceStore {
  getInitialState() {
    return 0;
  }

  reduce(state, action) {
    if (action.type === ActionTypes.FAVICON_SET) {
      return action.counter;
    }

    return state;
  }
}

export default new FaviconStore(Dispatcher);
