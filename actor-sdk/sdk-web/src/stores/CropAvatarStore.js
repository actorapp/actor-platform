/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class CropStore extends ReduceStore {
  getInitialState() {
    return {
      source: null,
      callback: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.CROP_MODAL_SHOW:
        return {
          ...state,
          source: action.source,
          callback: action.callback
        }
      case ActionTypes.CROP_MODAL_HIDE:
        return this.getInitialState();

      default:
        return state;
    }
  }
}

export default new CropStore(Dispatcher);
