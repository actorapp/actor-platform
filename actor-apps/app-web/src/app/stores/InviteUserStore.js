/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { EventEmitter } from 'events';
import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _isInviteModalOpen = false,
    _isInviteByLinkModalOpen = false,
    _group = null,
    _inviteUrl = null,
    _inviteUserState = [];

class InviteUserStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

  isModalOpen() {
    return _isInviteModalOpen;
  }

  isInviteWithLinkModalOpen() {
    return _isInviteByLinkModalOpen;
  }

  getGroup() {
    return _group;
  }

  getInviteUrl() {
    return _inviteUrl;
  }

  getInviteUserState(uid) {
    return (_inviteUserState[uid] || AsyncActionStates.PENDING);
  }

  resetInviteUserState(uid) {
    delete _inviteUserState[uid];
  }
}

let InviteUserStoreInstance = new InviteUserStore();

InviteUserStoreInstance.dispatchToken = register(action => {
  switch(action.type) {
    case ActionTypes.SELECTED_DIALOG_INFO_CHANGED:
      _group = action.info;
      InviteUserStoreInstance.emitChange();
      break;

    case ActionTypes.INVITE_USER_MODAL_SHOW:
      _isInviteModalOpen = true;
      _group = action.group;
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_MODAL_HIDE:
      _inviteUserState = [];
      _isInviteModalOpen = false;
      InviteUserStoreInstance.emitChange();
      break;

    case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
      _isInviteByLinkModalOpen = true;
      _group = action.group;
      ActorClient.getInviteUrl(_group.id)
        .then((url) => {
          _inviteUrl = url;
          InviteUserStoreInstance.emitChange();
        });
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE:
      _isInviteByLinkModalOpen = false;
      InviteUserStoreInstance.emitChange();
      break;

    // Invite user
    case ActionTypes.INVITE_USER:
      _inviteUserState[action.uid] = AsyncActionStates.PROCESSING;
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_SUCCESS:
      _inviteUserState[action.uid] = AsyncActionStates.SUCCESS;
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_ERROR:
      _inviteUserState[action.uid] = AsyncActionStates.FAILURE;
      InviteUserStoreInstance.emitChange();
      break;
  }
});

export default InviteUserStoreInstance;
