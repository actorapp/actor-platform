import _ from 'lodash';

import { EventEmitter } from 'events';

import { ActionTypes } from 'constants/ActorAppConstants';

import Dispatcher from 'dispatcher/ActorAppDispatcher';

const CHANGE_EVENT = 'change';

let _modalOpen = false;

const CreateGroupStore = _.assign(EventEmitter.prototype, {
  emitChange: () => {
    CreateGroupStore.emit(CHANGE_EVENT);
  },

  addChangeListener: (callback) => {
    CreateGroupStore.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: (callback) => {
    CreateGroupStore.removeListener(CHANGE_EVENT, callback);
  },

  isModalOpen: () => {
    return _modalOpen;
  }
});

CreateGroupStore.dispatchToken = Dispatcher.register((action) => {
  switch (action.type) {
    case ActionTypes.GROUP_CREATE_MODAL_OPEN:
      _modalOpen = true;
      CreateGroupStore.emitChange();
      break;
    case ActionTypes.GROUP_CREATE_MODAL_CLOSE:
      _modalOpen = false;
      CreateGroupStore.emitChange();
      break;

    case ActionTypes.GROUP_CREATE:
    case ActionTypes.GROUP_CREATE_SUCCESS:
      CreateGroupStore.emitChange();
      break;
    case ActionTypes.GROUP_CREATE_ERROR:
      console.error('Failed to create group', action.error);
      CreateGroupStore.emitChange();
      break;
  }
});

export default CreateGroupStore;
