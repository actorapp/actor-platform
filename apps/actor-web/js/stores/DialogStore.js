var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var DialogActionCreators = require('../actions/DialogActionCreators');

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var ActorClient = require('../utils/ActorClient');
var LoginStore = require('./LoginStore');

var CHANGE_EVENT = 'change';
var SELECT_EVENT = 'select';
var SELECTED_CHANGE_EVENT = 'selected_change';

var _dialogs = [];
var _selectedDialog = null;
var _selectedDialogInfo = null;

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

  emitSelectedChange: function() {
    this.emit(SELECTED_CHANGE_EVENT);
  },

  addSelectedChangeListener: function(callback) {
    this.on(SELECTED_CHANGE_EVENT, callback);
  },

  removeSelectedChangeListener: function(callback) {
    this.removeListener(SELECTED_CHANGE_EVENT, callback);
  },

  getSelectedDialogInfo: function() {
    return(_selectedDialogInfo);
  },

  getSelectedDialog: function() {
    return(_selectedDialog);
  },

  getAll: function() {
    return(_dialogs);
  }
});

var setDialogs = function(dialogs) {
  // We need setTimeout here because bindDialogs dispatches event but bindDialogs itseld is called in the middle of dispatch (DialogStore)
  setTimeout(function() {
    DialogActionCreators.setDialogs(dialogs);
  }, 0);
};

var _currentPeer = null;

var onCurrentDialogInfoChange = function(info) {
  _selectedDialogInfo = info;
  //console.warn('inf', info);
  DialogActionCreators.createSelectedDialogInfoChanged(info);
};

var bindDialogInfo = function(peer) {
  _currentPeer = peer;

  switch(peer.type) {
    case 'user':
      ActorClient.bindUser(peer.id, onCurrentDialogInfoChange);
      break;
    case 'group':
      ActorClient.bindGroup(peer.id, onCurrentDialogInfoChange);
      break;
    default:

  }
};

var unbindCurrentDialogInfo = function() {
  if (_currentPeer != null) {
    switch (_currentPeer.type) {
      case 'user':
        ActorClient.unbindUser(_currentPeer.id, onCurrentDialogInfoChange);
        break;
      case 'group':
        ActorClient.unbindGroup(_currentPeer.id, onCurrentDialogInfoChange);
        break;
      default:

    }
  }
};

DialogStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SET_LOGGED_IN:
      ActorAppDispatcher.waitFor([LoginStore.dispatchToken]);

      ActorClient.bindDialogs(setDialogs);

      break;
    case ActionTypes.SELECT_DIALOG:
      unbindCurrentDialogInfo();

      _selectedDialog = action.dialog;
      DialogStore.emitSelect();

      setTimeout(function() {
        bindDialogInfo(action.dialog.peer.peer);
      }, 0);
      break;
    case ActionTypes.SELECTED_DIALOG_INFO_CHANGED:
      _selectedDialogInfo = action.info;
      DialogStore.emitSelectedChange();
      break;
    case ActionTypes.DIALOGS_CHANGED:
      _dialogs = action.dialogs;
      DialogStore.emitChange();
      break;
    default:

  }
});

module.exports = DialogStore;
