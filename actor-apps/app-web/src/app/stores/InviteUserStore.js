import { EventEmitter } from 'events';
import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, ChangeState } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _isInviteModalOpen = false,
    _isInviteByLinkModalOpen = false,
    _group = null,
    _inviteUrl = null,
    _changeState = [];

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

  getChangeState(gid, uid) {
    if (typeof _changeState[gid] === 'undefined') {
      _changeState[gid] = [];
    }
    if (typeof _changeState[gid][uid] === 'undefined') {
      _changeState[gid][uid] = ChangeState.INIT;
    }

    return _changeState[gid][uid];
  }

  resetChangeState(gid, uid) {
    _changeState[gid][uid] = ChangeState.INIT;
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
      _isInviteModalOpen = false;
      //_group = null;
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
    case ActionTypes.INVITE_USER:
      _changeState[action.gid][action.uid] = ChangeState.IN_PROCESS;
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_SUCCESS:
      _changeState[action.gid][action.uid] = ChangeState.SUCCESS;
      InviteUserStoreInstance.emitChange();
      break;
    case ActionTypes.INVITE_USER_ERROR:
      _changeState[action.gid][action.uid] = ChangeState.FAILURE;
      InviteUserStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default InviteUserStoreInstance;
