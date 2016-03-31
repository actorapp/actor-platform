/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import { Map } from 'immutable';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class InviteUserStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      isInviteByLinkOpen: false,
      group: null,
      inviteUrl: null,
      users: new Map()
    };
  }

  reduce(state, action) {
    switch(action.type) {
      case ActionTypes.DIALOG_INFO_CHANGED:
        return {
          ...state,
          group: action.info
        };
      case ActionTypes.INVITE_USER_MODAL_SHOW:
        return {
          ...state,
          isOpen: true,
          group: action.group
        };
      case ActionTypes.INVITE_USER_MODAL_HIDE:
        return {
          ...state,
          isOpen: false,
          users: state.users.clear()
        };
      case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
        return {
          ...state,
          isInviteByLinkOpen: true,
          group: action.group,
          inviteUrl: action.url
        };
      case ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE:
        return {
          ...state,
          isInviteByLinkOpen: false
        };

      // Invite user
      case ActionTypes.INVITE_USER:
        return {
          ...state,
          users: state.users.set(action.uid, AsyncActionStates.PROCESSING)
        };
      case ActionTypes.INVITE_USER_SUCCESS:
        return {
          ...state,
          users: state.users.set(action.uid, AsyncActionStates.SUCCESS)
        };
      case ActionTypes.INVITE_USER_ERROR:
        return {
          ...state,
          users: state.users.set(action.uid, AsyncActionStates.FAILURE)
        };
      case ActionTypes.INVITE_USER_RESET:
        return {
          ...state,
          users: state.users.delete(action.uid)
        };
      default:
        return state;
    }
  }

  isModalOpen() {
    return this.getState().isOpen;
  }

  isInviteWithLinkModalOpen() {
    return this.getState().isInviteByLinkOpen;
  }

  getGroup() {
    return this.getState().group;
  }

  getInviteUrl() {
    return this.getState().inviteUrl;
  }

  getInviteUserState(uid) {
    const { users } = this.getState();
    return users.get(uid) || AsyncActionStates.PENDING;
  }
}

export default new InviteUserStore(Dispatcher);
