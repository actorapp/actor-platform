/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class EditMessageStore extends ReduceStore {
  getInitialState() {
    return {
      message: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.MESSAGES_EDIT_START:
        return {
          ...state,
          message: action.message
        };

      case ActionTypes.MESSAGES_EDIT_END:
        return this.getInitialState();
    }

    return state;
  }
}

export default new EditMessageStore(Dispatcher);
