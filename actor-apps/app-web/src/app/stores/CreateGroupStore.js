import _ from 'lodash';

import { EventEmitter } from 'events';

import { ActionTypes } from 'constants/ActorAppConstants';

import Dispatcher from 'dispatcher/ActorAppDispatcher';

const CHANGE_EVENT = 'change';

let modalOpen = false;

const CreateGroupStore = _.assign(EventEmitter.prototype, {
  emitChange: () => {
    CreateGroupStore.emit(CHANGE_EVENT);
  },

  addChangeListener: (cb) => {
    CreateGroupStore.on(CHANGE_EVENT, cb);
  },

  removeChangeListener: (cb) => {
    CreateGroupStore.removeListener(CHANGE_EVENT, cb);
  },

  isModalOpen: () => {
    return modalOpen;
  }
});

CreateGroupStore.dispatchToken = Dispatcher.register((action) => {
  switch (action.type) {
    case ActionTypes.CREATE_GROUP_MODAL_OPEN:
      modalOpen = true;
      CreateGroupStore.emitChange();
      break;
    case ActionTypes.CREATE_GROUP_MODAL_CLOSE:
      modalOpen = false;
      CreateGroupStore.emitChange();
  }
});

export default CreateGroupStore;
