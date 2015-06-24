import { EventEmitter } from 'events';
import assign from 'object-assign';

import ActorClient from '../utils/ActorClient';

import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import DialogStore from '../stores/DialogStore';

const DRAFT_LOAD_EVENT = 'draft_load';

let _draft = null;

const DraftStore = assign({}, EventEmitter.prototype, {
  emitLoadDraft() {
    this.emit(DRAFT_LOAD_EVENT);
  },

  addLoadDraftListener(callback) {
    this.on(DRAFT_LOAD_EVENT, callback);
  },

  removeLoadDraftListener(callback) {
    this.removeListener(DRAFT_LOAD_EVENT, callback);
  },

  getDraft() {
    return _draft;
  }
});

DraftStore.dispatchToken = ActorAppDispatcher.register((action) => {
  switch (action.type) {
    case ActionTypes.DRAFT_LOAD:
      _draft = ActorClient.loadDraft(action.peer);
      DraftStore.emitLoadDraft();
      break;

    case ActionTypes.DRAFT_SAVE:
      _draft = action.draft;
      break;

    case ActionTypes.SELECT_DIALOG_PEER:
      const lastPeer = DialogStore.getLastPeer();
      if (_draft !== null) {
        ActorClient.saveDraft(lastPeer, _draft);
      }
      break;

    default:
  }
});

export default DraftStore;
