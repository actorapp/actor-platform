import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';
var ActionTypes = ActorAppConstants.ActionTypes;

import DialogStore from 'stores/DialogStore';

import { EventEmitter } from 'events';
import assign from 'object-assign';

import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _messages = [];
let _boundPeer = null;

var MessageStore = assign({}, EventEmitter.prototype, {
  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  getAll: function() {
    return _messages;
  }
});

var _bindMessages = function(messages) {
  _messages = messages;
  console.debug(messages[messages.length - 1]);
  MessageStore.emitChange();
};

MessageStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SELECT_DIALOG_PEER:
      if (_boundPeer != null) {
        ActorClient.unbindChat(_boundPeer, _bindMessages);
      }

      ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);

      _boundPeer = action.peer;

      ActorClient.bindChat(action.peer, _bindMessages);

      break;
    default:

  }
});

export default MessageStore;
