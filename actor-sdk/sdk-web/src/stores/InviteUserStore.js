/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class InviteUserStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this._isInviteModalOpen = false;
    this._isInviteByLinkModalOpen = false;
    this._group = null;
    this._inviteUrl = null;
    this._inviteUserState = {};
  }

  isModalOpen() {
    return this._isInviteModalOpen;
  }

  isInviteWithLinkModalOpen() {
    return this._isInviteByLinkModalOpen;
  }

  getGroup() {
    return this._group;
  }

  getInviteUrl() {
    return this._inviteUrl;
  }

  getInviteUserState() {
    return this._inviteUserState;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.DIALOG_INFO_CHANGED:
        this._group = action.info;
        this.__emitChange();
        break;

      case ActionTypes.INVITE_USER_MODAL_SHOW:
        this._isInviteModalOpen = true;
        this._group = action.group;
        this.__emitChange();
        break;
      case ActionTypes.INVITE_USER_MODAL_HIDE:
        this._inviteUserState = {};
        this._isInviteModalOpen = false;
        this.__emitChange();
        break;

      case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
        this._isInviteByLinkModalOpen = true;
        this._group = action.group;
        this._inviteUrl = action.url;
        this.__emitChange();
        break;
      case ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE:
        this._isInviteByLinkModalOpen = false;
        this.__emitChange();
        break;

      // Invite user
      case ActionTypes.INVITE_USER:
        this._inviteUserState[action.uid] = AsyncActionStates.PROCESSING;
        this.__emitChange();
        break;
      case ActionTypes.INVITE_USER_SUCCESS:
        this._inviteUserState[action.uid] = AsyncActionStates.SUCCESS;
        this.__emitChange();
        break;
      case ActionTypes.INVITE_USER_ERROR:
        this._inviteUserState[action.uid] = AsyncActionStates.FAILURE;
        this.__emitChange();
        break;
    }
  }
}

export default new InviteUserStore(Dispatcher);
