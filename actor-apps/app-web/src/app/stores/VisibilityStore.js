import { EventEmitter } from 'events';
import assign from 'object-assign';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

const VisibilityStore = assign({}, EventEmitter.prototype, {
  isVisible: false,

  emitChange() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
});

VisibilityStore.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.APP_VISIBLE:
      VisibilityStore.isVisible = true;
      VisibilityStore.emitChange();
      break;
    case ActionTypes.APP_HIDDEN:
      VisibilityStore.isVisible = false;
      VisibilityStore.emitChange();
      break;
    default:

  }
});

export default VisibilityStore;
