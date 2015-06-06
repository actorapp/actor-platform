var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var DialogStore = require('./DialogStore');

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var ActorClient = require('../utils/ActorClient');

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

module.exports = MessageStore;
