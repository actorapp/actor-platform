import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
//import DialogStore from './DialogStore';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _isInviteModalOpen = false,
    _isInviteByLinkModalOpen = false,
    _group = null,
    _inviteUrl = null;

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
}

let InviteUserStoreInstance = new InviteUserStore();

InviteUserStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.INVITE_USER_MODAL_SHOW:
      _isInviteModalOpen = true;
      _group = action.group;
      break;
    case ActionTypes.INVITE_USER_MODAL_HIDE:
      _isInviteModalOpen = false;
      //_group = null;
      break;
    case ActionTypes.SELECTED_DIALOG_INFO_CHANGED:
      _group = action.info;
      break;
    case ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW:
      _isInviteByLinkModalOpen = true;
      _group = action.group;
      ActorClient.getInviteUrl(_group.id)
        .then((url) => {
          _inviteUrl = url;
          InviteUserStoreInstance.emitChange();
        });
      break;
    case ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE:
      _isInviteByLinkModalOpen = false;
      break;
    default:
      return;
  }
  InviteUserStoreInstance.emitChange();
});

export default InviteUserStoreInstance;
