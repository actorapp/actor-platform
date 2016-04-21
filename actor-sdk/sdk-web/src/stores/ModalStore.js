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
          prevModal: action.prevModal,
          currentModal: ModalTypes.CROP
        }
      case ActionTypes.CROP_MODAL_HIDE:
        return {
          ...state,
          prevModal: null,
          currentModal: state.prevModal
        }

      case ActionTypes.GROUP_LIST_SHOW:
      return {
        ...state,
        currentModal: ModalTypes.GROUP_LIST
      }
      case ActionTypes.GROUP_LIST_HIDE:
        return this.getInitialState();

      case ActionTypes.CONTACT_ADD_MODAL_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.ADD_CONTACT
        }
      case ActionTypes.CONTACT_ADD_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.GROUP_CREATE_MODAL_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.CREATE_GROUP
        }
      case ActionTypes.GROUP_CREATE_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.GROUP_EDIT_MODAL_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.EDIT_GROUP
        }
      case ActionTypes.GROUP_EDIT_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.PREFERENCES_MODAL_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.PREFERENCES
        }
      case ActionTypes.PREFERENCES_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.QUICK_SEARCH_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.QUICK_SEARCH
        }
      case ActionTypes.QUICK_SEARCH_HIDE:
        return this.getInitialState();

      case ActionTypes.INVITE_USER_MODAL_SHOW:
        return {
          ...state,
          currentModal: ModalTypes.INVITE
        }
      case ActionTypes.INVITE_USER_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
        return {
          ...state,
          prevModal: action.prevModal,
          currentModal: ModalTypes.INVITE_BY_LINK
        }
      case ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE:
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
