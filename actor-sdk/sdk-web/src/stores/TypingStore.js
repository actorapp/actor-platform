/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class TypingStore extends ReduceStore {
  getInitialState() {
    return {
      typing: null
    };
  }

  reduce(state, action) {
    if (action.type === ActionTypes.TYPING_CHANGED) {
      return {
        typing: action.typing
      };
    }

    return state;
  }
}

export default new TypingStore(Dispatcher);
