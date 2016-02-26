/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class ArchiveStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this.isLoading = true;
    this.dialogs = [];
    this.archiveChatState = [];
  }

  isArchiveLoading() {
    return this.isLoading;
  }

  getDialogs() {
    return this.dialogs;
  }

  getArchiveChatState(id) {
    return (this.archiveChatState[id] || AsyncActionStates.PENDING);
  }

  resetArchiveChatState(id) {
    delete this.archiveChatState[id];
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.ARCHIVE_ADD:
        this.archiveChatState[action.peer.id] = AsyncActionStates.PROCESSING;
        this.__emitChange();
        break;
      case ActionTypes.ARCHIVE_ADD_SUCCESS:
        this.resetArchiveChatState(action.peer.id);
        this.__emitChange();
        break;
      case ActionTypes.ARCHIVE_ADD_ERROR:
        this.archiveChatState[action.peer.id] = AsyncActionStates.FAILURE;
        this.__emitChange();
        break;

      case ActionTypes.ARCHIVE_LOAD:
      case ActionTypes.ARCHIVE_LOAD_MORE:
        this.isLoading = true;
        this.__emitChange();
        break;

      case ActionTypes.ARCHIVE_LOAD_SUCCESS:
      case ActionTypes.ARCHIVE_LOAD_MORE_SUCCESS:
        this.isLoading = false;
        this.dialogs = action.response;
        this.__emitChange();
        break;

      default:
    }
  }
}

export default new ArchiveStore(Dispatcher);
