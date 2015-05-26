var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher.react');
var ActorAppConstants = require('../constants/ActorAppConstants.react');
var ActionTypes = ActorAppConstants.ActionTypes;

var DialogStore = require('./DialogStore.react');

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _messages = [];

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
    return(_messages);
  }
});

var _boundPeer = null;
var _bindMessages = function(messages) {
  _messages = messages;
  MessageStore.emitChange();
};

MessageStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SELECT_DIALOG:
      if (_boundPeer != null) {
        window.messenger.unbindChat(_boundPeer, _bindMessages);
      }

      ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);

      _boundPeer = action.dialog.peer.peer;
      window.messenger.bindChat(action.dialog.peer.peer, _bindMessages);

      break;
    default:

  }
});

module.exports = MessageStore;
