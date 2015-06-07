'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _modal = null;

var ModalStore = assign({}, EventEmitter.prototype, {
  getModal: function() {
    return(_modal);
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

ModalStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  console.warn(action);
  switch(action.type) {
    case ActionTypes.SHOW_MODAL:
      _modal = action.modal;
      ModalStore.emitChange();
      break;

    case ActionTypes.HIDE_MODAL:
      _modal = null;
      ModalStore.emitChange();
      break;

    default:
  }
});

module.exports = ModalStore;
