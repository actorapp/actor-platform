import { EventEmitter } from 'events';
import assign from 'object-assign';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';
var ActionTypes = ActorAppConstants.ActionTypes;

var CHANGE_EVENT = 'change';

var VisibilityStore = assign({}, EventEmitter.prototype, {
  isVisible: false,

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
});

VisibilityStore.dispatchToken = ActorAppDispatcher.register(function(action) {
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
