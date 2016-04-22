/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class InviteUserStore extends ReduceStore {
  getInitialState() {
    return {
      query: null,
      group: null,
      inviteUrl: null,
      users: {}
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
          group: action.group
        };
      case ActionTypes.INVITE_USER_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
        return {
          ...state,
          group: action.group,
          inviteUrl: action.url
        };

      // Invite user
      case ActionTypes.INVITE_USER:
        state.users[action.uid] = AsyncActionStates.PROCESSING;
        return {
          ...state
        };
      case ActionTypes.INVITE_USER_SUCCESS:
        state.users[action.uid] = AsyncActionStates.SUCCESS;
        return {
          ...state
        };
      case ActionTypes.INVITE_USER_ERROR:
        state.users[action.uid] = AsyncActionStates.FAILURE;
        return {
          ...state
        };
      case ActionTypes.INVITE_USER_RESET:
        delete state.users[action.uid];
        return {
          ...state
        };
      default:
        return state;
    }
  }

  getGroup() {
    return this.getState().group;
  }

  getInviteUrl() {
    return this.getState().inviteUrl;
  }

  getInviteUserState() {
    return this.getState().users;
  }
}

export default new InviteUserStore(Dispatcher);
