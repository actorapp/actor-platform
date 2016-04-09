/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class MessageArtStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      stickers: []
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.MESSAGE_ART_SHOW:
        return {
          ...state,
          isOpen: true
        };
      case ActionTypes.MESSAGE_ART_CLOSE:
        return {
          ...state,
          isOpen: false
        };
      case ActionTypes.STICKERS_SET:
        return {
          ...state,
          stickers: action.stickers
        };
      default:
        return state;
    }
  }
}

export default new MessageArtStore(Dispatcher);
