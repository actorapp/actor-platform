var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';
var SELECT_EVENT = 'select';

var _dialogs = [];
var _selectedDialog = null;

var DialogStore = assign({}, EventEmitter.prototype, {
  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  emitSelect: function() {
    this.emit(SELECT_EVENT);
  },

  addSelectListener: function(callback) {
    this.on(SELECT_EVENT, callback);
  },

  removeSelectListener: function(callback) {
    this.removeListener(SELECT_EVENT, callback);
  },

  getSelectedDialog: function() {
    return(_selectedDialog);
  },

  getAll: function() {
    return(_dialogs);
  }
});

DialogStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SELECT_DIALOG:
      _selectedDialog = action.dialog;
      DialogStore.emitSelect();
      break;
    case ActionTypes.DIALOGS_CHANGED:
      _dialogs = action.dialogs;
      DialogStore.emitChange();
      break;
    default:

  }
});

module.exports = DialogStore;
