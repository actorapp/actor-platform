/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, ModalTypes } from '../constants/ActorAppConstants';

class ModalStore extends ReduceStore {
  getInitialState() {
    return {
      prevModal: null,
      currentModal: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.PROFILE_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.PROFILE
        }
      case ActionTypes.PROFILE_HIDE:
        return this.getInitialState();

      case ActionTypes.CROP_MODAL_SHOW:
        return {
          ...state,
          prevModal: ModalTypes.PROFILE,
          currentModal: ModalTypes.CROP
        }
      case ActionTypes.CROP_MODAL_HIDE:
        return {
          ...state,
          prevModal: null,
          currentModal: state.prevModal
        }
      default:
        return state;
    }
  }
}

export default new ModalStore(Dispatcher);
