'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActorClient = require('../utils/ActorClient');
var ActionTypes = ActorAppConstants.ActionTypes;

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _contacts = null;

var ContactStore = assign({}, EventEmitter.prototype, {
  getContacts: function() {
    return(_contacts);
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback)
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback)
  }
});

ContactStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  console.warn(action);
  switch(action.type) {
    case ActionTypes.CONTACT_ADD:
      ActorClient.addContact(action.uid);
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_REMOVE:
      ActorClient.removeContact(action.uid);
      ContactStore.emitChange();
      break;

    default:
  }
});

module.exports = ContactStore;
